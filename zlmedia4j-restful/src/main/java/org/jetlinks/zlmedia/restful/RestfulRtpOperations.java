package org.jetlinks.zlmedia.restful;

import lombok.AllArgsConstructor;
import org.jetlinks.zlmedia.exception.ZLMediaException;
import org.jetlinks.zlmedia.restful.model.ListRtpServer;
import org.jetlinks.zlmedia.rtp.RtpInfo;
import org.jetlinks.zlmedia.rtp.RtpMode;
import org.jetlinks.zlmedia.rtp.RtpOperations;
import org.jetlinks.zlmedia.rtp.SendRtpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
class RestfulRtpOperations implements RtpOperations {

    final RestfulClient client;

    @Override
    public Mono<Integer> openRtpServer(String stream, RtpMode mode) {
        Map<String, Object> params = new HashMap<>();
        params.put("tcp_mode", mode.ordinal());
        params.put("stream_id", stream);
        return client
            .request("/index/api/openRtpServer", params, HashMap.class)
            .mapNotNull(map -> {
                if (Objects.equals(0, map.get("code"))) {
                    return (Integer) map.get("port");
                }
                throw new ZLMediaException("open rtp server failed:" + map.get("msg"));
            });
    }

    @Override
    public Mono<Void> closeRtpServer(String stream) {
        return client
            .request("/index/api/closeRtpServer",
                     Collections.singletonMap("stream_id", stream),
                     HashMap.class)
            .then();
    }

    @Override
    public Flux<RtpInfo> listRtpServer() {
        return client
            .request(new ListRtpServer())
            .flatMapIterable(resp -> resp
                .assertSuccess()
                .getDataOrElse(Collections.emptyList()));
    }

    @Override
    public Mono<Integer> startSendRtp(SendRtpRequest request) {
        return client
            .request("/index/api/startSendRtp", request, HashMap.class)
            .mapNotNull(map -> {
                if (Objects.equals(0, map.get("code"))) {
                    return (Integer) map.get("local_port");
                }
                throw new ZLMediaException("start send rtp failed:" + map.get("msg"));
            });
    }

    @Override
    public Mono<Void> stopSendRtp(String app, String stream) {
        return stopSendRtp(app, stream, null);
    }

    @Override
    public Mono<Void> stopSendRtp(String app, String stream, String ssrc) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("stream", stream);
        param.put("ssrc", ssrc);
        return client
            .request("/index/api/stopSendRtp",
                     param,
                     HashMap.class)
            .then();
    }
}
