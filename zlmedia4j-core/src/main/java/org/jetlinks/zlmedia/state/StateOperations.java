package org.jetlinks.zlmedia.state;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface StateOperations {

    Mono<Boolean> isAlive();

    Mono<Map<String, String>> getConfigs();

    Mono<Map<String, String>> setConfigs(Map<String, String> configs);
}
