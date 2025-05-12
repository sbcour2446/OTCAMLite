package gov.mil.otc._3dvis.settings;

public enum IconType {
    PLATFORM(1, 50, 25),
    LIFE_FORM(1, 50, 25),
    MUNITION(1, 50, 12),
    PIN(1, 50, 12),
    SQUARE(1, 50, 12),
    OTHER(1, 50, 25);
    final int minimumSize;
    final int maximumSize;
    final int defaultSize;

    IconType(int minimumSize, int maximumSize, int defaultSize) {
        this.minimumSize = minimumSize;
        this.maximumSize = maximumSize;
        this.defaultSize = defaultSize;
    }

    public int getMinimumSize() {
        return minimumSize;
    }

    public int getMaximumSize() {
        return maximumSize;
    }

    public int getDefaultSize() {
        return defaultSize;
    }
}
