package org.jetlinks.zlmedia.runner.process;

import io.netty.util.internal.PlatformDependent;
import org.jetlinks.zlmedia.restful.ZLMediaConfigs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmbeddedProcessMediaRuntimeResourceTest {

    private static final String BM_ARTIFACT = "zlmedia-native-bm";
    private static final String CV_ARTIFACT = "zlmedia-native-cv";

    @TempDir
    Path tempDir;

    @Test
    void selectsCvNativeResourceByArtifactId() throws Throwable {
        Path bmJar = createNativeJar("bm.jar", BM_ARTIFACT, "BM");
        Path cvJar = createNativeJar("cv.jar", CV_ARTIFACT, "CV");
        Path workdir = tempDir.resolve("cv-runtime");

        withNativeJars(List.of(bmJar, cvJar), () -> {
            new EmbeddedProcessMediaRuntime(workdir.toString(), new ZLMediaConfigs(), CV_ARTIFACT);
            return null;
        });

        assertEquals("CV", Files.readString(workdir.resolve("MediaServer")));
    }

    @Test
    void selectsBmNativeResourceByArtifactId() throws Throwable {
        Path cvJar = createNativeJar("cv.jar", CV_ARTIFACT, "CV");
        Path bmJar = createNativeJar("bm.jar", BM_ARTIFACT, "BM");
        Path workdir = tempDir.resolve("bm-runtime");

        withNativeJars(List.of(cvJar, bmJar), () -> {
            new EmbeddedProcessMediaRuntime(workdir.toString(), new ZLMediaConfigs(), BM_ARTIFACT);
            return null;
        });

        assertEquals("BM", Files.readString(workdir.resolve("MediaServer")));
    }

    @Test
    void closesNativeResourceWhenTarGzDecompressionFails() throws Throwable {
        Path jar = createNativeJar(
            "invalid-tar-gz.jar",
            CV_ARTIFACT,
            ".tar.gz",
            "invalid-gzip-content".getBytes(StandardCharsets.UTF_8)
        );

        withNativeJars(
            List.of(jar),
            () -> assertThrows(
                IOException.class,
                () -> new EmbeddedProcessMediaRuntime(
                    tempDir.resolve("invalid-tar-gz-runtime").toString(),
                    new ZLMediaConfigs(),
                    CV_ARTIFACT
                )
            )
        );

        Files.delete(jar);
    }

    @Test
    void rejectsMissingNativeArtifactWhenNoResourcesExist() throws Throwable {
        String missingArtifact = "zlmedia-native-missing";

        IllegalStateException error = withNoNativeResources(
            () -> assertThrows(
                IllegalStateException.class,
                () -> new EmbeddedProcessMediaRuntime(
                    tempDir.resolve("missing-runtime").toString(),
                    new ZLMediaConfigs(),
                    missingArtifact
                )
            )
        );

        assertSelectionContext(error, missingArtifact);
    }

    @Test
    void rejectsDuplicateNativeArtifactForSameSuffix() throws Throwable {
        Path firstJar = createNativeJar("duplicate-first.jar", CV_ARTIFACT, "FIRST");
        Path secondJar = createNativeJar("duplicate-second.jar", CV_ARTIFACT, "SECOND");

        IllegalStateException error = withNativeJars(
            List.of(firstJar, secondJar),
            () -> assertThrows(
                IllegalStateException.class,
                () -> new EmbeddedProcessMediaRuntime(
                    tempDir.resolve("duplicate-runtime").toString(),
                    new ZLMediaConfigs(),
                    CV_ARTIFACT
                )
            )
        );

        assertAll(
            () -> assertTrue(error.getMessage().contains(CV_ARTIFACT)),
            () -> assertTrue(error.getMessage().contains(PlatformDependent.normalizedOs())),
            () -> assertTrue(error.getMessage().contains(PlatformDependent.normalizedArch())),
            () -> assertTrue(error.getMessage().contains(nativeResourcePath())),
            () -> assertTrue(error.getMessage().contains(firstJar.getFileName().toString())),
            () -> assertTrue(error.getMessage().contains(secondJar.getFileName().toString()))
        );
    }

    @Test
    void rejectsBlankNativeArtifactBeforeResourceLookup() {
        IllegalArgumentException nullError = assertThrows(
            IllegalArgumentException.class,
            () -> new EmbeddedProcessMediaRuntime(
                tempDir.resolve("null-artifact").toString(),
                new ZLMediaConfigs(),
                null
            )
        );
        IllegalArgumentException blankError = assertThrows(
            IllegalArgumentException.class,
            () -> new EmbeddedProcessMediaRuntime(
                tempDir.resolve("blank-artifact").toString(),
                new ZLMediaConfigs(),
                "  "
            )
        );

        assertSelectionContext(nullError, "<null>");
        assertSelectionContext(blankError, "<blank>");
    }

    private Path createNativeJar(String fileName, String artifactId, String marker) throws IOException {
        return createNativeJar(fileName, artifactId, ".zip", createNativeZip(marker));
    }

    private Path createNativeJar(String fileName,
                                 String artifactId,
                                 String suffix,
                                 byte[] archive) throws IOException {
        Path jar = tempDir.resolve(fileName);
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            writeJarEntry(output, nativeResourcePath() + suffix, archive);
            writeJarEntry(
                output,
                "META-INF/maven/org.jetlinks/" + artifactId + "/pom.properties",
                ("artifactId=" + artifactId + "\n").getBytes(StandardCharsets.UTF_8)
            );
        }
        return jar;
    }

    private byte[] createNativeZip(String marker) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream output = new ZipOutputStream(bytes)) {
            output.putNextEntry(new ZipEntry("MediaServer"));
            output.write(marker.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return bytes.toByteArray();
    }

    private void writeJarEntry(JarOutputStream output, String name, byte[] content) throws IOException {
        output.putNextEntry(new JarEntry(name));
        output.write(content);
        output.closeEntry();
    }

    private String nativeResourcePath() {
        return "zlmedia-native/"
            + PlatformDependent.normalizedOs()
            + "/"
            + PlatformDependent.normalizedArch();
    }

    private void assertSelectionContext(Throwable error, String nativeArtifact) {
        String message = error.getMessage();
        assertAll(
            () -> assertTrue(message.contains("nativeArtifact=" + nativeArtifact)),
            () -> assertTrue(message.contains("normalizedOs=" + PlatformDependent.normalizedOs())),
            () -> assertTrue(message.contains("normalizedArch=" + PlatformDependent.normalizedArch())),
            () -> assertTrue(message.contains(nativeResourcePath() + ".zip")),
            () -> assertTrue(message.contains(nativeResourcePath() + ".tar.gz")),
            () -> assertTrue(message.contains(nativeResourcePath() + ".tar"))
        );
    }

    private <T> T withNoNativeResources(ThrowingSupplier<T> action) throws Throwable {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[0], null)) {
            Thread.currentThread().setContextClassLoader(classLoader);
            return action.get();
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private <T> T withNativeJars(List<Path> jars, ThrowingSupplier<T> action) throws Throwable {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        URL[] urls = jars
            .stream()
            .map(this::toUrl)
            .toArray(URL[]::new);
        try (URLClassLoader classLoader = new URLClassLoader(urls, previous)) {
            Thread.currentThread().setContextClassLoader(classLoader);
            return action.get();
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
