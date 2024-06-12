package org.jetlinks.zlmedia.exception;

public class ZLMediaException extends RuntimeException {

    public ZLMediaException(String message) {
        super(message);
    }

    public ZLMediaException(String message, Throwable cause) {
        super(message, cause);
    }
}
