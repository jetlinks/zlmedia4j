package org.jetlinks.zlmedia.commons;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaTrack {

    //Video = 0, Audio = 1
    private int codecType;

    // # H264 = 0, H265 = 1, AAC = 2, G711A = 3, G711U = 4
    private int codecId;

    private String codecIdName;


}
