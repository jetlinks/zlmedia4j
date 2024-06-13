package org.jetlinks.zlmedia.restful;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.zlmedia.exception.ZLMediaException;

@Getter
@Setter
public class RestfulResponse<T> {
    private int code;
    private String msg;
    private T data;
    private Integer result;

    public T getDataOrElse(T orElse) {
        return data == null ? orElse : data;
    }

    public RestfulResponse<T> assertSuccess() {
        if (code != 0) {
            throw new ZLMediaException(msg);
        }
        return this;
    }
}
