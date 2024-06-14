package org.jetlinks.zlmedia;

import org.jetlinks.zlmedia.hook.HookOperations;
import org.jetlinks.zlmedia.state.StateOperations;
import org.jetlinks.zlmedia.media.MediaOperations;
import org.jetlinks.zlmedia.proxy.ProxyOperations;
import org.jetlinks.zlmedia.record.RecordOperations;
import org.jetlinks.zlmedia.rtp.RtpOperations;

/**
 * 针对ZLMedia流媒体服务操作的封装
 *
 * @author zhouhao
 * @see MediaOperations
 * @see RecordOperations
 * @see RtpOperations
 * @see ProxyOperations
 * @see HookOperations
 * @since 1.0
 */
public interface ZLMediaOperations {

    /**
     * 获取媒体操作接口
     *
     * @return 媒体操作接口
     */
    MediaOperations opsForMedia();

    /**
     * 获取录像操作接口
     *
     * @return 录像操作接口
     */
    RecordOperations opsForRecord();

    /**
     * 获取RTP操作接口
     *
     * @return RTP操作接口
     */
    RtpOperations opsForRtp();

    /**
     * 获取代理操作接口
     *
     * @return 代理操作接口
     */
    ProxyOperations opsForProxy();

    /**
     * 获取钩子操作接口
     *
     * @return 钩子操作接口
     */
    HookOperations opsForHook();

    /**
     * 获取状态操作接口
     *
     * @return 状态操作接口
     */
    StateOperations opsForState();
}
