package org.jetlinks.zlmedia.runner.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.PlatformDependent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.IOUtils;
import org.jetlinks.zlmedia.restful.ZLMediaConfigs;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EmbeddedProcessMediaRuntime extends ProcessZLMediaRuntime {
    private static final Map<String, String> installed = new ConcurrentHashMap<>();

    public EmbeddedProcessMediaRuntime(String workdir) {
        this(workdir, new ZLMediaConfigs());
    }

    public EmbeddedProcessMediaRuntime(String workdir, ZLMediaConfigs configs) {
        super(install(workdir), configs);
    }

    public EmbeddedProcessMediaRuntime(String workdir,
                                       WebClient.Builder builder,
                                       ObjectMapper mapper,
                                       ZLMediaConfigs configs) {
        super(install(workdir), builder, mapper, configs);
    }


    static String install(String workdir) {
        return installed.computeIfAbsent(
            workdir,
            dir -> install0(
                // zlmedia-native/linux/x86_64
                "zlmedia-native/"
                    + PlatformDependent.normalizedOs() + "/"
                    + PlatformDependent.normalizedArch(),
                workdir
            ));

    }


    @SneakyThrows
    private static String installTar(InputStream tar, String workdir) {
        String mediaServer = null;
        try (TarArchiveInputStream zip = new TarArchiveInputStream(tar)) {
            TarArchiveEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String filename = entry.getName();
                if (filename.contains(" __MACOSX") || filename.endsWith(".DS_Store")) {
                    continue;
                }
                if (filename.endsWith("/")) {
                    continue;
                }
                Path copyTo = Paths.get(workdir, filename);
                Files.createDirectories(copyTo.getParent());

                // 处理软链接
                if (entry.isSymbolicLink()) {
                    Files.createSymbolicLink(copyTo, Paths.get(entry.getLinkName()));
                    continue;
                }

                File copyToFile = copyTo.toFile();
                if (copyToFile.isDirectory()) {
                    continue;
                }
                log.debug("copy {} to {}", filename, copyTo);

                try (OutputStream output = Files.newOutputStream(
                    copyTo,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                    StreamUtils.copy(zip, output);
                }
                String _fileName = copyToFile.getName();
                if (_fileName.equals("MediaServer") ||
                    //windows
                    _fileName.equals("MediaServer.exe")) {
                    mediaServer = copyTo.toString();
                }
                // chmod +x MediaServer
                chmodX(copyTo);
            }
        }
        return mediaServer;
    }

    @SneakyThrows
    private static String install0(String basePath, String workdir) {
        String[] supports = {".zip", ".tar.gz",".tar"};
        String suffix = null;
        InputStream stream = null;
        for (String _suffix : supports) {
            String file = basePath + _suffix;
            Resource resource = new FileSystemResource(file);
            if (!resource.exists()) {
                resource = new ClassPathResource(file);
            }
            if (resource.exists()) {
                suffix = _suffix;
                stream = resource.getInputStream();
                break;
            }
        }
        if (suffix == null) {
            throw new IllegalAccessException("Not found ZLMedia Runtime Lib" + workdir);
        }

        log.debug("install ZLMediaKit to {}", workdir);
        try {
            String mediaServer = switch (suffix) {
                case ".zip" -> installZip(stream, workdir);
                case ".tar" -> installTar(stream, workdir);
                case ".tar.gz" -> installTar(new GzipCompressorInputStream(stream), workdir);
                default -> throw new IllegalStateException("Unexpected file: " + suffix);
            };

            if (mediaServer == null) {
                throw new IllegalAccessException("No process file 'MediaServer' found in:" + workdir);
            }

            //copy config.ini
            ClassPathResource config = new ClassPathResource("zlmedia-native/config.ini");
            try (InputStream input = config.getInputStream();
                 OutputStream output = Files.newOutputStream(
                     Paths.get(mediaServer).getParent().resolve("config.ini"),
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING,
                     StandardOpenOption.WRITE)) {
                StreamUtils.copy(input, output);
            }
            return mediaServer;
        } catch (Throwable e) {
            log.error("install ZLMediaKit error", e);
            throw e;
        }
    }

    @SneakyThrows
    private static String installZip(InputStream stream, String workdir) {
        String mediaServer = null;
        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(stream)) {
            ZipArchiveEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String filename = entry.getName();
                if (filename.contains(" __MACOSX") || filename.endsWith(".DS_Store")) {
                    continue;
                }
                if (filename.endsWith("/")) {
                    continue;
                }
                Path copyTo = Paths.get(workdir, filename);
                Files.createDirectories(copyTo.getParent());

                File copyToFile = copyTo.toFile();
                if (copyToFile.isDirectory()) {
                    continue;
                }
                log.debug("copy {} to {}", filename, copyTo);

                try (OutputStream output = Files.newOutputStream(
                    copyTo,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                    StreamUtils.copy(zip, output);
                }
                String _fileName = copyToFile.getName();
                if (_fileName.equals("MediaServer") ||
                    //windows
                    _fileName.equals("MediaServer.exe")) {
                    mediaServer = copyTo.toString();
                }
                // chmod +x MediaServer
                chmodX(copyTo);
            }
        }

        return mediaServer;
    }

    private static void chmodX(Path file) {
        if (PlatformDependent.isWindows()) {
            return;
        }
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(file, PosixFileAttributeView.class);
            view.setPermissions(Sets.newHashSet(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.OTHERS_READ));
        } catch (Throwable ignore) {
        }
    }

}
