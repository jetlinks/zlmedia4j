package org.jetlinks.zlmedia.restful;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import org.jetlinks.zlmedia.exception.ZLMediaException;
import org.jetlinks.zlmedia.record.*;
import org.jetlinks.zlmedia.restful.model.GetMp4RecordFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
class RestfulRecordOperations implements RecordOperations {
    private final RestfulClient client;

    @Override
    public Mono<Void> startRecord(StartRecordRequest request) {
        return client
            .request("/index/api/startRecord", request, Void.class)
            .then();
    }

    @Override
    public Mono<Void> stopRecord(String app, RecordType type, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("type", type.ordinal());
        param.put("stream", stream);
        return client
            .request("/index/api/stopRecord", param, Void.class)
            .then();
    }

    @Override
    public Mono<Boolean> isRecording(String app, RecordType type, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("type", type.ordinal());
        param.put("stream", stream);
        return client
            .request("/index/api/isRecording", param, HashMap.class)
            .mapNotNull(map -> Boolean.TRUE.equals(map.getOrDefault("status", false)));
    }

    @Override
    public Flux<ByteBuf> getSnapshot(String url) {
        Map<String, Object> param = new HashMap<>();
        param.put("url", url);
        param.put("timeout_sec", 20);
        param.put("expire_sec", 5);
        return client
            .http
            .post()
            .uri("/index/api/getSnap")
            .bodyValue(param)
            .exchangeToFlux(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToFlux(ByteBuf.class);
                }
                return response
                    .bodyToMono(String.class)
                    .<ByteBuf>flatMap(s -> Mono.error(new ZLMediaException(s)))
                    .flux();
            });
    }

    @Override
    public Flux<RecordFileInfo> getMp4RecordFile(GetMp4RecordRequest request) {
        return client
            .request(new GetMp4RecordFile(request))
            .mapNotNull(response -> response.assertSuccess().getData())
            .flux();
    }
}
