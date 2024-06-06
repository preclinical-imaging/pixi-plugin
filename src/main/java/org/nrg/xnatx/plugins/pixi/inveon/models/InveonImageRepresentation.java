package org.nrg.xnatx.plugins.pixi.inveon.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class InveonImageRepresentation {
    private String name;
    private String headerFileName;
    private String pixelFileName;
    private String prearchiveTempFolder;
    private String prearchiveTimestampPath;
    private String timestamp;
    private String modality;
    private int    index;
    private Map<String, String> headerMap;
    private List<String> headerUnmappedLines;

    public void putHeaderValue(String key, String value) {
        if (headerMap == null) {
            headerMap = new HashMap<>();
        }
        headerMap.put(key, value);
    }

    public String getHeaderValue(String key) {
        String rtn = "";
        if (headerMap == null) {
            headerMap = new HashMap<>();
        }
        if (headerMap.containsKey(key)) {
            rtn = headerMap.get(key);
        }
        return rtn;
    }

    public void addUnmappedLine(String line) {
        if (headerUnmappedLines == null) {
            headerUnmappedLines = new ArrayList<>();
        }
        headerUnmappedLines.add(line);
    }
}

