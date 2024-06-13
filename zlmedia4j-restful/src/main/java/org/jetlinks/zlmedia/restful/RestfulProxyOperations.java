package org.jetlinks.zlmedia.restful;

import lombok.AllArgsConstructor;
import org.jetlinks.zlmedia.proxy.*;
import org.jetlinks.zlmedia.restful.model.*;
import reactor.core.publisher.Mono;


@AllArgsConstructor
class RestfulProxyOperations implements ProxyOperations {
    private final RestfulClient client;

    @Override
    public Mono<String> addStreamProxy(StreamProxyRequest request) {
        request.validate();
        return client
            .request(new AddStreamProxy(request))
            .mapNotNull(resp -> resp.assertSuccess().getData())
            .map(ProxyStreamResponse::getKey);
    }

    @Override
    public Mono<Void> delStreamProxy(String key) {
        return client
            .request(new DelStreamProxy(key))
            .doOnNext(RestfulResponse::assertSuccess)
            .then();
    }

    @Override
    public Mono<String> addStreamPusherProxy(StreamPusherRequest request) {
        return client
            .request(new AddStreamPusherProxy(request))
            .mapNotNull(resp -> resp.assertSuccess().getData())
            .map(ProxyStreamResponse::getKey);
    }

    @Override
    public Mono<Void> delStreamPusherProxy(String key) {
        return client
            .request(new DelStreamPusherProxy(key))
            .doOnNext(RestfulResponse::assertSuccess)
            .then();
    }

    @Override
    public Mono<String> addFFMpegSource(FFMpegRequest request) {
        return client
            .request(new AddFFmpegSource(request))
            .mapNotNull(resp -> resp.assertSuccess().getData())
            .map(ProxyStreamResponse::getKey);
    }

    @Override
    public Mono<Void> delFFMpegSource(String key) {
        return client
            .request(new DelFFmpegSource(key))
            .doOnNext(RestfulResponse::assertSuccess)
            .then();
    }
}
