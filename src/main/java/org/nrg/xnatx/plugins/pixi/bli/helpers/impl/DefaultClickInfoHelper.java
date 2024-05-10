package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.helpers.ClickInfoHelper;
import org.nrg.xnatx.plugins.pixi.bli.models.*;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Scanner;

@Service
@Slf4j
public class DefaultClickInfoHelper implements ClickInfoHelper {

    @Override
    public ClickInfo parseTxt(Path txtFile) throws IOException {
        // Verify that the file exists
        if (!txtFile.toFile().exists()) {
            throw new IOException("File does not exist: " + txtFile.toString());
        }

        // Create an InputStream from the file
        try (InputStream inputStream = txtFile.toUri().toURL().openStream()) {
            return parseTxt(inputStream, null, null);
        }
    }

    @Override
    public ClickInfo parseTxt(InputStream inputStream, Path outputPath, Path jsonOutputPath) throws IOException {
        log.debug("Parsing ClickInfo.txt.");

        BufferedWriter outputWriter = null;

        if (outputPath != null) {
            outputWriter = new BufferedWriter(new FileWriter(outputPath.toFile()));
        }

        ClickInfo clickInfo = new ClickInfo();
        ClickNumber clickNumber = new ClickNumber();
        CameraSystemInfo cameraSystemInfo = new CameraSystemInfo();

        // Scanner will find the subject and session labels if they weren't already provided
        Scanner clickInfoScanner = new Scanner(inputStream);

        while (clickInfoScanner.hasNextLine()) {
            String line = clickInfoScanner.nextLine();

            if (outputWriter != null) {
                outputWriter.write(line);
                outputWriter.newLine();
            }

            // File should be colon delineated
            String[] splitLine = line.split(":", 2);

            if (splitLine.length == 2) {

                String key = splitLine[0];
                String value = splitLine[1].trim();

                switch (key) {
                    // Camera System Info
                    case("Living Image Version"): {
                        clickNumber.setLivingImageVersion(value);
                        break;
                    }
                    case("Camera System ID"): {
                        cameraSystemInfo.setCameraSystemId(value);
                        break;
                    }
                    case("Camera System alias"): {
                        cameraSystemInfo.setCameraSystemAlias(value);
                        break;
                    }
                    case("System Configuration"): {
                        cameraSystemInfo.setSystemConfiguration(value);
                        break;
                    }
                }
            }
        }

        // Finished parsing ClickInfo.txt
        if (outputWriter != null) {
            outputWriter.flush();
            outputWriter.close();
        }

        // Build ClickInfo object
        clickInfo.setClickNumber(clickNumber);
        clickInfo.setCameraSystemInfo(cameraSystemInfo);

        if (jsonOutputPath != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(jsonOutputPath.toFile(), clickInfo);
        }

        return clickInfo;
    }
}
