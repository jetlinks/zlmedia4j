package org.jetlinks.zlmedia.commons;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioTrack extends MediaTrack{

    //音频通道数
    private int channels;

    //轨道是否准备就绪
    private boolean ready;

    //累计接收帧数
    private long frames;

    //音频采样位数
    @JsonAlias("sample_bit")
    @JsonProperty
    private int sampleBit;

    //音频采样率
    private int sampleRate;
}
