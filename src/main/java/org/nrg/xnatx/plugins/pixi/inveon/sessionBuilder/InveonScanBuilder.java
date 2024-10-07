package org.nrg.xnatx.plugins.pixi.inveon.sessionBuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.bean.*;
import org.nrg.xnatx.plugins.pixi.inveon.factories.InveonImageRepresentationFactory;
import org.nrg.xnatx.plugins.pixi.inveon.models.InveonImageRepresentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Slf4j
public class InveonScanBuilder implements Callable<XnatImagescandataBean> {

    private final Path scanDir;

    private final InveonImageRepresentationFactory factory = new InveonImageRepresentationFactory();

    public InveonScanBuilder(final Path scanDir) {
        this.scanDir = scanDir;
    }

    @Override
    public XnatImagescandataBean call() throws Exception {
        if (scanDir == null) {
            throw new Exception("Unable to process/create an XnatImagescandataBean; the scanDir variable is null");
        }
        log.debug("Building Inveon scans for {}", scanDir);

        InveonImageRepresentation inveonImageRepresentation = factory.buildImageRepresentationFromFolder(scanDir.toString());
        XnatImagescandataBean imagescandataBean = buildImageScanDataBean(inveonImageRepresentation);

        //String id = scanDir.getFileName().toString();
        String id = scanDir.getParent().getFileName().toString();

        // Need to replace any special characters in the ID
        id = id.replaceAll("[^a-zA-Z0-9]", "_");

        int maxIdLength = 63; // Arbitrary but reasonable length

        if (id.length() > maxIdLength) {
            id = id.substring(0, maxIdLength);
        }

        log.debug("Scan ID {}", id);

        imagescandataBean.setId(id);
        imagescandataBean.setUid(UUID.randomUUID().toString());

        // TODO fix this
//        imagescandataBean.setStartDate(factory.transformScanDate(inveonImageRepresentation));

        File resourceCatalogXml = new File(scanDir.toFile(), "scan_catalog.xml");
        XnatResourcecatalogBean resourceCatalog = new XnatResourcecatalogBean();
        resourceCatalog.setUri(Paths.get("SCANS", id, "INVEON", "scan_catalog.xml").toString());
        resourceCatalog.setLabel("INVEON");
        resourceCatalog.setFormat("INVEON");
        resourceCatalog.setContent("INVEON");
        resourceCatalog.setDescription("Inveon " + imagescandataBean.getModality() + " scan data");

        CatCatalogBean catCatalogBean = new CatCatalogBean();

        log.debug("Walk through scanDir: {}", scanDir);

        try (final Stream<Path> files = Files.list(scanDir)) {
            files.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(fileName -> {
                        CatEntryBean catEntryBean = new CatEntryBean();
                        catEntryBean.setUri(fileName);
                        Path p = Paths.get(scanDir.toString(), fileName);

                        log.debug("File {} exists? {}", p, Files.exists(p));
                        log.debug("File name: {}", fileName);
                        return catEntryBean;
                    })
                    .forEach(catCatalogBean::addEntries_entry);
        }

        log.debug("Resource catalog: {}", resourceCatalog);
        log.debug("Resource Catalog XML: {}", resourceCatalogXml);
        imagescandataBean.addFile(resourceCatalog);

        try (FileWriter resourceCatalogXmlWriter = new FileWriter(resourceCatalogXml)) {
            catCatalogBean.toXML(resourceCatalogXmlWriter, true);
        } catch (IOException e) {
            log.error("Unable to write scan catalog", e);
            throw e;
        }

        log.debug("Inveon Scan Builder, about to return XNAT Scan Data Bean");
        return imagescandataBean;
    }

    private XnatImagescandataBean buildImageScanDataBean(InveonImageRepresentation inveonImageRepresentation) throws Exception {
        XnatImagescandataBean scandataBean = null;
        if (inveonImageRepresentation.getModality().equals("PET")) {
            scandataBean = buildPetScandataBean(inveonImageRepresentation);
        } else if (inveonImageRepresentation.getModality().equals("CT")) {
            scandataBean = buildCTScandataBean(inveonImageRepresentation);
        } else {
            log.error("Unrecognized modality {} from this Inveon Image {}", inveonImageRepresentation.getModality(), inveonImageRepresentation.getName());
        }
        if (scandataBean == null) {
            throw new Exception("Could not create an XnatImagescandataBean for this instance of InveonImageRepresentation, Name: " + inveonImageRepresentation.getName() + ", Modality: " + inveonImageRepresentation.getModality());
        }
        fillCommonScanParameters(scandataBean, inveonImageRepresentation);
        return scandataBean;
    }


    private void fillCommonScanParameters(XnatImagescandataBean scandataBean, InveonImageRepresentation inveonImageRepresentation) {
        log.debug("::fillCommonScanParameters, name: {}, modality: {}, time stamp: {}, header file: {}",
                inveonImageRepresentation.getName(),
                inveonImageRepresentation.getModality(),
                inveonImageRepresentation.getTimestamp(),
                inveonImageRepresentation.getHeaderFileName());

        Map<String, String> map = inveonImageRepresentation.getHeaderMap();

        scandataBean.setModality(inveonImageRepresentation.getModality().toUpperCase());
        scandataBean.setScanner_manufacturer(Optional.ofNullable(map.get("manufacturer")).orElse(""));

        // TODO Move the map to the factory code
        scandataBean.setScanner_model(mapModel(map.get("model")));

        scandataBean.setFrames(Optional.ofNullable(map.get("total_frames")).orElse(""));
        Date scanDateTime = factory.transformScanDate(inveonImageRepresentation);
        scandataBean.setStartDate(scanDateTime);
        scandataBean.setStarttime(scanDateTime);
        scandataBean.setOperator(Optional.ofNullable(map.get("operator")).orElse(""));
        scandataBean.setType(Optional.ofNullable(map.get("ACQUISITION_MODE_TEXT")).orElse(""));
        log.debug("Scan Data Bean type {}", scandataBean.getType());
    }

    String mapModel(String value) {
        int v = Integer.parseInt(Optional.ofNullable(value).orElse("0"));
        String rtn = "Unknown";
        switch (v) {
            case 0:
                rtn = "Unknown";
                break;
            case 2000:
                rtn = "Primate";
                break;
            case 2001:
                rtn = "Rodent";
                break;
            case 2002:
                rtn = "microPET2";
                break;
            case 2500:
                rtn = "Focus_220";
                break;
            case 2501:
                rtn = "Focus_120";
                break;
            case 3000:
                rtn = "mCAT";
                break;
            case 3500:
                rtn = "mCATII";
                break;
            case 4000:
                rtn = "mSPECT";
                break;
            case 5000:
                rtn = "Inveon_Dedicated_PET";
                break;
            case 5001:
                rtn = "Inveon_MM_Platform";
                break;
            case 6000:
                rtn = "MR_PET_Head_Insert";
                break;
            case 8000:
                rtn = "Tuebingen_PET_MR";
                break;
            default:
        }
        return rtn;
    }

    private XnatPetscandataBean buildPetScandataBean(InveonImageRepresentation inveonImageRepresentation) {
        XnatPetscandataBean scandataBean = new XnatPetscandataBean();

        Map <String, String> map = inveonImageRepresentation.getHeaderMap();
        String injectedCompound = Optional.ofNullable(map.get("injected_compound")).orElse("");
        String isotopeName      = Optional.ofNullable(map.get("isotope")).orElse("");
        String isotopeHalfLife  = Optional.ofNullable(map.get("isotope_half_life")).orElse("");
        String injectionTime    = Optional.ofNullable(map.get("injection_time")).orElse("");
        String dose             = Optional.ofNullable(map.get("dose")).orElse("");
        String doseUnits        = Optional.ofNullable(map.get("dose_units")).orElse("");
        String delimiter = "\t";
        log.debug("Injected compound {} ", injectedCompound);

        //TODO Fix this
        scandataBean.setNote(injectedCompound + delimiter + isotopeName + delimiter + isotopeHalfLife + delimiter +
                injectionTime + delimiter + dose + delimiter + doseUnits);
        //scandataBean.setParameters_recontype(inveonImageRepresentation.getHeaderValue("RECON_ALGORITHM_TEXT"));
        return scandataBean;
    }

    private XnatCtscandataBean buildCTScandataBean(InveonImageRepresentation inveonImageRepresentation) {
        XnatCtscandataBean scandataBean = new XnatCtscandataBean();

        // TODO Review and add other parameters
        if (StringUtils.isNotBlank(inveonImageRepresentation.getHeaderValue("ct_exposure_time"))) {
            scandataBean.setParameters_exposuretime(inveonImageRepresentation.getHeaderValue("ct_exposure_time"));
        }

        if (StringUtils.isNotBlank(inveonImageRepresentation.getHeaderValue("ct_xray_voltage"))) {
            scandataBean.setParameters_kvp(inveonImageRepresentation.getHeaderValue("ct_xray_voltage"));
        }

        if (StringUtils.isNotBlank(inveonImageRepresentation.getHeaderValue("ct_anode_current"))) {
            scandataBean.setParameters_xraytubecurrent(inveonImageRepresentation.getHeaderValue("ct_anode_current"));
        }

        if (StringUtils.isNotBlank(inveonImageRepresentation.getHeaderValue("ct_source_to_detector"))) {
            scandataBean.setParameters_distancesourcetodetector(inveonImageRepresentation.getHeaderValue("ct_source_to_detector"));
        }

        if (StringUtils.isNotBlank(inveonImageRepresentation.getHeaderValue("pixel_size_x"))) {
            scandataBean.setParameters_voxelres_x(inveonImageRepresentation.getHeaderValue("pixel_size_x"));
        }

        if (StringUtils.isNotBlank(inveonImageRepresentation.getHeaderValue("pixel_size_y"))) {
            scandataBean.setParameters_voxelres_y(inveonImageRepresentation.getHeaderValue("pixel_size_y"));
        }

        if (StringUtils.isNotBlank(inveonImageRepresentation.getHeaderValue("pixel_size_z"))) {
            scandataBean.setParameters_voxelres_z(inveonImageRepresentation.getHeaderValue("pixel_size_z"));
        }

        return scandataBean;
    }
}
