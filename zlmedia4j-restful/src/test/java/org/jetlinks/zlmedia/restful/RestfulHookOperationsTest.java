package org.jetlinks.zlmedia.restful;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetlinks.zlmedia.hook.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;

class RestfulHookOperationsTest {

    @Test
    void testParams() {
        doTest(Play.class, "test/on_play", "{\"params\":\"name=test\"}")
            .mapNotNull(Play::getParams)
            .mapNotNull(params -> params.getFirst("name"))
            .as(StepVerifier::create)
            .expectNext("test")
            .verifyComplete();
    }
    @Test
    void testHttpAccess() {
        doTest(HttpAccess.class, "on_http_access", "{\"ip\":\"127.0.0.1\",\"header.name\":\"test\"}")
            .doOnNext(System.out::println)
            .mapNotNull(HttpAccess::getHeaders)
            .mapNotNull(headers -> headers.getFirst("name"))
            .as(StepVerifier::create)
            .expectNext("test")
            .verifyComplete();
    }

    @Test
    void testFlowReport() {


        doTest(FlowReport.class, "on_flow_report", "{\"total_bytes\":1024}")
            .map(FlowReport::getTotalBytes)
            .as(StepVerifier::create)
            .expectNext(1024L)
            .verifyComplete();

        doTest(FlowReport.class, "on_flow_report", "{\"totalBytes\":1024}")
            .map(FlowReport::getTotalBytes)
            .as(StepVerifier::create)
            .expectNext(1024L)
            .verifyComplete();

    }

    protected <E extends HookEvent<R>, R extends HookResponse>
    Mono<E> doTest(Class<E> clazz, String type, String payload) {
        RestfulHookOperationsImpl operations = new RestfulHookOperationsImpl(
            RestfulZLMediaOperations.createObjectMapper(new ObjectMapper())
        );
        Sinks.One<E> sink = Sinks.one();

        operations.on(clazz, report -> {
            sink.tryEmitValue(report).orThrow();
            return Mono.empty();
        });
        return operations
            .fireEvent(type, payload)
            .then(sink.asMono())
            .timeout(Duration.ofSeconds(1));
    }
}