package org.jetlinks.zlmedia.hook;

import reactor.core.publisher.Mono;

public interface HookListener<E extends HookEvent<R>, R extends HookResponse> {

    Mono<R> fire(E event);

}
