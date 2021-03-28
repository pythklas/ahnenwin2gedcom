package de.ng.ahnenwin2gedcom.gedcom;

import java.util.Objects;

class HusbandWifeRef {
    private final int husbandId;
    private final int wifeId;

    HusbandWifeRef(int husbandId, int wifeId) {
        this.husbandId = husbandId;
        this.wifeId = wifeId;
    }

    int getHusbandId() {
        return husbandId;
    }

    int getWifeId() {
        return wifeId;
    }

    boolean noParents() {
        return husbandId == 0 && wifeId == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HusbandWifeRef that = (HusbandWifeRef) o;
        return husbandId == that.husbandId && wifeId == that.wifeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(husbandId, wifeId);
    }

    @Override
    public String toString() {
        return String.format("HusbandWifeRef{husbandId=%s, wifeId=%s}", husbandId, wifeId);
    }
}
