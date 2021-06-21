package org.nrg.xnatx.plugins.pixi.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.entities.PatientEntity;
import org.nrg.xnatx.plugins.pixi.entities.TumorEntity;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.models.Patient;
import org.nrg.xnatx.plugins.pixi.models.Tumor;
import org.nrg.xnatx.plugins.pixi.services.PDXEntityService;
import org.nrg.xnatx.plugins.pixi.services.PDXService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PDXServiceImplTest {

    private PDXEntityService pdxEntityService;
    private PDXService pdxService;

    @BeforeEach
    public void setup() {
        pdxEntityService = mock(PDXEntityService.class);
        pdxService = new PDXServiceImpl(pdxEntityService);
    }

    @Test
    public void testGetPDXWithNullPatientAndTumor() {
        String pdxID = "ID1";
        PDXEntity pdxEntity = new PDXEntity();
        pdxEntity.setPdxID("ID1");

        when(pdxEntityService.getPDXEntity(pdxID)).thenReturn(Optional.of(pdxEntity));

        PDX pdx = pdxService.getPDX(pdxID).get();
        assertEquals(pdxID, pdx.getPdxID());
        assertNull(pdx.getPatient());
        assertNull(pdx.getTumor());
    }

    @Test
    public void testGetPDXWithPatientAndTumor() {
        String pdxID = "ID1";
        PDXEntity pdxEntity = new PDXEntity();
        pdxEntity.setPdxID(pdxID);

        PatientEntity patientEntity = new PatientEntity();
        Integer age = 1;
        patientEntity.setAge(age);
        pdxEntity.setPatientEntity(patientEntity);

        TumorEntity tumorEntity = new TumorEntity();
        String tumorType = "Primary";
        tumorEntity.setTumorType(tumorType);
        pdxEntity.setTumorEntity(tumorEntity);

        when(pdxEntityService.getPDXEntity(pdxID)).thenReturn(Optional.of(pdxEntity));

        PDX pdx = pdxService.getPDX(pdxID).get();
        assertEquals(pdxID, pdx.getPdxID());
        assertNotNull(pdx.getPatient());
        assertEquals(age, pdx.getPatient().getAge());
        assertNotNull(pdx.getTumor());
        assertEquals(tumorType, pdx.getTumor().getTumorType());
    }

    @Test
    public void testGetAllReturnsNothing() {
        when(pdxEntityService.getAll()).thenReturn(Collections.emptyList());
        assertEquals(0, pdxService.getAllPDX().size());
    }

    @Test
    public void testGeAll() {
        String pdxID1 = "ID1";
        PDXEntity pdxEntity1 = new PDXEntity();
        pdxEntity1.setPdxID(pdxID1);

        PatientEntity patientEntity = new PatientEntity();
        Integer age = 1;
        patientEntity.setAge(age);
        pdxEntity1.setPatientEntity(patientEntity);

        TumorEntity tumorEntity = new TumorEntity();
        String tumorType = "Primary";
        tumorEntity.setTumorType(tumorType);
        pdxEntity1.setTumorEntity(tumorEntity);

        String pdxID2 = "ID2";
        PDXEntity pdxEntity2 = new PDXEntity();
        pdxEntity2.setPdxID(pdxID2);

        when(pdxEntityService.getAll()).thenReturn(Arrays.asList(pdxEntity1, pdxEntity2));

        List<PDX> pdxs = pdxService.getAllPDX();
        assertEquals(pdxID1, pdxs.get(0).getPdxID());
        assertEquals(age, pdxs.get(0).getPatient().getAge());
        assertEquals(tumorType, pdxs.get(0).getTumor().getTumorType());
        assertEquals(pdxID2, pdxs.get(1).getPdxID());
    }

    @Test
    public void testCreateExistingPDX() {
        String pdxID = "ID1";
        PDX pdx = PDX.builder().pdxID(pdxID).build();

        when(pdxEntityService.pdxEntityExists(pdxID)).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class, () -> pdxService.createPDX(pdx));
    }

    @Test
    public void testCreateNewPDX() {
        String pdxID = "ID1";
        PDX pdx = PDX.builder().pdxID(pdxID).build();

        when(pdxEntityService.pdxEntityExists(pdxID)).thenReturn(false);

        try {
            pdxService.createPDX(pdx);
            verify(pdxEntityService).create(Mockito.any(PDXEntity.class));
        } catch (ResourceAlreadyExistsException e) {
            fail("Resource does not exist, should not fail.");
        }
    }

    @Test
    public void testUpdatePDXNotFound() {
        when(pdxEntityService.getPDXEntity(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> pdxService.updatePDX(new PDX()));
    }

    @Test
    public void testUpdateKnownPDX() {
        String pdxID = "ID1";
        PDXEntity pdxEntity = new PDXEntity();
        pdxEntity.setPdxID(pdxID);

        Patient patient = Patient.builder().age(12).build();
        Tumor tumor = Tumor.builder().tumorGrade("Primary").build();
        PDX pdx = PDX.builder().pdxID(pdxID).patient(patient).tumor(tumor).build();

        when(pdxEntityService.getPDXEntity(any())).thenReturn(Optional.of(pdxEntity));

        try {
            pdxService.updatePDX(pdx);
            verify(pdxEntityService).update(Mockito.any(PDXEntity.class));
        } catch (NotFoundException e) {
            fail("Resource does exist, should not fail.");
        }
    }

    @Test
    public void testDeleteNotFound() {
        when(pdxEntityService.getPDXEntity(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> pdxService.updatePDX(new PDX()));
    }

    @Test
    public void testDelete() {
        String pdxID = "ID1";
        PDXEntity pdxEntity = new PDXEntity();
        pdxEntity.setPdxID(pdxID);

        when(pdxEntityService.getPDXEntity(any())).thenReturn(Optional.of(pdxEntity));

        try {
            pdxService.deletePDX(pdxID);
            verify(pdxEntityService).delete(pdxEntity);
        } catch (NotFoundException e) {
            fail("Resource does exist, should not fail.");
        }
    }
}