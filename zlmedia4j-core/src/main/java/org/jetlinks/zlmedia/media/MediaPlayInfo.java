package org.jetlinks.zlmedia.media;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MediaPlayInfo {

    private String rtsp;
    private String rtmp;

    private String flv;

    private String mp4;
    private String websocketMp4;

    private String hls;
    private String hlsFmp4;

    private String ts;
    private String websocketTs;

    private String rtc;

}
