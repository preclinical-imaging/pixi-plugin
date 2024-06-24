package org.nrg.xnatx.plugins.pixi.bli.factories;

import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifier;

public interface AnalyzedClickInfoObjectIdentifierFactory {

    AnalyzedClickInfoObjectIdentifier create(String name);
    AnalyzedClickInfoObjectIdentifier create(AnalyzedClickInfoObjectIdentifierMapping mapping);

}
