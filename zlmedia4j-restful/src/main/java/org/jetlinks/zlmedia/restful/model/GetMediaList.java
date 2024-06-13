package org.jetlinks.zlmedia.restful.model;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.zlmedia.commons.MediaInfo;
import org.jetlinks.zlmedia.restful.RestfulRequest;

import java.util.List;

@Getter
@Setter
public class GetMediaList implements RestfulRequest<List<MediaInfo>> {

    private String app;

    private String stream;

    public GetMediaList withApp(String app) {
        this.app = app;
        return this;
    }

    public GetMediaList withStream(String stream) {
        this.stream = stream;
        return this;
    }
}
