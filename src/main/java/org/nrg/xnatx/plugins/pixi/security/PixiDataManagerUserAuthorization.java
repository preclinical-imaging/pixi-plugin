package org.nrg.xnatx.plugins.pixi.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.nrg.xapi.authorization.AbstractXapiAuthorization;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.PixiUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Checks whether user has a ContainerManager Role.
 */
@Slf4j
@Component
public class PixiDataManagerUserAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        return Roles.checkRole(user, PixiUtils.PIXI_DATA_MANAGER_ROLE) || ((XDATUser) user).isSiteAdmin();
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}


