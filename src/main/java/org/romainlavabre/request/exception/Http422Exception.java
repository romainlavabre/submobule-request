package org.romainlavabre.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus( code = HttpStatus.UNPROCESSABLE_ENTITY )
public class Http422Exception extends RuntimeException {

    public Http422Exception( String message ) {
        super( message );
    }
}
