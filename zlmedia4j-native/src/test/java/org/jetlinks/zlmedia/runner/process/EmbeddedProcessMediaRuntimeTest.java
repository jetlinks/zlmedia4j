package org.jetlinks.zlmedia.runner.process;

import lombok.SneakyThrows;
import org.jetlinks.zlmedia.restful.ZLMediaConfigs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Collections;

class EmbeddedProcessMediaRuntimeTest {

    static EmbeddedProcessMediaRuntime runtime;

    @BeforeAll
    static void init() {
        ZLMediaConfigs configs = new ZLMediaConfigs();
        configs.getPorts().setSrt(9999);
        configs.getPorts().setRtsp(11554);
        configs.getPorts().setRtmp(11935);
        configs.getPorts().setRtc(8001);
        configs.getPorts().setSrt(19000);
        configs.setCommandArgs(new String[]{"-l", "4"});
        runtime = new EmbeddedProcessMediaRuntime("target/zlmedia", configs);
        runtime.start()
               .as(StepVerifier::create)
               .expectComplete()
               .verify();

    }

    @AfterAll
    static void shutdown() {
        runtime.dispose();
    }

    @SneakyThrows
    public static void main(String[] args) {
        ZLMediaConfigs configs = new ZLMediaConfigs();
        configs.getPorts().setSrt(9999);
        configs.getPorts().setRtsp(12554);
        configs.getPorts().setRtmp(12935);
        configs.getPorts().setRtc(18001);
        configs.getPorts().setHttp(8080);
        configs.getPorts().setSrt(19200);
        configs.setCommandArgs(new String[]{"-l", "0"});
        EmbeddedProcessMediaRuntime runtime =
            new EmbeddedProcessMediaRuntime("target/zlmedia", configs);


        runtime.start0();
        runtime.start0();

        System.in.read();
    }

    @Test
    @SneakyThrows
    void testIsAlive() {
        runtime
            .getOperations()
            .opsForState()
            .isAlive()
            .as(StepVerifier::create)
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @SneakyThrows
    void testGetConfigs() {

        runtime.getOperations()
               .opsForState()
               .getConfigs()
               .doOnNext(System.out::println)
               .as(StepVerifier::create)
               .expectNextCount(1)
               .verifyComplete();

    }

    @Test
    @SneakyThrows
    void testSetConfigs() {

        runtime
            .getOperations()
            .opsForState()
            .setConfigs(Collections.singletonMap("api.apiDebug", "1"))
            .doOnNext(System.out::println)
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();

    }

}