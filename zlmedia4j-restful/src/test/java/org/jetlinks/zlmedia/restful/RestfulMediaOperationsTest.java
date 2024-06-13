package org.jetlinks.zlmedia.restful;

import org.jetlinks.zlmedia.commons.AudioTrack;
import org.jetlinks.zlmedia.commons.MediaInfo;
import org.jetlinks.zlmedia.commons.VideoTrack;
import org.jetlinks.zlmedia.media.MediaOperations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;

class RestfulMediaOperationsTest {


    @Test
    void testGetMediaList() {
        MediaOperations operations = RestfulTestHelper
            .create(server -> server
                .expect(requestTo("/index/api/getMediaList"))
                .andRespond(MockRestResponseCreators
                                .withStatus(HttpStatus.OK)
                                .body(new ClassPathResource("getMediaList.json"))))
            .operations()
            .opsForMedia();
        assertNotNull(operations);

        MediaInfo info = operations.getMediaList().blockLast();
        assertNotNull(info);
        System.out.println(info);

        assertNotNull(info.getApp());
        assertNotNull(info.getStream());

        assertNotNull(info.getTracks());
        assertEquals(2, info.getTracks().size());
        assertInstanceOf(AudioTrack.class, info.getTracks().get(0));
        assertInstanceOf(VideoTrack.class, info.getTracks().get(1));

    }

    @Test
    void testCloseStreams() {
        MediaOperations operations = RestfulTestHelper
            .create(server -> server
                .expect(requestTo("/index/api/close_streams"))
                .andExpect(jsonPath("$.stream").value("test"))
                .andExpect(jsonPath("$.force").value(1))
                .andRespond(MockRestResponseCreators
                                .withStatus(HttpStatus.OK)
                                .body("{\"code\":0,\"count_hit\" : 1,\"count_closed\" : 1}"))
            )
            .operations()
            .opsForMedia();
        assertNotNull(operations);

        operations.closeStreams("test", true)
                  .as(StepVerifier::create)
                  .expectComplete()
                  .verify();

    }

    @Test
    void testGetMediaPlayInfo() {
        MediaOperations operations = RestfulTestHelper
            .create(server -> {
            })
            .operations()
            .opsForMedia();
        assertNotNull(operations);

        operations
            .getMediaPlayInfo("test", "test-stream")
            .doOnNext(System.out::println)
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();

    }
}