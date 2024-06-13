package org.jetlinks.zlmedia.proxy;

import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;

/**
 * 视频流代理相关操作接口
 *
 * @author zhouhao
 * @since 1.0
 */
public interface ProxyOperations {

    /**
     * 动态添加rtsp/rtmp/hls/http-ts/http-flv拉流代理(只支持H264/H265/aac/G711/opus负载)
     * <p>
     * 可通过 {@link ProxyOperations#delStreamProxy}
     * 或者{@link org.jetlinks.zlmedia.media.MediaOperations#closeStreams(String, boolean)}停止代理
     *
     * @param request 请求
     * @return Key
     */
    Mono<String> addStreamProxy(StreamProxyRequest request);

    /**
     * 停止视频流代理
     *
     * @param key 通过{@link ProxyOperations#addStreamProxy(StreamProxyRequest)}返回的key
     * @return void
     */
    Mono<Void> delStreamProxy(String key);

    /**
     * 添加rtsp/rtmp主动推流(把本服务器的直播流推送到其他服务器去)
     *
     * @param request 请求
     * @return Key
     */
    Mono<String> addStreamPusherProxy(StreamPusherRequest request);

    /**
     * 停止视频流推送
     *
     * @param key 通过{@link ProxyOperations#addStreamPusherProxy(StreamPusherRequest)}返回的key
     * @return void
     */
    Mono<Void> delStreamPusherProxy(String key);

    /**
     * 添加一个FFMpeg拉流代理
     *
     * @param request 请求
     * @return Key
     */
    Mono<String> addFFMpegSource(FFMpegRequest request);

    /**
     * 停止FFMpeg拉流代理
     *
     * @param key 通过{@link ProxyOperations#addFFMpegSource(FFMpegRequest)}返回的key
     * @return void
     */
    Mono<Void> delFFMpegSource(String key);

}
