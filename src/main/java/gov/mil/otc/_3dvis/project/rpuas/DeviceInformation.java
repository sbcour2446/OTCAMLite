package gov.mil.otc._3dvis.project.rpuas;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class DeviceInformation {

    @SerializedName("deviceId")
    private final String deviceId;

    @SerializedName("vendor")
    private final String vendor;

    public DeviceInformation(String deviceId, String vendor) {
        this.deviceId = deviceId;
        this.vendor = vendor;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getVendor() {
        return vendor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceInformation deviceConfiguration = (DeviceInformation) o;
        return deviceId.equals(deviceConfiguration.deviceId) &&
                vendor.equals(deviceConfiguration.vendor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, vendor);
    }
}
