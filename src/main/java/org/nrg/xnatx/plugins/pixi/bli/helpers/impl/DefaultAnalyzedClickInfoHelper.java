package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.helpers.AnalyzedClickInfoHelper;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;
import org.nrg.xnatx.plugins.pixi.bli.models.ClickNumber;
import org.nrg.xnatx.plugins.pixi.bli.models.LuminescentImage;
import org.nrg.xnatx.plugins.pixi.bli.models.UserLabelNameSet;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@Component
@Slf4j
public class DefaultAnalyzedClickInfoHelper implements AnalyzedClickInfoHelper {

    public AnalyzedClickInfo parseTxt(InputStream inputStream) throws IOException {
        return parseTxt(inputStream, null, null);
    }

    public AnalyzedClickInfo parseTxt(InputStream inputStream, Path outputPath) throws IOException {
        return parseTxt(inputStream, outputPath, null);
    }

    public AnalyzedClickInfo parseTxt(InputStream inputStream, Path outputPath, Path jsonOutputPath) throws IOException {
        log.debug("Parsing AnalyzedClickInfo.txt.");

        BufferedWriter outputWriter = null;

        if (outputPath != null) {
            outputWriter = new BufferedWriter(new FileWriter(outputPath.toFile()));
        }

        AnalyzedClickInfo analyzedClickInfo = new AnalyzedClickInfo();
        ClickNumber clickNumber = new ClickNumber();
        LuminescentImage luminescentImage = new LuminescentImage();
        UserLabelNameSet userLabelNameSet = new UserLabelNameSet();

        // Scanner will find the subject and session labels if they weren't already provided
        Scanner analyzedClickInfoScanner = new Scanner(inputStream);

        boolean userLabelNameSetParsed = false;

        while (analyzedClickInfoScanner.hasNextLine()) {
            String line = analyzedClickInfoScanner.nextLine();

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
                    // ClickNumber
                    case("*** ClickNumber"): {
                        clickNumber.setClickNumber(value);

                        // ClickInfoType
                        String clickInfoType = analyzedClickInfoScanner.nextLine();

                        if (outputWriter != null) {
                            outputWriter.write(clickInfoType);
                            outputWriter.newLine();
                        }

                        clickInfoType = clickInfoType.split(":", 2)[1];
                        clickInfoType = clickInfoType.trim();

                        clickNumber.setClickInfoType(clickInfoType);

                        break;
                    }

                    // Luminescent Image
                    case ("*** luminescent image"): {
                        luminescentImage.setLuminescentImage(value);

                        // Acquisition Date
                        String acqDate = analyzedClickInfoScanner.nextLine();

                        if (outputWriter != null) {
                            outputWriter.write(acqDate);
                            outputWriter.newLine();
                        }

                        acqDate = acqDate.split(":", 2)[1];
                        acqDate = acqDate.trim();

                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
                        luminescentImage.setAcquisitionDate(LocalDate.parse(acqDate, dateFormatter));

                        // Acquisition Time
                        String acqTime = analyzedClickInfoScanner.nextLine();

                        if (outputWriter != null) {
                            outputWriter.write(acqTime);
                            outputWriter.newLine();
                        }

                        acqTime = acqTime.split(":", 2)[1];
                        acqTime = acqTime.trim();

                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        luminescentImage.setAcquisitionTime(LocalTime.parse(acqTime, timeFormatter));

                        // Acquisition Seconds
                        String acqSeconds = analyzedClickInfoScanner.nextLine();

                        if (outputWriter != null) {
                            outputWriter.write(acqSeconds);
                            outputWriter.newLine();
                        }

                        acqSeconds = acqSeconds.split(":", 2)[1];
                        acqSeconds = acqSeconds.trim();

                        luminescentImage.setAcquisitionSeconds(Long.parseLong(acqSeconds));

                        break;
                    }

                    // User Label Name Set
                    case ("*** User Label Name Set"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setUserLabelNameSet(value);
                        break;
                    }
                    case ("User"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setUser(value);
                        break;
                    }
                    case ("Group"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setGroup(value);
                        break;
                    }
                    case ("Experiment"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setExperiment(value);
                        break;
                    }
                    case("Comment1"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setComment1(value);
                        break;
                    }
                    case("Comment2"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setComment2(value);
                        break;
                    }
                    case("Time Point"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setTimePoint(value);
                        break;
                    }
                    case ("Animal Number"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setAnimalNumber(value);
                        break;
                    }
                    case ("Animal Strain"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setAnimalStrain(value);
                        break;
                    }
                    case ("Animal Model"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setAnimalModel(value);
                        break;
                    }
                    case ("Sex"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setSex(value);
                        break;
                    }
                    case ("View"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setView(value);
                        break;
                    }
                    case ("Cell Line"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setCellLine(value);
                        break;
                    }
                    case ("Reporter"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setReporter(value);
                        break;
                    }
                    case ("Treatment"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setTreatment(value);
                        break;
                    }
                    case ("Luc Injection Time"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setLucInjectionTime(value);
                        break;
                    }
                    case ("IACUC Number"): {
                        if (!userLabelNameSetParsed) userLabelNameSet.setIacucNumber(value);
                        userLabelNameSetParsed = true;
                        break;
                    }
                }
            }
        }

        // Finished parsing AnalyzedClickInfo.txt
        if (outputWriter != null) {
            outputWriter.flush();
            outputWriter.close();
        }

        // Build AnalyzedClickInfo object
        analyzedClickInfo.setClickNumber(clickNumber);
        analyzedClickInfo.setLuminescentImage(luminescentImage);
        analyzedClickInfo.setUserLabelNameSet(userLabelNameSet);

        if (jsonOutputPath != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(jsonOutputPath.toFile(), analyzedClickInfo);
        }

        return analyzedClickInfo;
    }

    @Override
    public AnalyzedClickInfo readJson(Path jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper.readValue(jsonFile.toFile(), AnalyzedClickInfo.class);
    }

    @Override
    public AnalyzedClickInfo readJson(InputStream inputStream) throws IOException {
        return readJson(inputStream, null);
    }

    @Override
    public AnalyzedClickInfo readJson(InputStream inputStream, Path jsonOutputPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        final AnalyzedClickInfo analyzedClickInfo = mapper.readValue(inputStream, AnalyzedClickInfo.class);

        if (jsonOutputPath != null) {
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(jsonOutputPath.toFile(), analyzedClickInfo);
        }

        return analyzedClickInfo;
    }
}
