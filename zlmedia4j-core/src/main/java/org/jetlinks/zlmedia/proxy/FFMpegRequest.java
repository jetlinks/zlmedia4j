package org.jetlinks.zlmedia.proxy;

import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FFMpegRequest {
    /**
     * FFmpeg拉流地址,支持任意协议或格式(只要FFmpeg支持即可)
     */
    private String srcUrl;

    /**
     * FFmpeg rtmp推流地址，一般都是推给自己，例如.
     * rtmp://127.0.0.1/live/stream_form_ffmpeg
     */
    private String dstUrl;

    /**
     * FFmpeg推流成功超时时间
     */
    private Integer timeoutMs;

    /**
     * 是否开启hls录制
     */
    private Boolean enableHls;

    /**
     * 是否开启mp4录制
     */
    private Boolean enableMp4;

    /**
     * 配置文件中FFmpeg命令参数模板key(非内容)，置空则采用默认模板:ffmpeg.cmd
     */
    private String ffmpegCmdKey;
}
