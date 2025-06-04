package org.nrg.xnatx.plugins.pixi.cmo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PreClinicalReportEntry implements Serializable {

    PdxReportEntry pdxReportEntry;
    PreClinicalImagingReportEntry preClinicalOtherReportEntry;

}
