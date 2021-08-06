package org.nrg.xnatx.plugins.pixi.rest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.services.AnimalModelService;
import org.nrg.xnatx.plugins.pixi.services.PDXEntityService;
import org.nrg.xnatx.plugins.pixi.services.PDXService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PIXIApiTest {

    private PDXService pdxService;
    private PDXEntityService pdxEntityService;
    private AnimalModelService animalModelService;
    private UserManagementServiceI userManagementService;
    private RoleHolder roleHolder;
    private PIXIApi PIXIApi;
    private static String username = "PDXUser";

    @BeforeAll
    public static void beforeAll() {
        UserI userI = mock(UserI.class);
        when(userI.getUsername()).thenReturn(username);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userI);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    public void beforeEach() {
        pdxService = mock(PDXService.class);
        pdxEntityService = mock(PDXEntityService.class);
        userManagementService = mock(UserManagementServiceI.class);
        roleHolder = mock(RoleHolder.class);
        animalModelService = mock(AnimalModelService.class);
        PIXIApi = new PIXIApi(userManagementService, roleHolder, pdxService, animalModelService);
    }

    @Test
    public void testGetPDXs() {
        PDX pdx1 = new PDX();
        PDX pdx2 = new PDX();
        pdx1.setId("WUXNAT01");
        pdx2.setId("WUXNAT02");

        List<PDX> pdxs = new ArrayList<>();
        pdxs.add(pdx1);
        pdxs.add(pdx2);

        when(pdxService.getAllPDX()).thenReturn(pdxs);

        assertEquals(PIXIApi.getAllPDX(), pdxs);
    }

    @Test
    public void testCreatePDX() {
        PDX pdx = new PDX();

        try {
            PIXIApi.createPDX(pdx);
            verify(pdxService).createPDX(pdx);
            assertEquals(username, pdx.getCreatedBy());
        } catch (ResourceAlreadyExistsException e) {
            fail("Exception should not be thrown. Resource does not already exist");
        }
    }

    @Test
    public void testGetKnownPDX() {
        String pdxID = "WUXNAT01";
        PDX pdx = PDX.builder().id(pdxID).build();

        when(pdxService.getPDX(pdxID)).thenReturn(Optional.of(pdx));

        try {
            assertEquals(pdx, PIXIApi.getPDX(pdxID));
        } catch (NotFoundException e) {
            fail("Exception should not be thrown. PDX should be found.");
        }
    }

    @Test
    public void testGetUnknownPDX() {
        String pdxID = "WUXNAT01";

        when(pdxService.getPDX(pdxID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> PIXIApi.getPDX(pdxID));
    }

    @Test
    public void testPutPDXWithIDMismatch() {
        PDX pdx = PDX.builder().id("junk").build();
        String pdxID = "WUXNAT01";
        assertThrows(DataFormatException.class, () -> PIXIApi.createOrUpdatePDX(pdxID, pdx));
    }

    @Test
    public void testPutPDXCreation() {
        String pdxID = "WUXNAT01";
        PDX pdx = PDX.builder().id(pdxID).build();

        try {
            PIXIApi.createOrUpdatePDX(pdxID, pdx);
            verify(pdxService).createPDX(pdx);
            verify(pdxService, never()).updatePDX(pdx);
        } catch (DataFormatException | NotFoundException | ResourceAlreadyExistsException e) {
            fail("Exception should not be thrown.");
        }
    }

    @Test
    public void testPutPDXUpdate() {
        String pdxID = "WUXNAT01";
        PDX pdx = PDX.builder().id(pdxID).build();

        try {
            doThrow(ResourceAlreadyExistsException.class).when(pdxService).createPDX(any());
            PIXIApi.createOrUpdatePDX(pdxID, pdx);
            verify(pdxService).createPDX(pdx);
            verify(pdxService).updatePDX(pdx);
        } catch (DataFormatException | NotFoundException | ResourceAlreadyExistsException e) {
            fail("Exception should not be thrown.");
        }
    }

    @Test
    public void testDeletePDX() {
        String pdxID = "WUXNAT01";

        try {
            PIXIApi.deletePDX(pdxID);
            verify(pdxService).deletePDX(pdxID);
        } catch (NotFoundException e) {
            fail("Exception should not be thrown.");
        }
    }
}