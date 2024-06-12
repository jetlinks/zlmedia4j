package org.jetlinks.zlmedia.restful;

import org.jetlinks.zlmedia.hook.HookOperations;
import org.jetlinks.zlmedia.hook.HookResponse;
import reactor.core.publisher.Mono;

public interface RestfulHookOperations extends HookOperations {

    Mono<HookResponse> fireEvent(String type, String payload);

}
