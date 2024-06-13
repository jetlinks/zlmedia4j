package org.jetlinks.zlmedia.restful.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.zlmedia.proxy.FFMpegRequest;
import org.jetlinks.zlmedia.restful.RestfulRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddFFmpegSource implements RestfulRequest<ProxyStreamResponse> {

    private FFMpegRequest request;

    @Override
    public Object params() {
        return request;
    }
}
