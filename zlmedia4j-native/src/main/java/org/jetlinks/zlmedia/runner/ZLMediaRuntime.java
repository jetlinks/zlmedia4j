package org.jetlinks.zlmedia.runner;

import org.jetlinks.zlmedia.ZLMediaOperations;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ZLMediaRuntime extends Disposable {

    /**
     * 启动
     *
     * @return 启动
     * @see org.jetlinks.zlmedia.exception.ZLMediaException
     * @see org.jetlinks.zlmedia.runner.process.ZLMediaProcessException
     */
    Mono<Void> start();

    /**
     * 获取运行时的输出日志
     *
     * @return 输出日志
     */
    Flux<String> output();

    /**
     * 获取 ZLMediaOperations 操作接口
     *
     * @return ZLMediaOperations
     */
    ZLMediaOperations getOperations();

}
