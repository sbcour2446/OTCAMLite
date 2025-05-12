package gov.mil.otc._3dvis.datamodel.timed;

public class GenericTimedData<T> extends TimedData {

    private final T data;

    public GenericTimedData(long timestamp, T data) {
        super(timestamp);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
