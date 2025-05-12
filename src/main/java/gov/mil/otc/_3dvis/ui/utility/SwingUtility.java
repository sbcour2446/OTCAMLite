package gov.mil.otc._3dvis.ui.utility;

import javafx.scene.paint.Color;

public class SwingUtility {

    private SwingUtility() {
    }

    public static java.awt.Color toAwtColor(Color fxColor) {
        return new java.awt.Color(
                (float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue(),
                (float) fxColor.getOpacity());
    }

    public static Color toFxColor(java.awt.Color awtColor) {
        int r = awtColor.getRed();
        int g = awtColor.getGreen();
        int b = awtColor.getBlue();
        int a = awtColor.getAlpha();
        double opacity = a / 255.0;
        return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }
}
