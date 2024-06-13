package org.jetlinks.zlmedia.restful.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetlinks.zlmedia.proxy.StreamProxyRequest;
import org.jetlinks.zlmedia.restful.RestfulRequest;

@AllArgsConstructor
@Getter
public class AddStreamProxy implements RestfulRequest<ProxyStreamResponse> {
    private StreamProxyRequest request;

    @Override
    public Object params() {
        return request;
    }
}
