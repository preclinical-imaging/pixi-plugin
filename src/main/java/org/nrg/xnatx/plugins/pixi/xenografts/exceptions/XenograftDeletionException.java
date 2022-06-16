package org.nrg.xnatx.plugins.pixi.xenografts.exceptions;

import org.nrg.xapi.exceptions.XapiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class XenograftDeletionException extends XapiException {

    public XenograftDeletionException() {
        super(HttpStatus.CONFLICT, "Subjects are referencing this xenograft. Cannot delete.");
    }

}