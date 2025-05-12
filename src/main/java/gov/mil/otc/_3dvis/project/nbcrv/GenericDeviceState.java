package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

import java.util.HashMap;
import java.util.Map;

public class GenericDeviceState extends TimedData {

    private final Map<String, String> valueMap = new HashMap<>();

    public GenericDeviceState(long timestamp, Map<String, String> valueMap) {
        super(timestamp);

        this.valueMap.putAll(valueMap);
    }

    public String[] getFieldNames() {
        return valueMap.keySet().toArray(new String[0]);
    }

    public String getValue(String fieldName) {
        return valueMap.get(fieldName);
    }
}
