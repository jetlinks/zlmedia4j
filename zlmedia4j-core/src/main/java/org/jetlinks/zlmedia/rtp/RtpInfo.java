package org.jetlinks.zlmedia.rtp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RtpInfo {

    private String streamId;

    private int port;
}
