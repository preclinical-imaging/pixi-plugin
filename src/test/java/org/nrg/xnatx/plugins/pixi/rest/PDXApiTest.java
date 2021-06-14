package org.nrg.xnatx.plugins.pixi.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.entities.PDX;
import org.nrg.xnatx.plugins.pixi.services.PDXService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PDXApiTest {

    PDXService pdxService;
    UserManagementServiceI userManagementService;
    RoleHolder roleHolder;
    PDXApi pdxApi;

    @BeforeEach
    public void setup() {
        pdxService = mock(PDXService.class);
        userManagementService = mock(UserManagementServiceI.class);
        roleHolder = mock(RoleHolder.class);
        pdxApi = new PDXApi(userManagementService, roleHolder, pdxService);
    }

    @Test
    public void testGetPDXs() {
        PDX pdx1 = new PDX();
        PDX pdx2 = new PDX();
        pdx1.setPdxID("WUXNAT01");
        pdx2.setPdxID("WUXNAT02");

        List<PDX> pdxs = new ArrayList<>();
        pdxs.add(pdx1);
        pdxs.add(pdx2);

        when(pdxService.getAll()).thenReturn(pdxs);

        assertEquals(pdxApi.getPDXs(), pdxs);
    }

    @Test
    public void testCreatePDX() {
        PDX pdx = new PDX();

        when(pdxService.create(pdx)).thenReturn(pdx);

        try {
            assertEquals(pdx, pdxApi.createPDX(pdx));
        } catch (ResourceAlreadyExistsException e) {
            fail("Exception should not be thrown. Resource does not already exist");
        }
    }

    @Test
    public void testCreateExistingPDX() {
        PDX pdx = new PDX();
        String pdxID = "WUXNAT01";
        pdx.setPdxID(pdxID);

        when(pdxService.exists("pdxID", pdxID)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> pdxApi.createPDX(pdx));
    }

    @Test
    public void testGetKnownPDX() {
        PDX pdx = new PDX();
        String pdxID = "WUXNAT01";
        pdx.setPdxID(pdxID);

        when(pdxService.findByPdxID(pdxID)).thenReturn(Optional.of(pdx));

        try {
            assertEquals(pdx, pdxApi.getPDX(pdxID));
        } catch (NotFoundException e) {
            fail("Exception should not be thrown. PDX should be found.");
        }
    }

    @Test
    public void testGetUnknownPDX() {
        String pdxID = "WUXNAT01";

        when(pdxService.findByPdxID(pdxID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pdxApi.getPDX(pdxID));
    }

    @Test
    public void testPutPDXWithIDMismatch() {
        PDX pdx = new PDX();
        String pdxID = "WUXNAT01";
        assertThrows(DataFormatException.class, () -> pdxApi.updatePDX(pdxID, pdx));
    }

    @Test
    public void testPutPDXCreation() {
        PDX pdx = new PDX();
        String pdxID = "WUXNAT01";
        pdx.setPdxID(pdxID);

        when(pdxService.exists(eq("pdxID"), anyString())).thenReturn(false);

        try {
            pdxApi.updatePDX(pdxID, pdx);
            verify(pdxService).create(pdx);
            verify(pdxService, never()).update(pdx);
        } catch (DataFormatException e) {
            fail("Exception should not be thrown. pdxIDs match.");
        }
    }

    @Test
    public void testPutPDXUpdate() {
        PDX pdx = new PDX();
        String pdxID = "WUXNAT01";
        pdx.setPdxID(pdxID);

        when(pdxService.exists(eq("pdxID"), anyString())).thenReturn(true);

        try {
            pdxApi.updatePDX(pdxID, pdx);
            verify(pdxService, never()).create(pdx);
            verify(pdxService).update(pdx);
        } catch (DataFormatException e) {
            fail("Exception should not be thrown. pdxIDs match.");
        }
    }

    @Test
    public void testDeleteUnknownPDX() {
        String pdxID = "WUXNAT01";

        when(pdxService.findByPdxID(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pdxApi.deletePDX(pdxID));
    }

    @Test
    public void testDeleteKnownPDX() {
        PDX pdx = new PDX();
        String pdxID = "WUXNAT01";
        pdx.setPdxID(pdxID);

        when(pdxService.findByPdxID(pdxID)).thenReturn(Optional.of(pdx));

        try {
            pdxApi.deletePDX(pdxID);
            verify(pdxService).delete(pdx);
        } catch (NotFoundException e) {
            fail("Exception should not be thrown. PDX is known and should be deleted");
        }
    }

}