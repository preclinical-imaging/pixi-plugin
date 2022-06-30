package org.nrg.xnatx.plugins.pixi.bli.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class UserLabelNameSet {

    private String userLabelNameSet;

    private String user;
    private String group;
    private String experiment;
    private String comment1;
    private String comment2;
    private String timePoint;
    private String animalNumber;
    private String animalStrain;
    private String animalModel;
    private String sex;
    private String view;
    private String cellLine;
    private String reporter;
    private String treatment;
    private String lucInjectionTime; // No sample data with dates, cannot parse without format. Leaving as a string.
    private String iacucNumber;

}
