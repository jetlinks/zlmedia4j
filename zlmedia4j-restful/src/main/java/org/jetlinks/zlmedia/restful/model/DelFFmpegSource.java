package org.jetlinks.zlmedia.restful.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.zlmedia.restful.RestfulRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DelFFmpegSource implements RestfulRequest<Void> {
    private String key;
}
