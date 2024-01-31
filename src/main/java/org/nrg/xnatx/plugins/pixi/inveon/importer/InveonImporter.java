package org.nrg.xnatx.plugins.pixi.inveon.importer;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.fileExtraction.Format;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@ImporterHandler(handler = InveonImporter.IMPORTER_HANDLER)
public class InveonImporter extends ImporterHandlerA {

    public static final String IMPORTER_HANDLER = "INVEON";

    private static final String UNKNOWN_SESSION_LABEL = "inveon_unknown";

    private final FileWriterWrapperI fw;
    private final InputStream in;
    private final UserI user;
    private final Map<String, Object> params;
    private final Format format;
    private final Date uploadDate;
    private final Set<String> uris;

    public InveonImporter(final Object listenerControl,
                          final UserI user,
                          final FileWriterWrapperI fw,
                          final Map<String, Object> params) throws IOException {
        super(listenerControl, user);
        this.user = user;
        this.params = params;
        this.in = fw.getInputStream();
        this.fw = fw;
        this.format = Format.getFormat(fw.getName());
        this.uploadDate = new Date();
        this.uris = Sets.newLinkedHashSet();
    }

    @Override
    public List<String> call() throws ClientException, ServerException {
        log.info("Importing Inveon data for user {}, upload date {}, file name {}",
                 user.getLogin(), uploadDate, fw.getName());
        log.debug("Importing Inveon data with parameters: {}", params);

        // TODO - Steve - Implement the InveonImporter. See BliImporter for an example.
        log.error("InveonImporter not implemented yet");

        return new ArrayList<>();
    }

}
