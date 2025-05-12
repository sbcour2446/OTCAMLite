package gov.mil.otc._3dvis.ui.utility;

import gov.mil.otc._3dvis.settings.SettingsManager;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

import java.net.URL;

/**
 * Checks the current preference for dark mode and applies the theme to a scene.
 */
public class ThemeHelper {

    public static final String DARK_MODE = "DarkModeEnabled";

    /**
     * Applies a theme to a scene.
     *
     * @param scene The scene to apply the theme to.
     */
    public static void applyTheme(Scene scene) {
        boolean isDarkModeEnabled = SettingsManager.getSettings().getBoolean(DARK_MODE, true);
        if (isDarkModeEnabled) {
            URL url = ThemeHelper.class.getResource("/css/dark_theme.css");
            if (url != null) {
                String css = url.toExternalForm();
                scene.getRoot().getStylesheets().add(css);
            }
        }
    }

    public static void applyTheme(Alert alert) {
        boolean isDarkModeEnabled = SettingsManager.getSettings().getBoolean(DARK_MODE, true);
        if (isDarkModeEnabled) {
            URL url = ThemeHelper.class.getResource("/css/dark_theme.css");
            if (url != null) {
                String css = url.toExternalForm();
                alert.getDialogPane().getStylesheets().add(css);
                alert.getDialogPane().getStyleClass().add("alert");
            }
        }
    }

    /**
     * Applies a theme to the images based on user preference.
     *
     * @param imageView The image view to apply the theme to.
     */
    public static void applyImageTheme(ImageView imageView) {
        boolean isDarkModeEnabled = SettingsManager.getSettings().getBoolean(DARK_MODE, true);
        if (isDarkModeEnabled) {
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setHue(-180);
            colorAdjust.setBrightness(100);
            colorAdjust.setSaturation(-100);

            imageView.setEffect(colorAdjust);
        }
    }

    private ThemeHelper() {
    }
}
