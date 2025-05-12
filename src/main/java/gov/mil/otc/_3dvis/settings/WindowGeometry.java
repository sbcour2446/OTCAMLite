package gov.mil.otc._3dvis.settings;

import com.google.gson.annotations.SerializedName;

/**
 * Serializes and deserializes window geometries for preservation across power cycles.
 */
public class WindowGeometry {
    @SerializedName("isMaximized")
    private Boolean maximized;
    @SerializedName("xLocation")
    private Double x;
    @SerializedName("yLocation")
    private Double y;
    @SerializedName("windowWidth")
    private Double width;
    @SerializedName("windowHeight")
    private Double height;

    /**
     * Gets whether the window was maximized.
     *
     * @return True if maximized, false otherwise.
     */
    public Boolean isMaximized() {
        return maximized;
    }

    /**
     * Sets the maximization state.
     *
     * @param maximized The new maximization state.
     */
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    /**
     * Gets the last X location of the window.
     *
     * @return The last x location of the window.
     */
    public Double getX() {
        return x;
    }

    /**
     * Sets the X location.
     *
     * @param x The new X location.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets the last Y location of the window.
     *
     * @return The last Y location of the window.
     */
    public Double getY() {
        return y;
    }

    /**
     * Sets the Y location of the window.
     *
     * @param y The new Y location of the window.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Gets the width of the window.
     *
     * @return The width of the window.
     */
    public Double getWidth() {
        return width;
    }

    /**
     * Sets the width of the window.
     *
     * @param width The width of the window.
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Gets the height of the window.
     *
     * @return The height of the window.
     */
    public Double getHeight() {
        return height;
    }

    /**
     * Sets the height of the window.
     *
     * @param height The height of the window.
     */
    public void setHeight(double height) {
        this.height = height;
    }
}
