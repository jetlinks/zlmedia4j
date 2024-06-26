package org.jetlinks.zlmedia.runner.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.PlatformDependent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.Sets;
import org.jetlinks.zlmedia.restful.ZLMediaConfigs;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
            dir -> install(
                // zlmedia-native/linux/x86_64
                "zlmedia-native/"
                    + PlatformDependent.normalizedOs() + "/"
                    + PlatformDependent.normalizedArch()
                    + ".zip",
                workdir
            ));

    }

    @SneakyThrows
    private static String install(String file, String workdir) {
        String mediaServer = null;

        try {
            Resource resource = new FileSystemResource(file);
            if (!resource.exists()) {
                resource = new ClassPathResource(file);
            }
            log.debug("install ZLMediaKit to {}", workdir);
            try (InputStream stream = resource.getInputStream();
                 ZipArchiveInputStream zip = new ZipArchiveInputStream(stream)) {
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
                    File copyToFile = copyTo.toFile();
                    if (copyToFile.isDirectory()) {
                        continue;
                    }
                    boolean ignore = copyToFile.getParentFile().mkdirs();
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

        } catch (Throwable e) {
            log.error("install ZLMediaKit error", e);
            throw e;
        }

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
        } catch (Throwable e) {
            throw e;
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
