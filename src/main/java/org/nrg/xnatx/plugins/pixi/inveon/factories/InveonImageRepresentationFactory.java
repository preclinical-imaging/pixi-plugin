package org.nrg.xnatx.plugins.pixi.inveon.factories;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.nrg.xnatx.plugins.pixi.inveon.models.InveonImageRepresentation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class InveonImageRepresentationFactory {

    public InveonImageRepresentation buildImageRepresentationFromFolder(String folder) {
        log.info("buildImageRepresentationFromFolder {}", folder);
        InveonImageRepresentation inveonImageRepresentation = new InveonImageRepresentation();

        try (final Stream<Path> files = Files.list(Paths.get(folder))) {
            List<Path> pathList = files.filter(Files::isRegularFile).collect(Collectors.toList());

            for (Path path : pathList) {
                String extension = FilenameUtils.getExtension(path.toString());
                log.info("Filename and extension {} {}", path.toString(), extension);
                if (extension.equals("hdr")) {
                    inveonImageRepresentation.setHeaderFileName(path.toString());
                } else if (extension.equals("img")) {
                    inveonImageRepresentation.setPixelFileName(path.toString());
                } else {
                    log.info("Unrecognized file extension: {} ", extension);
                }
            }
            this.fillInveonHeaderMap(inveonImageRepresentation);
        } catch (java.io.IOException e) {

        }
        return inveonImageRepresentation;
    }
    public void fillInveonHeaderMap(InveonImageRepresentation inveonImageRepresentation) {
        String fullPath = (inveonImageRepresentation.getPrearchiveTempFolder() == null) ?
                inveonImageRepresentation.getHeaderFileName() :
                inveonImageRepresentation.getPrearchiveTempFolder() + "/" + inveonImageRepresentation.getHeaderFileName();
        log.info("InveonImageRepresentationFactory::fillInveonHeaderMap: header file {}", fullPath);
        try (Stream<String> stream = Files.lines(Paths.get(fullPath))) {
            stream
                    .filter(s -> ! s.startsWith("#"))
                    .forEach(s -> this.processHeaderLine(inveonImageRepresentation, s));
        } catch (Exception e) {
            log.error("File read exception when reading: {}", fullPath);
            log.error(e.toString());
        }
        String pixelFileName = new File(inveonImageRepresentation.getPixelFileName()).getName();
        log.info("Pixel File Name {}", pixelFileName);
        // As an example: mpet4800a_ct1_v1.ct.img
        String[] tokens = pixelFileName.split("\\.");
        inveonImageRepresentation.setName(tokens[0]);

        inveonImageRepresentation.setModality(mapModality(inveonImageRepresentation.getHeaderValue("modality")));
    }

    private String mapModality(String value) {
        int v = Integer.parseInt(Optional.ofNullable(value).orElse("0"));
        String rtn = "Unknown";
        switch (v) {
            case -1:
                rtn = "UN";
                break;
            case 0:
                rtn = "PET";
                break;
            case 1:
                rtn = "CT";
                break;
            case 2: // SPECT maps to NM
                rtn = "NM";
                break;
        }
        log.info("Map modality {} {}", value, rtn);
        return rtn;
    }

    public Date transformScanDate(InveonImageRepresentation inveonImageRepresentation) {
        try {
            String dateTimeString = inveonImageRepresentation.getHeaderValue("SCAN_DATE")
                    + ":"
                    + inveonImageRepresentation.getHeaderValue("SCAN_TIME");
            LocalDateTime xx = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyyMMdd:HHmmss"));
            Instant instant = xx.atZone(ZoneId.systemDefault()).toInstant();
            Date date = Date.from(instant);
            return date;
        } catch (Exception e) {
            log.info(e.toString());
            return new Date();
        }
//        LocalDate date = LocalDate.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));

    }

    private void processHeaderLine(InveonImageRepresentation inveonImageRepresentation, String line) {
        //log.info("{}", line);

        String[] tokens = line.split(" ");
        //log.info("{}", tokens.length);
        if (tokens.length == 1) {
            inveonImageRepresentation.putHeaderValue(tokens[0], "");
        } else if (tokens.length == 2) {
            inveonImageRepresentation.putHeaderValue(tokens[0], tokens[1]);
            processSpecialHeaderLines(inveonImageRepresentation, tokens[0], tokens[1]);
        } else {
            inveonImageRepresentation.addUnmappedLine(line);
            processSpecialHeaderLines(inveonImageRepresentation, line);
        }
    }

    private void processSpecialHeaderLines(InveonImageRepresentation inveonImageRepresentation,String key, String value) {
        if (key.equals("scan_time")) {
            //log.info("Process scan_time");
            LocalDate scanTime = LocalDate.parse(value);
            String formattedDate = scanTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String formattedTime = scanTime.format(DateTimeFormatter.ofPattern("HHmm"));
            inveonImageRepresentation.putHeaderValue("SCAN_DATE", formattedDate);
            inveonImageRepresentation.putHeaderValue("SCAN_TIME", formattedTime);
            //log.info("PUT {}", formattedDate);
            //log.info("PUT {}", formattedTime);
        } else if (key.equals("acquisition_mode")) {
            inveonImageRepresentation.putHeaderValue("ACQUISITION_MODE_TEXT", mapAcquistionMode(value));
            log.info("Acquisition mode code {} and text {}",
                    value,
                    inveonImageRepresentation.getHeaderValue("ACQUISITION_MODE_TEXT"));
        } else if (key.equals("recon_algorithm")) {
            inveonImageRepresentation.putHeaderValue("RECON_ALGORITHM_TEXT", mapReconAlgorithm(value));
        }
    }

    private String mapReconAlgorithm(String value) {
        int mode = Integer.parseInt(value);
        String rtn = "Unknown, or no, algorithm type";
        switch (mode) {
            case 0:
                rtn = "Unknown, or no, algorithm type";
                break;
            case 1:
                rtn = "Filtered Backprojection";
                break;
            case 2:
                rtn = "OSEM2d";
                break;
            case 3:
                rtn = "OSEM3d";
                break;
            case 4:
                rtn = "3D Reprojection";
                break;
            // The value 5 does not show up in examples as documentation
            // We include it here to indicate we did not overlook it.
            case 5:
                rtn = "Unknown, or no, algorithm type";
                break;
            case 6:
                rtn = "OSEM3D/MAP";
                break;
            case 7:
                rtn = "MAPTR for transmission image";
                break;
            case 8:
                rtn = "MAP 3D reconstruction";
                break;
            case 9:
                rtn = "Feldkamp cone beam";
                break;

            default:
                break;
        }

        return rtn;
    }
    private String mapAcquistionMode(String value) {
        int mode = Integer.parseInt(value);
        String rtn = "Unknown acquisition mode";
        switch (mode) {
            case 0:
                rtn = "Unknown acquisition mode";
                break;
            case 1:
                rtn = "Blank acquisition";
                break;
            case 2:
                rtn = "Emission acquisition";
                break;
            case 3:
                rtn = "Dynamic acquisition";
                break;
            case 4:
                rtn = "Gated acquisition";
                break;
            case 5:
                rtn = "Continuous bed motion acquisition";
                break;
            case 6:
                rtn = "Singles transmission acquisition";
                break;
            case 7:
                rtn = "Windowed coincidence transmission acquisition";
                break;
            case 8:
                rtn = "Non-windowed coincidence transmission acquisition";
                break;
            case 9:
                rtn = "CT projection acquisition";
                break;
            case 10:
                rtn = "CT calibration acquisition";
            case 11:
                rtn = "SPECT planar projection acquisition";
                break;
            case 12:
                rtn = "SPECT multi-projection acquisition";
                break;
            case 13:
                rtn = "SPECT calibration acquisition";
                break;
            case 14:
                rtn = "SPECT tomography normalization acquisition";
                break;
            case 15:
                rtn = "SPECT detector setup acquisition";
                break;
            case 16:
                rtn = "SPECT scout view acquisition";
                break;
            case 17:
                rtn = "SPECT planar normalization acquisition";
                break;
            default:
                break;
        }
        return rtn;
    }

    private void processSpecialHeaderLines(InveonImageRepresentation inveonImageRepresentation, String line) {

        String[] tokens = line.split(" ");
        String key = tokens[0];
        int keyLength = key.length();
        if (key.equals("scan_time")) {
            try {
                // Example: scan_time Thu Dec 14 11:32:21 2023
                String rawDate = tokens[2] + " " + tokens[3] + " " + tokens[5];
                //log.info("Parse this date: {}", rawDate);
                LocalDate xx = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("MMM dd yyyy"));
                String formattedDate = xx.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                //log.info("Formatted date: {}", formattedDate);
                String formattedTime = tokens[4].replaceAll(":", "");
                //log.info("Formatted time: {}", formattedTime);
                String formatedDateTime = formattedDate + " " + formattedTime;
                inveonImageRepresentation.putHeaderValue("SCAN_DATE", formattedDate);
                inveonImageRepresentation.putHeaderValue("SCAN_TIME", formattedTime);
                inveonImageRepresentation.putHeaderValue("SCAN_DATE_TIME", formatedDateTime);
                String value = line.substring(keyLength + 1);
                inveonImageRepresentation.putHeaderValue(key, value);

                //log.info("PUT {}", formattedDate);
                //log.info("PUT {}", formattedTime);
            } catch (Exception e) {
                // TODO
                log.info(e.toString());
                throw e;
            }
        } else if (key.equals("injected_compound")) {
            String value = line.substring(keyLength + 1);
            inveonImageRepresentation.putHeaderValue(key, value);
        }
    }

}
