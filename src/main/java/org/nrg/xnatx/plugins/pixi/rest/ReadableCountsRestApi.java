package org.nrg.xnatx.plugins.pixi.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Api("Readable Counts API")
@XapiRestController
@RequestMapping(value = "/readable-counts")
@Slf4j
public class ReadableCountsRestApi extends AbstractXapiRestController {

    private final GroupsAndPermissionsCache groupsAndPermissionsCache;

    public ReadableCountsRestApi(UserManagementServiceI userManagementService,
                                 RoleHolder roleHolder,
                                 GroupsAndPermissionsCache groupsAndPermissionsCache) {
        super(userManagementService, roleHolder);
        this.groupsAndPermissionsCache = groupsAndPermissionsCache;
    }

    @ApiOperation(value = "Returns a list of all Readable Counts.", response = Map.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Readable Counts successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET)
    public Map<String, Long> getAll() {
        Map<String, Long> counts = groupsAndPermissionsCache.getReadableCounts(getSessionUser());

        // count image session data
        int imageSessionCount = 0;
        for (String xsiType : counts.keySet()) {
            if (instanceOf(xsiType, "xnat:imageSessionData")) {
                imageSessionCount += counts.get(xsiType);
            }
        }

        // count image scan data
        int imageScanCount = 0;
        for (String xsiType : counts.keySet()) {
            if (instanceOf(xsiType, "xnat:imageScanData")) {
                imageScanCount += counts.get(xsiType);
            }
        }

        // New map, the cache map is immutable
        Map<String, Long> updatedCounts = new HashMap<>(counts);

        updatedCounts.put("xnat:imageSessionData", (long) imageSessionCount);
        updatedCounts.put("xnat:imageScanData", (long) imageScanCount);

        return updatedCounts;
    }

    private boolean instanceOf(final String xsiType, final String instanceOfXsiType) {
        try {
            return GenericWrapperElement.GetElement(xsiType).instanceOf(instanceOfXsiType);
        } catch (ElementNotFoundException e) {
            return false;
        } catch (XFTInitException e) {
            log.error("Unable to compare xsi types", e);
            throw new RuntimeException(e);
        }
    }
}
