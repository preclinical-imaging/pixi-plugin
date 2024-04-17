package org.nrg.xnatx.plugins.pixi.bli.helpers;

import java.io.IOException;
import java.nio.file.Path;

public interface ZipFileUtils {

    void unzipFile(Path zipFilePath, Path destDirectory) throws IOException;
}