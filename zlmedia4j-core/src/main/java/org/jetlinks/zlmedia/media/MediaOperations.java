package org.jetlinks.zlmedia.media;

import org.jetlinks.zlmedia.commons.MediaInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * 流媒体相关操作接口
 *
 * @author zhouhao
 * @since 1.0
 */
public interface MediaOperations {

    /**
     * 获取全部流信息
     *
     * @return 流信息
     */
    Flux<MediaInfo> getMediaList();

    /**
     * 获取应用下的流信息
     *
     * @param app    应用标识,如: live,rtp ,为null时获取任意应用下的流信息
     * @param stream 流ID
     * @return 流信息
     */
    Flux<MediaInfo> getMediaList(@Nullable String app, String stream);

    /**
     * 关闭全部流信息
     *
     * @param force 强制关闭
     * @return void
     */
    Mono<Void> closeStreams(boolean force);

    /**
     * 关闭视频流
     *
     * @param stream 流ID
     * @param force  是否强制关闭
     * @return void
     */
    Mono<Void> closeStreams(String stream, boolean force);

    /**
     * 获取视频流的播放信息
     *
     * @param app    应用标识,如: live,rtp
     * @param stream 流ID
     * @return 播放信息
     */
    Mono<MediaPlayInfo> getMediaPlayInfo(String app, String stream);
}
