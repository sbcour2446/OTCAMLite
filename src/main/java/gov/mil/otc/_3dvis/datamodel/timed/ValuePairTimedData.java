package gov.mil.otc._3dvis.datamodel.timed;

import java.util.HashMap;
import java.util.Map;

public class ValuePairTimedData extends TimedData {

    private final Map<String, String> valueMap;

    public ValuePairTimedData(long timestamp, Map<String, String> valueMap) {
        super(timestamp);
        this.valueMap = valueMap;
    }

    public Map<String, String> getValueMap() {
        return valueMap;
    }
}
