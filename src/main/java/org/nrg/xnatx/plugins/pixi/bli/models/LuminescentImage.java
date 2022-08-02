package org.nrg.xnatx.plugins.pixi.bli.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class LuminescentImage {

    private String luminescentImage;
    @JsonFormat(pattern = "EEEE, MMMM dd, yyyy") private LocalDate acquisitionDate;
    @JsonFormat(pattern = "HH:mm:ss") private LocalTime acquisitionTime;
    private Long acquisitionSeconds;
    private Integer pixelWidth;
    private Integer pixelHeight;
    private String imageUnits;
    private Integer binningFactor;
    private Integer luminescentExposureSeconds;
    private String luminescentExposureUnits;
    private Integer fNumber;
    private Integer fieldOfView;
    private Double readBiasLevel;
    private String emissionFilter;
    private Integer filterPosition;
    private String excitationFilter;
    private Double subjectSize;
    private Integer demandTemperature;
    private Integer measuredTemperature;
    private String errorWave;
    private Integer cosmic;
    private Integer backgroundCorrected;
    private Integer FlatFieldCorrected;

    @JsonIgnore
    public LocalDateTime getLocalAcquisitionDateTime() {
        LocalDate localScanDate = this.getAcquisitionDate();
        LocalTime localScanTime = this.getAcquisitionTime();
        return LocalDateTime.of(localScanDate, localScanTime);
    }

    @JsonIgnore
    public Date getAcquisitionDateTime() {
        return Date.from(getLocalAcquisitionDateTime().atZone(ZoneId.systemDefault()).toInstant());
    }
}
