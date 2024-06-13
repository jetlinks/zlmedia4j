package org.jetlinks.zlmedia.commons;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class MediaInfo {

    private String app;

    private String vhost;

    private int readerCount;

    private int totalReaderCount;

    private String stream;

    //产生源类型，包括 unknown = 0,rtmp_push=1,rtsp_push=2,rtp_push=3,pull=4,ffmpeg_pull=5,mp4_vod=6,device_chn=7
    private int originType;

    private String originUrl;

    private long createStamp;

    private int aliveSecond;

    private long bytesSpeed;

    private List<MediaTrack> tracks;

}
