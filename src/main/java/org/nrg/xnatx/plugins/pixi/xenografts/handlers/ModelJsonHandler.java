package org.nrg.xnatx.plugins.pixi.xenografts.handlers;

import org.nrg.xnatx.plugins.pixi.xenografts.models.Xenograft;

public interface ModelJsonHandler {
    Xenograft process(Xenograft X);
}
