package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RtpServerTimeout extends HookEvent<HookResponse>{

    private String streamId;

    private int ssrc;

    private int tcpMode;
}
