package org.nrg.xnatx.plugins.pixi.hotelsplitter.exceptions;

import org.nrg.xapi.exceptions.XapiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HotelReferenceException extends XapiException {
    public HotelReferenceException() {
        super(HttpStatus.CONFLICT, "A hotel scan record is referencing this hotel. Cannot delete.");
    }
}
