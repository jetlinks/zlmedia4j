package org.jetlinks.zlmedia.hook;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface HookOperations {


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
