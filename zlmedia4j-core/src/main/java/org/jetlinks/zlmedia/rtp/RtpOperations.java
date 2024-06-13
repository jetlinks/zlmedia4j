package org.jetlinks.zlmedia.rtp;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * RTP相关操作接口
 *
 * @author zhouhao
 * @since 1.0
 */
public interface RtpOperations {

    /**
     * 开启一个RTP服务,用于接收RTP流
     *
     * @param stream 流ID
     * @param mode   模式
     * @return 端口
     */
    Mono<Integer> openRtpServer(String stream, RtpMode mode);

    /**
     * 关闭RTP服务
     *
     * @param stream 流ID
     * @return void
     */
    Mono<Void> closeRtpServer(String stream);

    /**
     * 获取RTP服务列表
     *
     * @return RTP服务列表
     */
    Flux<RtpInfo> listRtpServer();

    /**
     * 以RTP方式推送流到指定的RTP服务
     *
     * @param request 请求
     * @return 端口
     */
    Mono<Integer> startSendRtp(SendRtpRequest request);

    /**
     * 停止rtp推流
     *
     * @param stream 流id，例如 test
     * @return void
     */
    Mono<Void> stopSendRtp(String app, String stream);

    /**
     * 停止ps-rtp推流
     *
     * @param stream 流id，例如 test
     * @param ssrc   根据ssrc关停某路rtp推流，置空时关闭所有流
     * @return void
     */
    Mono<Void> stopSendRtp(String app, String stream, String ssrc);

}
