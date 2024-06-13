package org.jetlinks.zlmedia.proxy;

import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StreamPusherRequest {

    private String app;

    private String stream;

    private String dstUrl;

    private Integer retryCount;

    private Integer rtpType;

    private Integer timeoutSec;

}
