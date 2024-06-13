package org.jetlinks.zlmedia.restful;

import lombok.AllArgsConstructor;
import org.jetlinks.zlmedia.commons.MediaInfo;
import org.jetlinks.zlmedia.media.MediaOperations;
import org.jetlinks.zlmedia.media.MediaPlayInfo;
import org.jetlinks.zlmedia.restful.model.CloseStreams;
import org.jetlinks.zlmedia.restful.model.GetMediaList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@AllArgsConstructor
class RestfulMediaOperations implements MediaOperations {
    private final RestfulClient client;
    private final ZLMediaConfigs configs;

    @Override
    public Flux<MediaInfo> getMediaList() {
        return getMediaList(null, null);
    }

    @Override
    public Flux<MediaInfo> getMediaList(String app, String stream) {
        return client
            .request(new GetMediaList()
                         .withApp(app)
                         .withStream(stream))
            .flatMapIterable(response -> response
                .assertSuccess()
                .getDataOrElse(Collections.emptyList()));
    }

    @Override
    public Mono<Void> closeStreams(boolean force) {
        return this.closeStreams(null, force);
    }

    @Override
    public Mono<Void> closeStreams(String stream, boolean force) {
        return client
            .request(new CloseStreams()
                         .withStream(stream)
                         .witForce(force))
            .doOnNext(RestfulResponse::assertSuccess)
            .then();
    }

    @Override
    public Mono<MediaPlayInfo> getMediaPlayInfo(String app, String stream) {
        ZLMediaConfigs.Ports ports = configs.getPorts();
        MediaPlayInfo playInfo = new MediaPlayInfo();

        playInfo.setMp4("http://127.0.0.1:" + ports.getHttp() + "/" + app + "/" + stream + ".live.mp4");
        playInfo.setWebsocketMp4("ws://127.0.0.1:" + ports.getHttp() + "/" + app + "/" + stream + ".live.mp4");

        playInfo.setFlv("http://127.0.0.1:" + ports.getHttp() + "/" + app + "/" + stream + ".live.flv");
        playInfo.setHls("http://127.0.0.1:" + ports.getHttp() + "/" + app + "/" + stream + ".m3u8");
        playInfo.setHlsFmp4("http://127.0.0.1:" + ports.getHttp() + "/" + app + "/" + stream + ".fmp4.m3u8");

        playInfo.setTs("http://127.0.0.1:" + ports.getHttp() + "/" + app + "/" + stream + ".live.ts");
        playInfo.setWebsocketTs("ws://127.0.0.1:" + ports.getHttp() + "/" + app + "/" + stream + ".live.ts");

        playInfo.setRtsp("rtsp://127.0.0.1:" + ports.getRtsp() + "/" + app + "/" + stream);
        playInfo.setRtmp("rtmp://127.0.0.1:" + ports.getRtmp() + "/" + app + "/" + stream);

        playInfo.setRtc("rtc://127.0.0.1:" + ports.getRtc() + "/" + app + "/" + stream);


        return Mono.just(playInfo);
    }
}
