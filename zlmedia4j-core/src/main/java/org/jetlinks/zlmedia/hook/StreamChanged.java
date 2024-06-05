package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.zlmedia.commons.MediaInfo;

@Getter
@Setter
public class StreamChanged extends HookEvent<HookResponse> {

    //是否注册
    private boolean regist;

    private String schema;

    //流ID
    private String stream;

    //regist为true时有值
    private MediaInfo media;

}
