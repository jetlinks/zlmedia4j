package org.jetlinks.zlmedia.commons;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, property = "codecType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = VideoTrack.class, name = "0"),
    @JsonSubTypes.Type(value = AudioTrack.class, name = "1")
})
public class MediaTrack {

    //Video = 0, Audio = 1
    private int codecType;

    // # H264 = 0, H265 = 1, AAC = 2, G711A = 3, G711U = 4
    private int codecId;

    private String codecIdName;


}
