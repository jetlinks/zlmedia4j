package org.jetlinks.zlmedia.restful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jetlinks.zlmedia.hook.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RestfulHookOperations extends AbstractHookOperations {

    private final Map<String, Class<? extends HookEvent<?>>> pathMapping = new ConcurrentHashMap<>();

    protected final ObjectMapper mapper;

    public RestfulHookOperations(ObjectMapper mapper) {
        this.mapper = mapper.copy();
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                                                                 BeanDescription beanDesc,
                                                                 List<BeanPropertyDefinition> propDefs) {
                List<BeanPropertyDefinition> def = new ArrayList<>(propDefs);
                for (BeanPropertyDefinition propDef : propDefs) {
                    //同时支持蛇形命名
                    String newName = PropertyNamingStrategies.SNAKE_CASE.nameForField(config, null, propDef.getName());
                    if (!newName.equals(propDef.getName())) {
                        def.add(propDef.withName(new PropertyName(newName)));
                    }
                }
                return def;
            }
        });

        this.mapper
            .setConfig(
                this.mapper
                    .getDeserializationConfig()
                    .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                    .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            )
            .registerModule(module);
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


    public Mono<HookResponse> fireEvent(String type, String payload) {
        // http path?
        if (type.contains("/")) {
            type = type.substring(type.lastIndexOf('/'), type.length() - 1);
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
