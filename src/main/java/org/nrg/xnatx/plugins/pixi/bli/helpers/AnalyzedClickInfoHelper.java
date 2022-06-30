package org.nrg.xnatx.plugins.pixi.bli.helpers;

import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface AnalyzedClickInfoHelper {
    AnalyzedClickInfo parse(InputStream inputStream) throws IOException;
    AnalyzedClickInfo parse(InputStream inputStream, Path outputPath) throws IOException;
    AnalyzedClickInfo parse(InputStream inputStream, Path outputPath, Path jsonOutputPath) throws IOException;
    AnalyzedClickInfo readJson(Path jsonFile) throws IOException;
}
