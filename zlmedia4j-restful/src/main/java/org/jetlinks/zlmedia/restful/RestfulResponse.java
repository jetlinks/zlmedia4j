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


    public void assertSuccess() {
        if (code != 0) {
            throw new ZLMediaException(msg);
        }
    }
}
