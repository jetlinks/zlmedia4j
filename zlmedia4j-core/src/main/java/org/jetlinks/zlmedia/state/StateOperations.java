package org.jetlinks.zlmedia.state;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * ZLMedia运行状态操作接口
 *
 * @author zhouhao
 * @since 1.0
 */
public interface StateOperations {

    /**
     * @return 运行是否正常
     */
    Mono<Boolean> isAlive();

    /**
     * 获取配置信息
     *
     * @return 配置信息
     */
    Mono<Map<String, String>> getConfigs();

    /**
     * 设置配置信息,请谨慎修改<code>api.secret</code>等配置信息,否则可能导致无法访问.
     *
     * @param configs 配置信息
     */
    Mono<Map<String, String>> setConfigs(Map<String, String> configs);
}
