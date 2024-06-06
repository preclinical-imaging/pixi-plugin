package org.nrg.xnatx.plugins.pixi.bli.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class ClickInfo {

    private ClickNumber clickNumber;
    private CameraSystemInfo cameraSystemInfo;

}
