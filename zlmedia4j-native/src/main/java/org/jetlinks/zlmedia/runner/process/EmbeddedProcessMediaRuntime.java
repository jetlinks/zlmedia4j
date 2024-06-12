package org.jetlinks.zlmedia.runner.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.Sets;
import org.jetlinks.zlmedia.runner.ZLMediaConfigs;
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

@Slf4j
public class EmbeddedProcessMediaRuntime extends ProcessZLMediaRuntime {

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


    static String install(String target) {
        return install(
            // zlmedia-native/linux/x86_64
            "zlmedia-native/"
                + PlatformDependent.normalizedOs() + "/"
                + PlatformDependent.normalizedArch()
                + ".zip",
            target
        );
    }

    static String install(String file, String target) {
        String mediaServer = null;
        String path = file.contains(".") ? file.substring(0, file.lastIndexOf(".")) : file;

        try {
            Resource resource = new FileSystemResource(file);
            if (!resource.exists()) {
                resource = new ClassPathResource(file);
            }
            log.debug("install ZLMediaKit to {}/{}", target, path);
            try (InputStream stream = resource.getInputStream();
                 ZipArchiveInputStream zip = new ZipArchiveInputStream(stream)) {
                ZipArchiveEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String filename = entry.getName();
                    if(filename.startsWith(" __MACOSX")||filename.endsWith(".DS_Store")){
                        continue;
                    }
                    if (filename.endsWith("/")) {
                        continue;
                    }

                    Path copyTo = Paths.get(target, path, filename);
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

                    if (filename.endsWith("MediaServer")||
                        //windows
                        filename.endsWith("MediaServer.exe")) {
                        mediaServer = copyTo.toString();
                    }
                    // chmod +x MediaServer
                    chmodX(copyTo);
                }
            }

        } catch (Throwable e) {
            log.error("install ZLMediaKit error", e);
            return null;
        }
        //copy config.ini
        if (mediaServer == null) {
            return null;
        }

        ClassPathResource config = new ClassPathResource("zlmedia-native/config.ini");
        try (InputStream input = config.getInputStream();
             OutputStream output = Files.newOutputStream(
                 Paths.get(mediaServer).getParent().resolve("config.ini"),
                 StandardOpenOption.CREATE,
                 StandardOpenOption.TRUNCATE_EXISTING,
                 StandardOpenOption.WRITE)) {
            StreamUtils.copy(input, output);
        } catch (Throwable ignore) {

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
                PosixFilePermission.values()));
        } catch (Throwable ignore) {
        }
    }
}
