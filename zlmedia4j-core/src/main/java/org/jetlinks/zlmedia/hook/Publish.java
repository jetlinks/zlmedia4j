package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Publish extends HookEvent<HookResponse>{

    private String schema;

    private String stream;
}
