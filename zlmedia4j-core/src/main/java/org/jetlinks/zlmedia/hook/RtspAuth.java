package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RtspAuth extends HookEvent<RtspAuth.Response> {

    private boolean mustNoEncrypt;

    private String realm;

    private String stream;


    @Getter
    @Setter
    public static class Response extends HookResponse {
        //用户密码是明文还是摘要
        private boolean encrypted;

        //用户密码明文或摘要(md5(username:realm:password))
        private String passwd;
    }


}
