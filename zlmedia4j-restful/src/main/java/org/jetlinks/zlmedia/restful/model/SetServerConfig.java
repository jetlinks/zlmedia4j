package org.jetlinks.zlmedia.restful.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetlinks.zlmedia.restful.RestfulRequest;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
public class SetServerConfig implements RestfulRequest<Void> {

    private Map<String, String> configs;

    @Override
    public Object params() {
        return configs;
    }
}
