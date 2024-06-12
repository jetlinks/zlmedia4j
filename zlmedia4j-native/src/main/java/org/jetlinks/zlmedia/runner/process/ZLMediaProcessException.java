package org.jetlinks.zlmedia.runner.process;

import lombok.Getter;
import org.jetlinks.zlmedia.exception.ZLMediaException;

@Getter
public class ZLMediaProcessException extends ZLMediaException {
    private final int exitCode;

    public ZLMediaProcessException(int exitCode) {
        this(exitCode, "process exit code:" + exitCode);
    }

    public ZLMediaProcessException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public ZLMediaProcessException(int exitCode, String message, Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }
}
