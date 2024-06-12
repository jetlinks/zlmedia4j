package org.jetlinks.zlmedia.restful.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetlinks.zlmedia.proxy.StreamProxyRequest;
import org.jetlinks.zlmedia.restful.RestfulRequest;

@AllArgsConstructor
@Getter
public class AddStreamProxy implements RestfulRequest<AddStreamProxy.Response> {
    private StreamProxyRequest request;

    @Override
    public Object params() {
        
        return request;
    }

    @Getter
    @Setter
    public static class Response {
        private String key;

    }
}
