package org.jetlinks.zlmedia.runner.process;

import lombok.SneakyThrows;
import org.jetlinks.zlmedia.runner.ZLMediaConfigs;
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
        ZLMediaConfigs configs= new ZLMediaConfigs();
        configs.getPorts().setRtsp(11554);
        configs.getPorts().setRtmp(11935);
        configs.getPorts().setSrt(19000);

        EmbeddedProcessMediaRuntime runtime = new EmbeddedProcessMediaRuntime("target/zlmedia",configs);

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