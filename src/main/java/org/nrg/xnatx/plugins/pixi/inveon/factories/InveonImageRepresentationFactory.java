package org.nrg.xnatx.plugins.pixi.inveon.factories;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class InveonImageRepresentationFactory {

    public InveonImageRepresentation buildImageRepresentationFromFolder(String folder) {
        log.debug("buildImageRepresentationFromFolder {}", folder);
        InveonImageRepresentation inveonImageRepresentation = new InveonImageRepresentation();

        try (final Stream<Path> files = Files.list(Paths.get(folder))) {
            List<Path> pathList = files.filter(Files::isRegularFile).collect(Collectors.toList());

            for (Path path : pathList) {
                String extension = FilenameUtils.getExtension(path.toString());
                log.debug("Filename and extension {} {}", path.toString(), extension);
                if (extension.equals("hdr")) {
                    inveonImageRepresentation.setHeaderFileName(path.toString());
                } else if (extension.equals("img")) {
                    inveonImageRepresentation.setPixelFileName(path.toString());
                } else {
                    log.info("Unrecognized file extension: {}, full path {}", extension, path.toString());
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
        log.info("::fillInveonHeaderMap(): header file {}", fullPath);

        // Read each line of the header, skipping those that begin with #
        // Invoke this.processHeaderLine for each line. That method will either
        // process the line directly or delegate to another method.
        try (Stream<String> stream = Files.lines(Paths.get(fullPath))) {
            stream
                    .filter(s -> ! s.startsWith("#"))
                    .forEach(s -> this.processHeaderLine(inveonImageRepresentation, s));
        } catch (Exception e) {
            log.error("File read exception when reading: {}", fullPath);
            log.error(e.toString());
        }

        // As an example: mpet4800a_ct1_v1.ct.img
        String pixelFileName = new File(inveonImageRepresentation.getPixelFileName()).getName();
        inveonImageRepresentation.setName(FilenameUtils.getBaseName(pixelFileName).replaceAll("\\.", "_"));

        inveonImageRepresentation.setModality(mapModality(inveonImageRepresentation.getHeaderValue("modality")));
    }

    // Map the integer value for modality taken from the Inveon .hdr file to
    // a string known by XNAT.
    private String mapModality(String value) {
        if (StringUtils.isBlank(value)) {
            value = "0";
        }
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
        return rtn;
    }

    // Transform Date/Time values stored in the Inveon .hdr file to yyyyMMdd:HHmmss format expected by XNAT
    // We are pulling the values for keys SCAN_DATE and SCAN_TIME. These are not original values
    // in the .hdr file. These are values already derived from the Time/Date string in the .hdr file
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
            log.error(e.toString());
            return new Date();
        }
    }

    // Process a text line from the Inveon .hdr file
    // Most of the lines are of the format: Key Value
    // We just add those Key/Value pairs to the map in the InveonImageRepresentation
    // We also invoke lower level methods that will provide special handling for
    // lines that need it, such as lines that have spaces in the value or
    // lines that need further interpretation of the Value.
    private void processHeaderLine(InveonImageRepresentation inveonImageRepresentation, String line) {
        String[] tokens = line.split(" ");
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
            LocalDate scanTime = LocalDate.parse(value);
            String formattedDate = scanTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String formattedTime = scanTime.format(DateTimeFormatter.ofPattern("HHmm"));
            inveonImageRepresentation.putHeaderValue("SCAN_DATE", formattedDate);
            inveonImageRepresentation.putHeaderValue("SCAN_TIME", formattedTime);
        } else if (key.equals("acquisition_mode")) {
            inveonImageRepresentation.putHeaderValue("ACQUISITION_MODE_TEXT", mapAcquistionMode(value));
        } else if (key.equals("recon_algorithm")) {
            inveonImageRepresentation.putHeaderValue("RECON_ALGORITHM_TEXT", mapReconAlgorithm(value));
        }
    }

    // Map the integer value for Reconstruction Algorithm in the Inveon .hdr file to a text string.
    // The mapping is copied from the Inveon file comments. You could choose to use different text
    // in the mapping.
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
                // Example: scan_time Thu Dec 4 11:32:21 2023
                String rawDate = tokens[2] + " " + tokens[3] + " " + tokens[5];
                LocalDate xx = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("MMM d yyyy"));
                String formattedDate = xx.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String formattedTime = tokens[4].replaceAll(":", "");
                String formatedDateTime = formattedDate + " " + formattedTime;
                inveonImageRepresentation.putHeaderValue("SCAN_DATE", formattedDate);
                inveonImageRepresentation.putHeaderValue("SCAN_TIME", formattedTime);
                inveonImageRepresentation.putHeaderValue("SCAN_DATE_TIME", formatedDateTime);
                String value = line.substring(keyLength + 1);
                inveonImageRepresentation.putHeaderValue(key, value);
            } catch (Exception e) {
                // TODO
                log.error(e.toString());
                throw e;
            }
        } else if (key.equals("injected_compound")) {
            String value = line.substring(keyLength + 1);
            inveonImageRepresentation.putHeaderValue(key, value);
        } else if (key.equals("injection_time")) {
            String value = line.substring(keyLength + 1);
            inveonImageRepresentation.putHeaderValue(key, value);
        }
    }
}
