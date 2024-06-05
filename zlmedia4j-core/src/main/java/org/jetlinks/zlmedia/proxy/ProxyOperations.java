package org.jetlinks.zlmedia.proxy;

import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;

public interface ProxyOperations {

    Mono<String> addStreamProxy(StreamProxyRequest request);

    Mono<Void> delStreamProxy(String key);


    Mono<String> addStreamPusherProxy(String stream,
                                      URL destination,
                                      RtpType mode);

    Mono<Void> delStreamPusherProxy(String key);

    Mono<String> addFFMpegSource(URL source,
                                 URL destination,
                                 Duration timeout,
                                 Boolean enableHls,
                                 Boolean enableMp4,
                                 String ffmpegCmdKey);
}
