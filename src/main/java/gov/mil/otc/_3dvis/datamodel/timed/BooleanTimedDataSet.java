package gov.mil.otc._3dvis.datamodel.timed;

public class BooleanTimedDataSet extends TimedDataSet<BooleanTimedData> {

    /**
     * Add data item to the set if the value has changed from the previous item in list.
     *
     * @param booleanTimedData The data item.
     */
    public void addIfChange(BooleanTimedData booleanTimedData) {
        // Search list to place new item, assuming most data is added to the end of the list.
        synchronized (indexedList) {
            int index = indexedList.size() - 1;
            if (indexedList.isEmpty()) {
                indexedList.add(booleanTimedData);
            } else if (indexedList.get(index).getTimestamp() < booleanTimedData.getTimestamp()) {
                if (indexedList.get(index).isValue() != booleanTimedData.isValue()) {
                    indexedList.add(booleanTimedData);
                }
            } else if (indexedList.get(index).getTimestamp() != booleanTimedData.getTimestamp()) {
                while (index > 0 && indexedList.get(index).getTimestamp() > booleanTimedData.getTimestamp()) {
                    index--;
                }
                if (indexedList.get(index).getTimestamp() != booleanTimedData.getTimestamp()) {
                    if (indexedList.get(index).getTimestamp() < booleanTimedData.getTimestamp()) {
                        if (indexedList.get(index).isValue() != booleanTimedData.isValue()) {
                            indexedList.add(index + 1, booleanTimedData);
                        }
                    } else {
                        indexedList.add(index, booleanTimedData);
                    }
                }
            }
        }
    }
}
