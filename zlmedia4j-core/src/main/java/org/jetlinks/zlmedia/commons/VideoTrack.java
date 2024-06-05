package org.jetlinks.zlmedia.commons;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoTrack extends MediaTrack {

    private int fps;

    private long frames;

    private long gopIntervalMs;

    private int gopSize;

    private long keyFrames;

    private int width;

    private int height;

    private boolean ready;
}
