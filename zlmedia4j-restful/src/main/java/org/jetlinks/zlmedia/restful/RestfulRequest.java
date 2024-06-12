package org.jetlinks.zlmedia.restful;

import org.springframework.core.ResolvableType;

public interface RestfulRequest<Response> {


    default Object params() {
        return this;
    }

    default ResolvableType responseType() {
        return ResolvableType
            .forClass(RestfulRequest.class, this.getClass())
            .getGeneric(0);
    }

    default String apiAddress() {
        String name = this.getClass().getSimpleName();
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return "/index/api/" + new String(chars);
    }

}
