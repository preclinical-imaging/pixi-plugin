package org.nrg.xnatx.plugins.pixi.cmo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.nrg.xnatx.plugins.pixi.cmo.CMOUtils;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
public class PdxPojo implements Serializable {
    String passageNumber;
    String sourceId;
    String engraftmentSite = CMOUtils.NOT_PROVIDED;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PdxPojo)) return false;
        PdxPojo other = (PdxPojo) obj;
        return this.passageNumber.equals(other.passageNumber) && this.sourceId.equals(other.sourceId) && this.engraftmentSite.equals(other.engraftmentSite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passageNumber, sourceId, engraftmentSite);
    }

    @Override
    public String toString() {
        return "PassageNumber: " + passageNumber
                + "Source Id: " + sourceId
                + "Engraftment Site: " + engraftmentSite;
    }
}
