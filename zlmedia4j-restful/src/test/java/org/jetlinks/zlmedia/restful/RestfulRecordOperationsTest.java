package org.jetlinks.zlmedia.restful;

import org.jetlinks.zlmedia.proxy.ProxyOperations;
import org.jetlinks.zlmedia.proxy.StreamProxyRequest;
import org.jetlinks.zlmedia.record.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

class RestfulRecordOperationsTest {

    @Test
    void testStartRecord() {
        RecordOperations operations = RestfulTestHelper
            .create(server -> {
                server
                    .expect(requestTo("/index/api/startRecord"))
                    .andExpect(jsonPath("$.stream").value("test"))
                    .andExpect(jsonPath("$.type").value(1))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"result\":true}"));
                server
                    .expect(requestTo("/index/api/isRecording"))
                    .andExpect(jsonPath("$.stream").value("test"))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"status\":true}"));

                server
                    .expect(requestTo("/index/api/stopRecord"))
                    .andExpect(jsonPath("$.stream").value("test"))
                    .andExpect(jsonPath("$.type").value(1))
                    .andRespond(MockRestResponseCreators
                                    .withStatus(HttpStatus.OK)
                                    .body("{\"code\":0,\"result\":true}"));
            })
            .operations()
            .opsForRecord();
        assertNotNull(operations);

        operations
            .startRecord(StartRecordRequest
                             .builder()
                             .type(1)
                             .stream("test")
                             .build())
            .as(StepVerifier::create)
            .expectComplete()
            .verify();

        operations
            .isRecording("test", RecordType.mp4, "test")
            .as(StepVerifier::create)
            .expectNext(true)
            .verifyComplete();

        operations
            .stopRecord("test", RecordType.mp4, "test")
            .as(StepVerifier::create)
            .expectComplete()
            .verify();
    }

    @Test
    void testGetSnapshot() {
        RecordOperations operations = RestfulTestHelper
            .create(server -> server
                .expect(requestTo("/index/api/getSnap"))
                .andExpect(jsonPath("$.url").value("rtmp://127.0.0.1:8000/test/test"))
                .andRespond(MockRestResponseCreators
                                .withStatus(HttpStatus.OK)
                                .body("fake image")))
            .operations()
            .opsForRecord();
        assertNotNull(operations);

        operations
            .getSnapshot("rtmp://127.0.0.1:8000/test/test")
            .map(buf -> buf.toString(StandardCharsets.UTF_8))
            .as(StepVerifier::create)
            .expectNext("fake image")
            .verifyComplete();
    }

    @Test
    void testGetMp4RecordFile() {
        RecordOperations operations = RestfulTestHelper
            .create(server -> server
                .expect(requestTo("/index/api/getMp4RecordFile"))
                .andExpect(jsonPath("$.period").value("2020-02-01"))
                .andRespond(MockRestResponseCreators
                                .withStatus(HttpStatus.OK)
                                .body(new ClassPathResource("recordFiles.json"))))
            .operations()
            .opsForRecord();
        assertNotNull(operations);

        RecordFileInfo fileInfo = operations
            .getMp4RecordFile(GetMp4RecordRequest
                                  .builder()
                                  .app("record")
                                  .period("2020-02-01")
                                  .build())
            .blockLast();

        assertNotNull(fileInfo);
        System.out.println(fileInfo);
        assertNotNull(fileInfo.getRootPath());
        assertNotNull(fileInfo.getPaths());
    }

}