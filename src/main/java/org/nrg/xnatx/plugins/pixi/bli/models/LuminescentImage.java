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

    private String luminescentImageFileName;

    @JsonFormat(pattern = "EEEE, MMMM dd, yyyy")
    private LocalDate acquisitionDate;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime acquisitionTime;

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
