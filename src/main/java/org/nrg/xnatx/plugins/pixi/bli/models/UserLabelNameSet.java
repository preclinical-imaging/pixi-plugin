package org.nrg.xnatx.plugins.pixi.bli.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

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

    /**
     * Reflectively get the string value of a field.
     * @param field The field to get the value of.
     * @return The string value of the field, or null if the field is null.
     */
    public String get(String field) {
        if (field == null) {
            return null;
        }

        try {
            Field f = getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object value = f.get(this);
            return value != null ? value.toString() : null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error getting field value", e);
            return null;
        }
    }

}
