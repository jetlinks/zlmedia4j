package org.jetlinks.zlmedia.restful;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jetlinks.zlmedia.exception.ZLMediaException;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

class RestfulClient {

    private final ObjectMapper mapper;
    private final WebClient client;

    public RestfulClient(WebClient client,
                         ObjectMapper mapper) {
        this.mapper = mapper;
        this.client = client;
    }


    @SneakyThrows
    <R> Mono<RestfulResponse<R>> request(RestfulRequest<R> request) {
        Object body = request.params();
        if (body != null && !(body instanceof String)) {
            body = mapper.writeValueAsString(body);
        }
        return client
            .post()
            .uri(request.apiAddress())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body == null ? "{}" : body)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    if (MediaType.APPLICATION_JSON.includes(response.headers().asHttpHeaders().getContentType())) {
                        return response
                            .bodyToMono(String.class)
                            .map(json -> decode(json, request));
                    }
                }
                return response
                    .bodyToMono(String.class)
                    .flatMap(json -> Mono.error(new ZLMediaException(json)));
            });
    }

    @SneakyThrows
    private <R> RestfulResponse<R> decode(String json, RestfulRequest<R> request) {
        return mapper.readValue(
            json,
            new TypeReference<RestfulResponse<R>>() {
                @Override
                public Type getType() {
                    return ResolvableType
                        .forClassWithGenerics(RestfulResponse.class, request.responseType())
                        .getType();
                }
            }
        );
    }
}
