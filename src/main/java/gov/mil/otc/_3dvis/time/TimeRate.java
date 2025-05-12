package gov.mil.otc._3dvis.time;

public enum TimeRate {

    SPEED_EIGHTH(".125X", .125),
    SPEED_QUARTER(".25X", .25),
    SPEED_HALF(".5X", .5),
    SPEED_NORMAL("1X", 1.0),
    SPEED_2X("2X", 2.0),
    SPEED_4X("4X", 4.0),
    SPEED_8X("8X", 8.0),
    SPEED_16X("16X", 16.0),
    SPEED_32X("32X", 32.0),
    SPEED_100X("100X", 100.0),
    SPEED_200X("200X", 200.0),
    SPEED_1000X("1000X", 1000.0);
    private final String display;
    private final double value;

    TimeRate(String display, double value) {
        this.display = display;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public TimeRate increase() {
        return ordinal() < values().length - 1 ? TimeRate.values()[ordinal() + 1] : TimeRate.values()[TimeRate.values().length - 1];
    }

    public TimeRate decrease() {
        return ordinal() > 0 ? TimeRate.values()[ordinal() - 1] : TimeRate.values()[0];
    }

    @Override
    public String toString() {
        return display;
    }
}
