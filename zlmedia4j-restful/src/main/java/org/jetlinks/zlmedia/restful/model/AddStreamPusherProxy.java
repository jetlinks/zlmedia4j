package org.jetlinks.zlmedia.restful.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.zlmedia.proxy.StreamPusherRequest;
import org.jetlinks.zlmedia.restful.RestfulRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddStreamPusherProxy implements RestfulRequest<ProxyStreamResponse> {
    private StreamPusherRequest request;

    @Override
    public Object params() {
        return request;
    }
}
