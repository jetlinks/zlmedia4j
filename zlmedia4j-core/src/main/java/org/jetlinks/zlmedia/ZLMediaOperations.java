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

    MediaOperations opsForMedia();

    RecordOperations opsForRecord();

    RtpOperations opsForRtp();

    ProxyOperations opsForProxy();

    HookOperations opsForHook();

    StateOperations opsForState();
}
