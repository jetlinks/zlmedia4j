package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamNotFound extends HookEvent<HookResponse>{
     private String stream;
}
