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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Runs the embedded ZLMedia process from an installed native archive.
 *
 * Legacy constructors retain filesystem-first resource lookup. The native-artifact constructor
 * only selects classpath archives whose owning JAR declares the requested Maven artifactId.
 */
@Slf4j
public class EmbeddedProcessMediaRuntime extends ProcessZLMediaRuntime {
    private static final Map<String, String> installed = new ConcurrentHashMap<>();
    private static final String[] SUPPORTED_ARCHIVES = {".zip", ".tar.gz", ".tar"};

    public EmbeddedProcessMediaRuntime(String workdir) {
        this(workdir, new ZLMediaConfigs());
    }

    public EmbeddedProcessMediaRuntime(String workdir, ZLMediaConfigs configs) {
        super(install(workdir), configs);
    }

    /**
     * Creates a runtime using the native archive contributed by the requested Maven artifact.
     *
     * @param workdir       installation directory and cache key
     * @param configs       runtime configuration
     * @param nativeArtifact exact Maven artifactId of the JAR that owns the native archive
     */
    public EmbeddedProcessMediaRuntime(String workdir,
                                       ZLMediaConfigs configs,
                                       String nativeArtifact) {
        super(install(workdir, nativeArtifact), configs);
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

    static String install(String workdir, String nativeArtifact) {
        String normalizedOs = PlatformDependent.normalizedOs();
        String normalizedArch = PlatformDependent.normalizedArch();
        String basePath = "zlmedia-native/" + normalizedOs + "/" + normalizedArch;
        List<String> searchedPaths = createSearchedPaths(basePath);
        assertNativeArtifact(nativeArtifact, normalizedOs, normalizedArch, searchedPaths);
        return installed.computeIfAbsent(
            workdir,
            dir -> install0(
                selectNativeResource(
                    searchedPaths,
                    nativeArtifact,
                    normalizedOs,
                    normalizedArch
                ),
                workdir
            )
        );
    }

    private static void assertNativeArtifact(String nativeArtifact,
                                             String normalizedOs,
                                             String normalizedArch,
                                             List<String> searchedPaths) {
        if (nativeArtifact == null || nativeArtifact.isBlank()) {
            throw new IllegalArgumentException(
                resourceSelectionError(
                    "nativeArtifact must not be blank",
                    nativeArtifact,
                    normalizedOs,
                    normalizedArch,
                    searchedPaths,
                    List.of()
                )
            );
        }
    }

    private static List<String> createSearchedPaths(String basePath) {
        List<String> searchedPaths = new ArrayList<>(SUPPORTED_ARCHIVES.length);
        for (String suffix : SUPPORTED_ARCHIVES) {
            searchedPaths.add(basePath + suffix);
        }
        return List.copyOf(searchedPaths);
    }

    private static NativeResource selectNativeResource(List<String> searchedPaths,
                                                       String nativeArtifact,
                                                       String normalizedOs,
                                                       String normalizedArch) {
        List<String> discoveredResources = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException(
                resourceSelectionError(
                    "Context ClassLoader is not available",
                    nativeArtifact,
                    normalizedOs,
                    normalizedArch,
                    searchedPaths,
                    discoveredResources
                )
            );
        }

        try {
            for (int i = 0; i < SUPPORTED_ARCHIVES.length; i++) {
                String suffix = SUPPORTED_ARCHIVES[i];
                String resourcePath = searchedPaths.get(i);
                Enumeration<URL> resources = classLoader.getResources(resourcePath);
                List<URL> matches = new ArrayList<>();
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    discoveredResources.add(resource.toExternalForm());
                    if (matchesArtifact(resource, nativeArtifact)) {
                        matches.add(resource);
                    }
                }
                if (matches.size() > 1) {
                    throw new IllegalStateException(
                        resourceSelectionError(
                            "Multiple native resources matched suffix " + suffix,
                            nativeArtifact,
                            normalizedOs,
                            normalizedArch,
                            searchedPaths,
                            discoveredResources
                        )
                    );
                }
                if (matches.size() == 1) {
                    return new NativeResource(suffix, matches.get(0));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                resourceSelectionError(
                    "Failed to inspect native resources",
                    nativeArtifact,
                    normalizedOs,
                    normalizedArch,
                    searchedPaths,
                    discoveredResources
                ),
                e
            );
        }

        throw new IllegalStateException(
            resourceSelectionError(
                "No native resource matched",
                nativeArtifact,
                normalizedOs,
                normalizedArch,
                searchedPaths,
                discoveredResources
            )
        );
    }

    private static boolean matchesArtifact(URL resource, String nativeArtifact) throws IOException {
        URLConnection connection = resource.openConnection();
        if (connection instanceof JarURLConnection jarConnection) {
            jarConnection.setUseCaches(false);
            try (JarFile jar = jarConnection.getJarFile()) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (entry.isDirectory()
                        || !entryName.startsWith("META-INF/maven/")
                        || !entryName.endsWith("/pom.properties")) {
                        continue;
                    }
                    Properties properties = new Properties();
                    try (InputStream input = jar.getInputStream(entry)) {
                        properties.load(input);
                    }
                    if (nativeArtifact.equals(properties.getProperty("artifactId"))) {
                        return true;
                    }
                }
            }
            return false;
        }
        if ("file".equalsIgnoreCase(resource.getProtocol())) {
            // The artifact-aware entry never reads exploded or relative filesystem resources.
            return false;
        }
        throw new IOException(
            "Unsupported classpath resource connection: " + resource.toExternalForm()
        );
    }

    private static String resourceSelectionError(String reason,
                                                 String nativeArtifact,
                                                 String normalizedOs,
                                                 String normalizedArch,
                                                 List<String> searchedPaths,
                                                 List<String> discoveredResources) {
        String artifact = nativeArtifact == null
            ? "<null>"
            : nativeArtifact.isBlank() ? "<blank>" : nativeArtifact;
        return reason
            + ", nativeArtifact=" + artifact
            + ", normalizedOs=" + normalizedOs
            + ", normalizedArch=" + normalizedArch
            + ", searchedPaths=" + searchedPaths
            + ", discoveredResources=" + discoveredResources;
    }


    @SneakyThrows
    private static String installTar(InputStream tar, String workdir) {
        String mediaServer = null;
        List<Tuple2<Path, Path>> symbolicLink = new ArrayList<>();

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
                    symbolicLink.add(
                        Tuples.of(copyTo, Paths.get(entry.getLinkName()))
                    );
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
                } catch (IOException e) {
                    // copy的文件失败了,但是文件还存在,文件被占用拒绝了?
                    if (!copyToFile.exists()) {
                        throw e;
                    }
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

        for (Tuple2<Path, Path> objects : symbolicLink) {
            Path link = objects.getT1();
            Path target = objects.getT2();
            try {
                Files.createSymbolicLink(link, target);
            } catch (FileAlreadyExistsException ignore) {

            }
        }
        return mediaServer;
    }

    @SneakyThrows
    private static String install0(String basePath, String workdir) {
        String suffix = null;
        InputStream stream = null;
        for (String _suffix : SUPPORTED_ARCHIVES) {
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
        return install0(suffix, stream, workdir);
    }

    @SneakyThrows
    private static String install0(NativeResource resource, String workdir) {
        return install0(resource.suffix(), resource.openStream(), workdir);
    }

    @SneakyThrows
    private static String install0(String suffix, InputStream stream, String workdir) {
        try (InputStream inputStream = stream) {
            log.debug("install ZLMediaKit to {}", workdir);
            try {
                String mediaServer = switch (suffix) {
                    case ".zip" -> installZip(inputStream, workdir);
                    case ".tar" -> installTar(inputStream, workdir);
                    case ".tar.gz" -> installTar(new GzipCompressorInputStream(inputStream), workdir);
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
                } catch (IOException e) {
                    // copy的文件失败了,但是文件还存在,文件被占用拒绝了?
                    if (!copyToFile.exists()) {
                        throw e;
                    }
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

    private record NativeResource(String suffix, URL url) {

        private InputStream openStream() throws IOException {
            URLConnection connection = url.openConnection();
            // Avoid retaining the owning JAR after installation through the global URL cache.
            connection.setUseCaches(false);
            return connection.getInputStream();
        }
    }

}
