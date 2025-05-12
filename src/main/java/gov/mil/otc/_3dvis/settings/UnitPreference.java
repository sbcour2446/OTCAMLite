package gov.mil.otc._3dvis.settings;

import com.google.gson.annotations.SerializedName;

public class UnitPreference {

    public enum PositionUnit {
        LAT_LON_DD("Lat/Lon (decimal degrees)"),
        MGRS("MGRS"),
        UTM("UTM");
        final String description;

        PositionUnit(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @SerializedName("position unit")
    private PositionUnit positionUnit;

    public PositionUnit getPositionUnit() {
        if (positionUnit == null) {
            positionUnit = PositionUnit.LAT_LON_DD;
        }
        return positionUnit;
    }

    public void setPositionUnit(PositionUnit positionUnit) {
        this.positionUnit = positionUnit;
    }
}
