package org.jetlinks.zlmedia.restful.model;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.zlmedia.restful.RestfulRequest;

@Getter
@Setter
public class CloseStreams implements RestfulRequest<Void> {

    private String app;

    private String stream;

    private Integer force;

    @Override
    public String apiAddress() {
        return "/index/api/close_streams";
    }

    public CloseStreams witForce(boolean force) {
        this.force = force ? 1 : 0;
        return this;
    }

    public CloseStreams withApp(String app) {
        this.app = app;
        return this;
    }

    public CloseStreams withStream(String stream) {
        this.stream = stream;
        return this;
    }

}
