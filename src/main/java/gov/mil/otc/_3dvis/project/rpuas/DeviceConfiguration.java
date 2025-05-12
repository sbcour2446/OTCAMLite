package gov.mil.otc._3dvis.project.rpuas;

import com.google.gson.annotations.SerializedName;
import gov.mil.otc._3dvis.datamodel.Affiliation;

import java.util.Objects;

public class DeviceConfiguration {

    private final transient String missionName;

    @SerializedName("deviceId")
    private final String deviceId;

    @SerializedName("affiliation")
    private final Affiliation affiliation;

    @SerializedName("operatorId")
    private final int operatorId;

    public DeviceConfiguration(String missionName, String deviceId, Affiliation affiliation, int operatorId) {
        this.missionName = missionName;
        this.deviceId = deviceId;
        this.affiliation = affiliation;
        this.operatorId = operatorId;
    }

    public String getMissionName() {
        return missionName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public int getOperatorId() {
        return operatorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceConfiguration deviceConfiguration = (DeviceConfiguration) o;
        return missionName.equals(deviceConfiguration.missionName) &&
                deviceId.equals(deviceConfiguration.deviceId) &&
                affiliation.equals(deviceConfiguration.affiliation) &&
                operatorId == deviceConfiguration.operatorId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(missionName, deviceId, affiliation, operatorId);
    }
}
