package org.nrg.xnatx.plugins.pixi.hotelsplitter.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * Initializes a Hotel SCP receiver. It will only create the receiver if a hotel receiver does not already exist.
 */
@Component
@Slf4j
public class CreateDefaultHotelScpReceiver extends AbstractInitializingTask {

    private final DatabaseHelper databaseHelper;
    private final DicomSCPManager dicomSCPManager;

    @Autowired
    public CreateDefaultHotelScpReceiver(final DatabaseHelper databaseHelper,
                                         final DicomSCPManager dicomSCPManager) {
        this.databaseHelper = databaseHelper;
        this.dicomSCPManager = dicomSCPManager;
    }

    @Override
    public String getTaskName() {
        return "InitializeHotelScpReceiver";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        log.debug("Initializing Hotel SCP receiver.");

        try {
            if (!databaseHelper.tableExists("xhbm_dicomscpinstance")) {
                log.debug("The table 'xhbm_dicomscpinstance' does not yet exist. Deferring execution.");
                throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "The table 'xhbm_dicomscpinstance' does not yet exist. Deferring execution.");
            }
        } catch (SQLException e) {
            String message = "An error occurred trying to access the database to check for the table 'xhbm_dicomscpinstance'.";
            log.error(message, e);
            throw new InitializingTaskException(InitializingTaskException.Level.Error, message, e);
        }

        // Check if a hotel receiver already exists
        // Either the AE title contains "hotel" or the subject routing expression contains "r:hotel" (regex to replace the subject ID with "Hotel")
        boolean hotelReceiverExists = dicomSCPManager.getDicomSCPInstancesList()
                .stream()
                .anyMatch(dicomSCPInstance ->
                                  (dicomSCPInstance.getAeTitle() != null && dicomSCPInstance.getAeTitle().toLowerCase().contains("hotel")) || (dicomSCPInstance.getSubjectRoutingExpression() != null && dicomSCPInstance.getSubjectRoutingExpression().toLowerCase().contains("r:hotel"))
                );

        if (hotelReceiverExists) {
            log.debug("A hotel session DICOM SCP instance already exists. Skipping initialization.");
        } else {
            log.debug("A hotel session DICOM SCP instance does not exist. Creating it.");
            DicomSCPInstance hotelScpInstance = DicomSCPInstance.createDefault();
            hotelScpInstance.setAnonymizationEnabled(false);
            hotelScpInstance.setRoutingExpressionsEnabled(true);
            hotelScpInstance.setAeTitle("PIXI_HOTEL");
            hotelScpInstance.setProjectRoutingExpression("(0010,4000):Project:(\\w+)\\s*Session:(\\w+):1\n" +
                                                         "(0032,4000):Project:(\\w+)\\s*Session:(\\w+):1\n" +
                                                         "(0010,21B0):Project:(\\w+)\\s*Session:(\\w+):1\n" +
                                                         "(0008,1030):(.*)\n" +
                                                         "(0008,0050):(.*)");
            hotelScpInstance.setSubjectRoutingExpression("(0010,0010):^(.*)$:1 t:^(.*)$ r:Hotel\n" +
                                                         "(0010,0020):^(.*)$:1 t:^(.*)$ r:Hotel");
            hotelScpInstance.setSessionRoutingExpression("(0010,4000):Project:(\\w+)\\s*Session:(\\w+):2\n" +
                                                         "(0032,4000):Project:(\\w+)\\s*Session:(\\w+):2\n" +
                                                         "(0010,21B0):Project:(\\w+)\\s*Session:(\\w+):2\n" +
                                                         "(0010,0020):(.*)");
            try {
                dicomSCPManager.saveDicomSCPInstance(hotelScpInstance);
                log.info("Created hotel session DICOM SCP instance.");
            } catch (Exception e) {
                log.error("An error occurred trying to create the hotel session DICOM SCP instance.", e);
            }
        }
    }
}
