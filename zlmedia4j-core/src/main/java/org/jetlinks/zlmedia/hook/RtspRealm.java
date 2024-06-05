package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RtspRealm extends HookEvent<RtspRealm.Response> {

    private String stream;


    @Getter
    @Setter
    public static class Response extends HookResponse {
        private String realm;
    }
}
