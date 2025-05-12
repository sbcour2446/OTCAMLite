package gov.mil.otc._3dvis.time;

public enum PlaySpeed {
    SPEED_EIGHTH(".125X", .125),
    SPEED_QUARTER(".25X", .25),
    SPEED_HALF(".5X", .5),
    SPEED_NORMAL("1X", 1.0),
    SPEED_2X("2X", 2.0),
    SPEED_4X("4X", 4.0),
    SPEED_8X("8X", 8.0),
    SPEED_16X("16X", 16.0),
    SPEED_32X("32X", 32.0),
    SPEED_64X("64X", 64.0),
    SPEED_128X("128X", 128.0);
    private final String display;
    private final double value;

    PlaySpeed(String display, double value) {
        this.display = display;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return display;
    }
}
