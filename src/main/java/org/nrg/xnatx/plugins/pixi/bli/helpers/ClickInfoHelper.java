package org.nrg.xnatx.plugins.pixi.bli.helpers;

import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;
import org.nrg.xnatx.plugins.pixi.bli.models.ClickInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface ClickInfoHelper {

    ClickInfo parseTxt(Path txtFile) throws IOException;
    ClickInfo parseTxt(InputStream inputStream, Path outputPath, Path jsonOutputPath) throws IOException;

}
