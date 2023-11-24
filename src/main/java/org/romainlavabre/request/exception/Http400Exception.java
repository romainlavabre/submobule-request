package org.romainlavabre.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus( code = HttpStatus.BAD_REQUEST )
public class Http400Exception extends RuntimeException {

    public Http400Exception( String message ) {
        super( message );
    }
}
