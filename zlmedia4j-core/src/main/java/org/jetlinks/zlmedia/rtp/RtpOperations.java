package org.jetlinks.zlmedia.rtp;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RtpOperations {

    Mono<Integer> openRtpServer(String stream, RtpMode mode);

    Mono<Void> closeRtpServer(String stream);

    Flux<RtpInfo> listRtpServer();

    Mono<Integer> startSendRtp(SendRtpRequest request);

    /**
     * 停止ps-rtp推流
     *
     * @param stream 流id，例如 test
     * @return void
     */
    Mono<Void> stopSendRtp(String stream);

    /**
     * 停止ps-rtp推流
     *
     * @param stream 流id，例如 test
     * @param ssrc   根据ssrc关停某路rtp推流，置空时关闭所有流
     * @return void
     */
    Mono<Void> stopSendRtp(String stream, String ssrc);

}
