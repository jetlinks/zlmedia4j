package org.jetlinks.zlmedia.restful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.jetlinks.zlmedia.hook.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class RestfulHookOperationsImpl extends AbstractHookOperations implements RestfulHookOperations {

    private final Map<String, Class<? extends HookEvent<?>>> pathMapping = new ConcurrentHashMap<>();

    protected final ObjectMapper mapper;

    RestfulHookOperationsImpl(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    // Play.class => on_play
    // HttpAccess.class => on_http_access
    @Override
    protected void onListen(Class<? extends HookEvent<?>> type) {
        String name = type.getSimpleName();
        StringBuilder builder = new StringBuilder("on_");
        int len = name.length();
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i != 0) {
                    builder.append('_');
                }
                c = Character.toLowerCase(c);
            }
            builder.append(c);
        }
        pathMapping.put(builder.toString(), type);
        pathMapping.put(name, type);
        pathMapping.put("on" + name, type);
    }


    @Override
    public Mono<HookResponse> fireEvent(String type, String payload) {
        // http path?
        if (type.contains("/")) {
            type = type.substring(type.lastIndexOf('/') + 1);
        }
        Class<? extends HookEvent<?>> clazz = pathMapping.get(type);
        if (clazz == null) {
            return Mono.empty();
        }
        try {
            return fireEvent(mapper.readerFor(clazz).readValue(payload));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }


}
