package org.nrg.xnatx.plugins.pixi.hotelsplitter.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PixiRuntimeException extends RuntimeException {

    public PixiRuntimeException(final String message) {
        super(message);
    }

    public PixiRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
