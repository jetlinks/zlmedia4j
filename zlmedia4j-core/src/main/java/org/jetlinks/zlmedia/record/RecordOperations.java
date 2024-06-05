package org.jetlinks.zlmedia.record;

import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecordOperations {

    Mono<Void> startRecord(RecordType type,
                           String stream,
                           String path,
                           int maxSecond);

    Mono<Void> stopRecord(RecordType type, String stream);

    Mono<Void> isRecording(RecordType type, String stream);

    Flux<ByteBuf> getSnapshot(String stream);

    Flux<RecordFileInfo> getMp4RecordFile(String stream, String prefix, String path);

    enum RecordType {
        hls, mp4
    }
}
