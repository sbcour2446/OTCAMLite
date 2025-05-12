package gov.mil.otc._3dvis.ui.tools.rangefinder;

enum DisplayType {
    BY_ID("Display by ID"),
    BY_NAME("Display by Name"),
    BY_DESCRIPTION("Display by Description");
    final String description;

    DisplayType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
