package org.nrg.xnatx.plugins.pixi.rest;

import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.entities.XenograftEntity;
import org.nrg.xnatx.plugins.pixi.models.Xenograft;
import org.nrg.xnatx.plugins.pixi.services.XenograftService;

import java.util.List;

public abstract class XenograftAPI<E extends XenograftEntity, T extends Xenograft> extends AbstractXapiRestController {

    private final XenograftService<E, T> xenograftService;
    private final Class<T> type;

    public XenograftAPI(final UserManagementServiceI userManagementService,
                        final RoleHolder roleHolder,
                        final XenograftService<E, T> xenograftService,
                        final Class<T> type) {
        super(userManagementService, roleHolder);
        this.xenograftService = xenograftService;
        this.type = type;
    }

    public List<T> getAll() {
        return xenograftService.getAllXenografts();
    }

    public T get(final String id) throws NotFoundException {
        return xenograftService.getXenograft(id)
                .orElseThrow(() -> new NotFoundException(type.getSimpleName(), id));
    }

    public void create(final T t) throws ResourceAlreadyExistsException {
        if (xenograftService.xenograftExists(t.getExternalID())) {
            throw new ResourceAlreadyExistsException(type.getSimpleName(), t.getExternalID());
        }

        t.setCreatedBy(getSessionUser().getUsername());
        xenograftService.createXenograft(t);
    }

    public void update(final String id, final T t) throws DataFormatException, NotFoundException {
        if (!id.equals(t.getExternalID())) {
            throw new DataFormatException("DataFormatException: Updates to ID are not allowed");
        }
        try {
            xenograftService.updateXenograft(t);
        } catch (org.nrg.framework.exceptions.NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    public void delete(final String id) {
        xenograftService.deleteXenograft(id);
    }

}
