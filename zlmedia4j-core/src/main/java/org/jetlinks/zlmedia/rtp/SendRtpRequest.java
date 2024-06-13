package org.jetlinks.zlmedia.rtp;

import lombok.*;

/**
 * @see <a href="https://github.com/zlmediakit/ZLMediaKit/wiki/MediaServer%E6%94%AF%E6%8C%81%E7%9A%84HTTP-API#27indexapistartsendrtp">Document</a>
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SendRtpRequest {
    /**
     * 应用名，例如 live
     */
    private String app;

    /**
     * 流id，例如 test
     */
    private String stream;

    /**
     * 推流的rtp的ssrc,指定不同的ssrc可以同时推流到多个服务器
     */
    private String ssrc;

    /**
     * rtp推流的目标地址
     */
    private String distUrl;

    /**
     * rtp推流的目标端口
     */
    private Integer distPort;

    /**
     * rtp推流模式
     */
    private Integer isUdp;

    /**
     * 使用的本机端口，为0或不传时默认为随机端口
     */
    private Integer srcPort;
    /**
     * 发送时，rtp的pt（uint8_t）,不传时默认为96
     */
    private Integer pt;
    /**
     * 发送时，rtp的负载类型。为1时，负载为ps；为0时，为es；不传时默认为1
     */
    private Integer usePs;
    /**
     * 当use_ps 为0时，有效。为1时，发送音频；为0时，发送视频；不传时默认为0
     */
    private Integer onlyAudio;
}
