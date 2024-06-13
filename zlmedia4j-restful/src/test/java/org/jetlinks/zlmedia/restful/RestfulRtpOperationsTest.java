package org.jetlinks.zlmedia.restful;

import org.jetlinks.zlmedia.proxy.ProxyOperations;
import org.jetlinks.zlmedia.proxy.StreamProxyRequest;
import org.jetlinks.zlmedia.rtp.RtpInfo;
import org.jetlinks.zlmedia.rtp.RtpMode;
import org.jetlinks.zlmedia.rtp.RtpOperations;
import org.jetlinks.zlmedia.rtp.SendRtpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

class RestfulRtpOperationsTest {

    @Test
    void testOpenRtpServer() {
        RtpOperations operations = RestfulTestHelper
            .create(server -> {
                server
                    .expect(requestTo("/index/api/openRtpServer"))
                    .andExpect(jsonPath("$.stream_id").value("test"))
                    .andExpect(jsonPath("$.tcp_mode").value(0))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"port\":1234}"));
                server
                    .expect(requestTo("/index/api/listRtpServer"))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"data\":[{\"stream_id\":\"test\",\"port\":1234}]}"));

                server
                    .expect(requestTo("/index/api/closeRtpServer"))
                    .andExpect(jsonPath("$.stream_id").value("test"))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"hit\":1}"));
            })
            .operations()
            .opsForRtp();
        assertNotNull(operations);

        operations.openRtpServer("test", RtpMode.udp)
                  .as(StepVerifier::create)
                  .expectNext(1234)
                  .verifyComplete();

        RtpInfo rtpInfo = operations.listRtpServer().blockLast();
        assertNotNull(rtpInfo);
        assertEquals("test", rtpInfo.getStreamId());
        assertEquals(1234, rtpInfo.getPort());

        operations
            .closeRtpServer("test")
            .as(StepVerifier::create)
            .expectComplete()
            .verify();
    }

    @Test
    void testStartSendRtp() {
        RtpOperations operations = RestfulTestHelper
            .create(server -> {
                server
                    .expect(requestTo("/index/api/startSendRtp"))
                    .andExpect(jsonPath("$.dist_url").value("127.0.0.1"))
                    .andExpect(jsonPath("$.dist_port").value(7000))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"local_port\":1234}"));

                server
                    .expect(requestTo("/index/api/stopSendRtp"))
                    .andExpect(jsonPath("$.stream").value("test-stream"))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0}"));
            })
            .operations()
            .opsForRtp();
        assertNotNull(operations);

        operations
            .startSendRtp(
                SendRtpRequest
                    .builder()
                    .app("test")
                    .stream("test-stream")
                    .ssrc("0FFBEBEFD")
                    .distUrl("127.0.0.1")
                    .distPort(7000)
                    .isUdp(1)
                    .build())
            .as(StepVerifier::create)
            .expectNext(1234)
            .verifyComplete();

        operations
            .stopSendRtp("test", "test-stream")
            .as(StepVerifier::create)
            .expectComplete()
            .verify();
    }


}