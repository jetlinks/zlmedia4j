package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamNoneReader extends HookEvent<HookResponse> {

    private String stream;

}
