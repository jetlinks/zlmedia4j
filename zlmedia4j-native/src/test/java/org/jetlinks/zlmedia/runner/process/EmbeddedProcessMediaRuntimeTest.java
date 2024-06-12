package org.jetlinks.zlmedia.runner.process;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddedProcessMediaRuntimeTest {

    @Test
    void testInstall() {

        String cmd = EmbeddedProcessMediaRuntime.
            install("zlmedia-native/test/x86_64.zip", "target/tmp");
        System.out.println(cmd);
        assertNotNull(cmd);
        assertTrue(new File(cmd).exists());

    }

    @Test
    @SneakyThrows
    void testStart() {
        EmbeddedProcessMediaRuntime runtime = new EmbeddedProcessMediaRuntime("target/zlmedia");

        try {
            runtime.start()
                   .as(StepVerifier::create)
                   .expectComplete()
                   .verify();

            runtime.getOperations()
                   .opsForState()
                   .getConfigs()
                   .doOnNext(System.out::println)
                   .as(StepVerifier::create)
                   .expectNextCount(1)
                   .verifyComplete();

//            Thread.sleep(10000000);
        } finally {
            runtime.dispose();
        }

    }

}