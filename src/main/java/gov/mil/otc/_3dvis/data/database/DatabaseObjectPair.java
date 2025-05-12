package gov.mil.otc._3dvis.data.database;

public class DatabaseObjectPair<T, S> {

    private final T object1;
    private final S object2;

    public DatabaseObjectPair(T object1, S object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    public T getObject1() {
        return object1;
    }

    public S getObject2() {
        return object2;
    }
}
