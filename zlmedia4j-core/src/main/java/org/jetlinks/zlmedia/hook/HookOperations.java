package org.jetlinks.zlmedia.hook;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Hook相关操作接口
 *
 * @author zhouhao
 * @see HookOperations#on(Class, HookListener)
 * @see HookOperations#onFlowReport(HookListener)
 * @see HookOperations#onPlay(HookListener)
 * @since 1.0
 */
public interface HookOperations {

    /**
     * 注册一个Hook事件监听器
     *
     * @param type    事件类型
     * @param handler 事件处理器
     * @param <E>     事件类型
     * @param <R>     响应类型
     * @return Disposable
     */
    <E extends HookEvent<R>, R extends HookResponse> Disposable on(Class<E> type, HookListener<E, R> handler);


    default Disposable onFlowReport(HookListener<FlowReport, HookResponse> handler) {
        return on(FlowReport.class, handler);
    }

    default Disposable onHttpAccess(HookListener<HttpAccess, HookResponse> handler) {
        return on(HttpAccess.class, handler);
    }

    default Disposable onPlay(HookListener<Play, PlayResponse> handler) {
        return on(Play.class, handler);
    }

    default Disposable onPublish(HookListener<Publish, HookResponse> handler) {
        return on(Publish.class, handler);
    }

    default Disposable onRecordMp4(HookListener<RecordMp4, HookResponse> handler) {
        return on(RecordMp4.class, handler);
    }

    default Disposable onStreamChanged(HookListener<StreamChanged, HookResponse> handler) {
        return on(StreamChanged.class, handler);
    }

    default Disposable onStreamNoneReader(HookListener<StreamNoneReader, HookResponse> handler) {
        return on(StreamNoneReader.class, handler);
    }

}
