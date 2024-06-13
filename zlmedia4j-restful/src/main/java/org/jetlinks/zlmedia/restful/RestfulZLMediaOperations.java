package org.jetlinks.zlmedia.restful;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.jetlinks.zlmedia.ZLMediaOperations;
import org.jetlinks.zlmedia.hook.HookOperations;
import org.jetlinks.zlmedia.restful.model.GetServerConfig;
import org.jetlinks.zlmedia.restful.model.SetServerConfig;
import org.jetlinks.zlmedia.state.StateOperations;
import org.jetlinks.zlmedia.media.MediaOperations;
import org.jetlinks.zlmedia.proxy.ProxyOperations;
import org.jetlinks.zlmedia.record.RecordOperations;
import org.jetlinks.zlmedia.rtp.RtpOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestfulZLMediaOperations implements ZLMediaOperations,
    StateOperations {

    private final RestfulClient client;

    private final HookOperations hookOperations;
    private final MediaOperations mediaOperations;
    private final RecordOperations recordOperations;
    private final RtpOperations rtpOperations;
    private final ProxyOperations proxyOperations;


    public RestfulZLMediaOperations(WebClient client,
                                    ZLMediaConfigs configs,
                                    ObjectMapper mapper) {

        this.client = new RestfulClient(client, createObjectMapper(mapper));
        this.hookOperations = new RestfulHookOperationsImpl(this.client.mapper);
        this.mediaOperations = new RestfulMediaOperations(this.client, configs);
        this.recordOperations = new RestfulRecordOperations(this.client);
        this.rtpOperations = new RestfulRtpOperations(this.client);
        this.proxyOperations = new RestfulProxyOperations(this.client);
    }

    static NameTransformer SNAKE_CASE = new NameTransformer() {
        @Override
        public String transform(String name) {
            return PropertyNamingStrategies.SNAKE_CASE.nameForField(null, null, name);
        }

        @Override
        public String reverse(String transformed) {
            return transformed;
        }
    };

    static ObjectMapper createObjectMapper(ObjectMapper mapper) {
        mapper = mapper.copy();
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription beanDesc,
                                                             List<BeanPropertyWriter> beanProperties) {
                List<BeanPropertyWriter> propertyWriters = new ArrayList<>(beanProperties.size());
                for (BeanPropertyWriter beanProperty : beanProperties) {
                    propertyWriters.add(beanProperty.rename(SNAKE_CASE));
                }
                return propertyWriters;
            }
        });

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

        return mapper
            .setConfig(
                mapper.getSerializationConfig()
                      .without(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            )
            .setConfig(
                mapper
                    .getDeserializationConfig()
                    .with(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                    .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            )
            .registerModule(module);
    }

    @Override
    public MediaOperations opsForMedia() {
        return mediaOperations;
    }

    @Override
    public RecordOperations opsForRecord() {
        return recordOperations;
    }

    @Override
    public RtpOperations opsForRtp() {
        return rtpOperations;
    }

    @Override
    public ProxyOperations opsForProxy() {
        return proxyOperations;
    }

    @Override
    public HookOperations opsForHook() {
        return hookOperations;
    }

    @Override
    public StateOperations opsForState() {
        return this;
    }

    @Override
    public Mono<Boolean> isAlive() {
        return getConfigs()
            .hasElement()
            .onErrorResume(err -> Mono.just(false));
    }

    @Override
    public Mono<Map<String, String>> getConfigs() {
        return client
            .request(new GetServerConfig())
            .filter(res -> res.getCode() == 0 && !CollectionUtils.isEmpty(res.getData()))
            .map(res -> res.getData().get(0));
    }

    @Override
    public Mono<Map<String, String>> setConfigs(Map<String, String> configs) {
        return client
            .request(new SetServerConfig(configs))
            .map(res -> {
                res.assertSuccess();
                return configs;
            });
    }
}
