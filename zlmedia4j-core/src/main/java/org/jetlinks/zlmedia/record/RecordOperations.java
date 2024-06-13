package org.jetlinks.zlmedia.record;

import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 录像相关操作接口
 *
 * @author zhouhao
 * @since 1.0
 */
public interface RecordOperations {

    /**
     * 开始录像
     *
     * @param request 录像请求
     * @return void
     */
    Mono<Void> startRecord(StartRecordRequest request);

    /**
     * 判断是否正在录像
     *
     * @param app    应用ID {@link StartRecordRequest#getApp()}
     * @param type   录像类型 {@link StartRecordRequest#getType()}
     * @param stream 流ID {@link StartRecordRequest#getStream()}
     * @return void
     */
    Mono<Boolean> isRecording(String app, RecordType type, String stream);

    /**
     * 停止录像
     *
     * @param app    应用ID {@link StartRecordRequest#getApp()}
     * @param type   录像类型 {@link StartRecordRequest#getType()}
     * @param stream 流ID {@link StartRecordRequest#getStream()}
     * @return void
     */
    Mono<Void> stopRecord(String app, RecordType type, String stream);

    /**
     * 对指定的URL视频流进行截图
     *
     * @param url URL
     * @return 截图文件流信息
     */
    Flux<ByteBuf> getSnapshot(String url);

    /**
     * 获取mp4录像信息
     *
     * @param request 请求
     * @return 录像信息
     */
    Flux<RecordFileInfo> getMp4RecordFile(GetMp4RecordRequest request);

}
