package gov.mil.otc._3dvis.datamodel;

import javafx.scene.paint.Color;

public class TrackingAttribute {

    private boolean modified = false;
    private boolean enabled = false;
    private int cutoff = 3600000;
    private Color color = Color.YELLOW;
    private boolean drawVerticals = false;

    public boolean isModified() {
        return modified;
    }

    public void resetModifiedFlag() {
        modified = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        modified = true;
    }

    public int getCutoff() {
        return cutoff;
    }

    public void setCutoff(int cutoff) {
        this.cutoff = cutoff;
        modified = true;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        modified = true;
    }

    public boolean isDrawVerticals() {
        return drawVerticals;
    }

    public void setDrawVerticals(boolean drawVerticals) {
        this.drawVerticals = drawVerticals;
        modified = true;
    }
}
