package org.nrg.xnatx.plugins.pixi.bli.helpers;

import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface AnalyzedClickInfoHelper {
    AnalyzedClickInfo parseTxt(Path txtFile) throws IOException;
    AnalyzedClickInfo parseTxt(InputStream inputStream) throws IOException;
    AnalyzedClickInfo parseTxt(InputStream inputStream, Path outputPath) throws IOException;
    AnalyzedClickInfo parseTxt(InputStream inputStream, Path outputPath, Path jsonOutputPath) throws IOException;
}
