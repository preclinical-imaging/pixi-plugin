package org.nrg.xnatx.plugins.pixi.bli.sessionBuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.axis.utils.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.*;
import org.nrg.xnatx.plugins.pixi.bli.helpers.AnalyzedClickInfoHelper;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Slf4j
public class BliScanBuilder implements Callable<XnatImagescandataBean> {

    private final Path scanDir;
    private final PixiBliscandataBean bliScan;
    private AnalyzedClickInfoHelper analyzedClickInfoHelper;

    public BliScanBuilder(final Path scanDir) {
        this.scanDir = scanDir;
        this.bliScan = new PixiBliscandataBean();
        this.analyzedClickInfoHelper = XDAT.getContextService().getBean(AnalyzedClickInfoHelper.class);
    }

    @Override
    public XnatImagescandataBean call() throws IOException {
        log.debug("Building BLI scans for {}", scanDir);

        String id = scanDir.getFileName().toString();

        bliScan.setId(id);

        AnalyzedClickInfo analyzedClickInfo = analyzedClickInfoHelper.readJson(scanDir.resolve("AnalyzedClickInfo.json"));

        bliScan.setOperator(analyzedClickInfo.getUserLabelNameSet().getUser());

        if (StringUtils.isEmpty(analyzedClickInfo.getClickNumber().getClickNumber())) {
            log.info("Unable to find a UID in AnalyzedClickInfo.txt for scan {}. Will generate a random UID instead.", bliScan.getId());
            bliScan.setUid(UUID.randomUUID().toString());
        } else {
            bliScan.setUid(analyzedClickInfo.getClickNumber().getClickNumber());
        }

        // Set scan datetime
        bliScan.setStartDate(analyzedClickInfo.getLuminescentImage().getAcquisitionDateTime());

        File resourceCatalogXml = new File(scanDir.toFile(), "scan_catalog.xml");
        XnatResourcecatalogBean resourceCatalog = new XnatResourcecatalogBean();

        resourceCatalog.setUri(Paths.get("SCANS", id, "scan_catalog.xml").toString());
        resourceCatalog.setLabel("BLI");
        resourceCatalog.setFormat("BLI");
        resourceCatalog.setContent("BLI");
        resourceCatalog.setDescription("BLI Scan data");

        CatCatalogBean catCatalogBean = new CatCatalogBean();

        try (final Stream<Path> files = Files.list(scanDir)) {
            files.filter(Files::isRegularFile)
                 .map(Path::getFileName)
                 .map(Path::toString)
                 .map(fileName -> {
                     CatEntryBean catEntryBean = new CatEntryBean();
                     catEntryBean.setUri(fileName);
                     return catEntryBean;
                 })
                 .forEach(catCatalogBean::addEntries_entry);
        }

        bliScan.addFile(resourceCatalog);

        try (FileWriter resourceCatalogXmlWriter = new FileWriter(resourceCatalogXml)) {
            catCatalogBean.toXML(resourceCatalogXmlWriter, true);
        } catch (IOException e) {
            log.error("Unable to write scan catalog", e);
        }

        return bliScan;
    }

    public void setAnalyzedClickInfoHelper(AnalyzedClickInfoHelper analyzedClickInfoHelper) {
        this.analyzedClickInfoHelper = analyzedClickInfoHelper;
    }
}
