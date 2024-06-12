package org.jetlinks.zlmedia.media;

import io.netty.buffer.ByteBuf;
import org.jetlinks.zlmedia.commons.MediaInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MediaOperations {

    Flux<MediaInfo> getMediaList();


    Flux<MediaInfo> getMediaList(String stream);


    Mono<Void> closeStreams(boolean force);


    Flux<Void> closeStreams(String stream, boolean force);


    Flux<ByteBuf> getMediaStream(String stream, MediaFormat format);
}
