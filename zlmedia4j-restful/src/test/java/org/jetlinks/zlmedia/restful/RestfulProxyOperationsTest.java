package org.jetlinks.zlmedia.restful;

import org.jetlinks.zlmedia.proxy.FFMpegRequest;
import org.jetlinks.zlmedia.proxy.ProxyOperations;
import org.jetlinks.zlmedia.proxy.StreamProxyRequest;
import org.jetlinks.zlmedia.proxy.StreamPusherRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

class RestfulProxyOperationsTest {

    @Test
    void testAddStreamProxy() {
        ProxyOperations operations = RestfulTestHelper
            .create(server -> {
                server
                    .expect(requestTo("/index/api/addStreamProxy"))
                    .andExpect(jsonPath("$.retry_count").value(2))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"data\":{\"key\":\"test\"}}"));
                server
                    .expect(requestTo("/index/api/delStreamProxy"))
                    .andExpect(jsonPath("$.key").value("test"))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"data\":{\"flat\":true}}"));
            })
            .operations()
            .opsForProxy();
        assertNotNull(operations);

        StreamProxyRequest request = StreamProxyRequest
            .builder()
            .app("test")
            .stream("test-stream")
            .retryCount(2)
            .url("rtsp://127.0.0.1/test")
            .build();

        operations
            .addStreamProxy(request)
            .as(StepVerifier::create)
            .expectNext("test")
            .verifyComplete();

        operations
            .delStreamProxy("test")
            .as(StepVerifier::create)
            .expectComplete()
            .verify();

    }

    @Test
    void testAddStreamPusherProxy() {
        ProxyOperations operations = RestfulTestHelper
            .create(server -> {
                server
                    .expect(requestTo("/index/api/addStreamPusherProxy"))
                    .andExpect(jsonPath("$.retry_count").value(2))
                    .andExpect(jsonPath("$.dst_url").value("rtmp://127.0.0.1/test"))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"data\":{\"key\":\"test\"}}"));
                server
                    .expect(requestTo("/index/api/delStreamPusherProxy"))
                    .andExpect(jsonPath("$.key").value("test"))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"data\":{\"flat\":true}}"));
            })
            .operations()
            .opsForProxy();
        assertNotNull(operations);

        StreamPusherRequest request = StreamPusherRequest
            .builder()
            .app("test")
            .stream("test-stream")
            .retryCount(2)
            .dstUrl("rtmp://127.0.0.1/test")
            .build();

        operations
            .addStreamPusherProxy(request)
            .as(StepVerifier::create)
            .expectNext("test")
            .verifyComplete();

        operations
            .delStreamPusherProxy("test")
            .as(StepVerifier::create)
            .expectComplete()
            .verify();

    }

    @Test
    void testAddFFMpegSource() {
        ProxyOperations operations = RestfulTestHelper
            .create(server -> {
                server
                    .expect(requestTo("/index/api/addFFmpegSource"))
                    .andExpect(jsonPath("$.src_url").value("http://127.0.0.1/record/test.mp4"))
                    .andExpect(jsonPath("$.dst_url").value("rtmp://127.0.0.1/test"))
                    .andExpect(jsonPath("$.timeout_ms").value(10))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"data\":{\"key\":\"test\"}}"));
                server
                    .expect(requestTo("/index/api/delFFmpegSource"))
                    .andExpect(jsonPath("$.key").value("test"))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"data\":{\"flat\":true}}"));
            })
            .operations()
            .opsForProxy();
        assertNotNull(operations);

        FFMpegRequest request = FFMpegRequest
            .builder()
            .srcUrl("http://127.0.0.1/record/test.mp4")
            .dstUrl("rtmp://127.0.0.1/test")
            .timeoutMs(10)
            .build();

        operations
            .addFFMpegSource(request)
            .as(StepVerifier::create)
            .expectNext("test")
            .verifyComplete();

        operations
            .delFFMpegSource("test")
            .as(StepVerifier::create)
            .expectComplete()
            .verify();

    }


}