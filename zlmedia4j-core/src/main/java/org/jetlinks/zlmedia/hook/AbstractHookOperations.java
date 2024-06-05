package org.jetlinks.zlmedia.hook;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractHookOperations implements HookOperations {

    private final Map<Class<?>, List<HookListener<?, ?>>> listenerRepository = new ConcurrentHashMap<>();


    protected final <E extends HookEvent<R>, R extends HookResponse> Mono<R> fireEvent(E event) {
        @SuppressWarnings("all")
        List<HookListener<E, R>> listeners =
            (List) listenerRepository.get(event.getClass());

        if (listeners != null) {
            return Flux
                .fromIterable(listeners)
                .concatMap(listener -> listener.fire(event))
                .takeLast(1)
                .singleOrEmpty();
        }
        return Mono.empty();
    }

    @Override
    public final <E extends HookEvent<R>, R extends HookResponse> Disposable on(
        Class<E> type,
        HookListener<E, R> handler) {

        listenerRepository
            .computeIfAbsent(type, ignore -> new CopyOnWriteArrayList<>())
            .add(handler);
        onListen(type);
        return () -> {
            listenerRepository.compute(type, (ignore, list) -> {
                if (list == null) {
                    return null;
                }
                list.remove(handler);
                if (list.isEmpty()) {
                    return null;
                }
                return list;
            });
        };
    }

    protected void onListen(Class<? extends HookEvent<?>> type) {

    }


}
