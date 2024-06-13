package org.jetlinks.zlmedia.restful;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetlinks.zlmedia.ZLMediaOperations;

import java.util.function.Consumer;

public class RestfulTestHelper {


    public MockReactiveRestServiceServer server;

    public ZLMediaConfigs configs;

    public static RestfulTestHelper create(Consumer<MockReactiveRestServiceServer> serverConsumer) {
        RestfulTestHelper helper = new RestfulTestHelper();
        helper.configs = new ZLMediaConfigs();
        helper.server = MockReactiveRestServiceServer.createServer();
        serverConsumer.accept(helper.server);
        return helper;
    }

    public ZLMediaOperations operations() {
        return new RestfulZLMediaOperations(server.createWebClient(), configs, new ObjectMapper());
    }

}
