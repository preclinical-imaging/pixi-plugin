package org.apache.turbine.app.xnat.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.exceptions.InvalidSearchException;
import org.nrg.xdat.turbine.modules.actions.DisplaySearchAction;
import org.nrg.xft.security.UserI;

import java.net.URLDecoder;

@Slf4j
public class BioDSummaryStats extends DisplaySearchAction {
    @Override
    public void doPerform(final RunData data, final Context context) {
        final UserI user = getUser();
        try {
            final String rawSearchXml = data.getParameters().getString("search_xml");
            if (StringUtils.isBlank(rawSearchXml)) {
                data.setMessage("Your search result has expired.  Please resubmit your query. ");
                data.setScreenTemplate("Error.vm");
                return;
            }

            final String searchXml   = StringUtils.replace(URLDecoder.decode(RegExUtils.replaceAll(rawSearchXml,
                    "%", "%25"), "UTF-8"), ".close.", "/");
            context.put("xss", searchXml);
            super.doPreliminaryProcessing(data, context);
            data.setScreenTemplate(getScreenTemplate(data));
            doFinalProcessing(data, context);
        } catch (SearchTimeoutException e) {
            log.error("A search by user {} appeared to time out", user.getUsername(), e);
            data.setMessage(e.getMessage());
            data.setScreenTemplate("Index.vm");
        } catch (IllegalAccessException e) {
            data.setMessage("The user does not have access to this data.");
            data.setScreenTemplate("Error.vm");
            data.getParameters().setString("exception", e.toString());
        } catch (InvalidSearchException e) {
            data.setMessage("You specified an invalid search condition: " + e.getMessage());
            data.setScreenTemplate("Error.vm");
        } catch (Exception e) {
            error(e, data);
        }
    }

    @Override
    public String getScreenTemplate(final RunData data) {
        return DEFAULT_SCREEN_TEMPLATE;
    }

    private static final String DEFAULT_SCREEN_TEMPLATE = "Xnat_Summary_Statistics_Dashboard.vm";


    private static final String START_TAG               = "<xdat:search_where";
    private static final String END_TAG                 = "</xdat:search_where>";
    private static final String PROJECT_HEADER          = "Project";
}
