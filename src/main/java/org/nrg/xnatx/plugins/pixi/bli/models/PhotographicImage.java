package org.nrg.xnatx.plugins.pixi.bli.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
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
public class PhotographicImage {

    private String photographicImage;
    @JsonFormat(pattern = "EEEE, MMMM dd, yyyy") private LocalDate acquisitionDate;
    @JsonFormat(pattern = "HH:mm:ss") private LocalTime acquisitionTime;
    private Long acquisitionSeconds;

    @JsonIgnore
    @Nullable
    public LocalDateTime getLocalAcquisitionDateTime() {
        if (getAcquisitionDate() == null || getAcquisitionTime() == null) {
            return null;
        }

        LocalDate localScanDate = this.getAcquisitionDate();
        LocalTime localScanTime = this.getAcquisitionTime();
        return LocalDateTime.of(localScanDate, localScanTime);
    }

    @JsonIgnore
    @Nullable
    public Date getAcquisitionDateTime() {
        if (getAcquisitionDate() == null || getAcquisitionTime() == null) {
            return null;
        }

        return Date.from(getLocalAcquisitionDateTime().atZone(ZoneId.systemDefault()).toInstant());
    }

}