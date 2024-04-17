package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.helpers.ZipFileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class DefaultZipFileUtils implements ZipFileUtils {

    @Override
    public void unzipFile(Path zipFilePath, Path destDirectory) throws IOException {
        log.debug("Unzipping file {} to directory {}", zipFilePath, destDirectory);

        // Validate the zip file path
        if (zipFilePath == null || !Files.exists(zipFilePath) || !zipFilePath.toString().toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Invalid zip file path");
        }

        if (Files.notExists(destDirectory)) {
            Files.createDirectories(destDirectory); // Creates the directory if it does not exist
        }

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // Iterates over entries in the zip file
            while (entry != null) {
                String fileName = entry.getName();
                // If ignoreMacOSFiles is true and the file is a .DS_Store file or the directory is __MACOSX, skip it
                if (fileName.equalsIgnoreCase(".DS_Store") || fileName.contains("__MACOSX")) {
                    entry = zipIn.getNextEntry();
                    continue;
                }
                Path filePath = destDirectory.resolve(fileName);
                if (!entry.isDirectory()) {
                    // Create intermediate directories if they don't exist before writing the file
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    // If the entry is a directory, make the directory
                    Files.createDirectories(filePath);
                }
                entry = zipIn.getNextEntry();
            }
            zipIn.closeEntry();
        }
    }

}