package org.nrg.xnatx.plugins.pixi.inveon.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class InveonSessionFiles {

    private String sessionLabel;
    private String timeStamp;
    private Map<String, InveonImageRepresentation> inveonImageMap;

    public void putInveonImageRepresentation(String name, InveonImageRepresentation inveonImageRepresentation) {
        if (inveonImageMap == null) {
            inveonImageMap = new HashMap<>();
        }
        inveonImageMap.put(name, inveonImageRepresentation);
    }

    public InveonImageRepresentation getInveonImageRepresentation(String name) {
        InveonImageRepresentation rtn = null;
        if (inveonImageMap != null) {
            rtn = inveonImageMap.get(name);
        }
        return rtn;
    }
}
