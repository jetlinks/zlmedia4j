package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowReport extends HookEvent<HookResponse> {

    private String schema;

    private long totalBytes;

}
