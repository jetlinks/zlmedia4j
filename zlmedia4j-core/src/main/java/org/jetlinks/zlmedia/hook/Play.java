package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Play extends HookEvent<PlayResponse>{

    private String schema;

    private String stream;

}
