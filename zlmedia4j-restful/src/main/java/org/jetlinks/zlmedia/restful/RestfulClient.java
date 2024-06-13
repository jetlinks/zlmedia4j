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

    final ObjectMapper mapper;
    final WebClient http;

    RestfulClient(WebClient client,
                  ObjectMapper mapper) {
        this.mapper = mapper;
        this.http = client;
    }

    @SneakyThrows
    <R> Mono<RestfulResponse<R>> request(RestfulRequest<R> request) {
        Object body = request.params();
        if (body != null && !(body instanceof String)) {
            body = mapper.writeValueAsString(body);
        }
        return http
            .post()
            .uri(request.apiAddress())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body == null ? "{}" : body)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response
                        .bodyToMono(String.class)
                        .map(json -> decode(json, request));
                }
                return response
                    .bodyToMono(String.class)
                    .flatMap(resp -> Mono.error(new ZLMediaException(resp)));
            });
    }

    @SneakyThrows
    <R> Mono<R> request(String api, Object request, Class<R> responseType) {
        Object body = request;
        if (body != null && !(body instanceof String)) {
            body = mapper.writeValueAsString(body);
        }
        return http
            .post()
            .uri(api)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body == null ? "{}" : body)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    if (Void.class == responseType) {
                        return response
                            .releaseBody()
                            .then(Mono.empty());
                    }
                    return response
                        .bodyToMono(String.class)
                        .map(json -> decode(json, responseType));
                }
                return response
                    .bodyToMono(String.class)
                    .flatMap(resp -> Mono.error(new ZLMediaException(resp)));
            });
    }

    @SneakyThrows
    <R> R decode(String json, Class<R> responseType) {
        return mapper.readValue(json, responseType);
    }

    @SneakyThrows
    <R> RestfulResponse<R> decode(String json, RestfulRequest<R> request) {
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
