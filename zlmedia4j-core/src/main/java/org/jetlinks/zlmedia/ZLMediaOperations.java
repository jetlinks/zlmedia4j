package org.jetlinks.zlmedia;

import org.jetlinks.zlmedia.hook.HookOperations;
import org.jetlinks.zlmedia.media.MediaOperations;
import org.jetlinks.zlmedia.proxy.ProxyOperations;
import org.jetlinks.zlmedia.record.RecordOperations;
import org.jetlinks.zlmedia.rtp.RtpOperations;

public interface ZLMediaOperations {

    MediaOperations opsForMedia();

    RecordOperations opsForRecord();

    RtpOperations opsForRtp();

    ProxyOperations opsForProxy();

    HookOperations opsForHook();
}
