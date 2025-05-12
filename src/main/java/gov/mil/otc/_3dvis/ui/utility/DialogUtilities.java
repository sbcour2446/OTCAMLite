package gov.mil.otc._3dvis.ui.utility;

import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * Helper utility to present dialogs to the end user.
 */
@SuppressWarnings("unused")
public class DialogUtilities {

    public static final String INVALID_ENTRY = "Invalid Entry";
    private static final String TRANSPARENT_STYLE = "transparent";

    /**
     * Possible user results.
     */
    public enum UserResult {
        /**
         * User acceptance result.
         */
        YES,
        /**
         * User denial result.
         */
        NO,
        /**
         * User cancel result.
         */
        CANCEL
    }

    /**
     * Default private constructor.
     */
    private DialogUtilities() {
        // Default private constructor.
    }

    /**
     * Shows a non-blocking warning dialog.
     *
     * @param title       The title to display.
     * @param message     The message to display.
     * @param blocking    True if the alert dialog should block the UI thread until user action is performed.
     * @param parentStage The parent stage.
     */
    public static void showWarningDialog(String title, String message, boolean blocking, Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageLoader.getLogo());
        Text text = new Text(message);
        text.setWrappingWidth(300);
        text.getStyleClass().add("text-id");
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPadding(new Insets(10, 10, 10, 10));
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        alert.getDialogPane().getScene().getRoot().getStyleClass().add(TRANSPARENT_STYLE);

        ThemeHelper.applyTheme(alert);
        StageUtility.centerStage(stage, parentStage);

        if (blocking) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    /**
     * Shows a blocking error dialog.
     *
     * @param title       The title to display.
     * @param message     The message to display.
     * @param parentStage The parent stage.
     */
    public static void showErrorDialog(String title, String message, Stage parentStage) {
        showErrorDialog(title, message, true, parentStage);
    }

    /**
     * Shows a non-blocking error dialog.
     *
     * @param title       The title to display.
     * @param message     The message to display.
     * @param blocking    True if the alert dialog should block the UI thread until user action is performed.
     * @param parentStage The parent stage.
     */
    public static void showErrorDialog(String title, String message, boolean blocking, Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setAlwaysOnTop(true);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.getDialogPane().setPadding(new Insets(10, 10, 10, 10));
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        alert.getDialogPane().getScene().getRoot().getStyleClass().add(TRANSPARENT_STYLE);

        ThemeHelper.applyTheme(alert);
        StageUtility.centerStage(stage, parentStage);

        if (blocking) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    /**
     * Creates a confirmation dialog that returns true if the user accepted the message.
     *
     * @param title       The title of the confirmation.
     * @param message     The message of the confirmation.
     * @param parentStage The parent stage.
     * @return Returns true if the user accepted the message.
     */
    public static boolean showConfigurationDialog(String title, String message, Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setAlwaysOnTop(true);
        alert.setTitle(title);
        Text text = new Text(message);
        text.setWrappingWidth(300);
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPadding(new Insets(10, 10, 10, 10));
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        alert.getDialogPane().getScene().getRoot().getStyleClass().add(TRANSPARENT_STYLE);

        ThemeHelper.applyTheme(alert);
        StageUtility.centerStage(stage, parentStage);

        Optional<ButtonType> results = alert.showAndWait();
        return results.isPresent() && results.get() == ButtonType.OK;
    }

    /**
     * Creates a Success dialog that returns true if the user accepted the message.
     *
     * @param title       The title of the confirmation.
     * @param header      The header text.
     * @param message     The message of the confirmation.
     * @param parentStage The parent stage.
     * @return Returns true if the user accepted the message.
     */
    public static boolean showInformationDialog(String title, String header, String message, Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setAlwaysOnTop(true);
        alert.setTitle(title);
        alert.setHeaderText(header);
        Text text = new Text(message);
        text.setWrappingWidth(300);
        text.getStyleClass().add("text-id");
        VBox vBox = new VBox(text);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        alert.getDialogPane().setContent(vBox);
        alert.getDialogPane().setPadding(new Insets(10, 10, 10, 10));
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        alert.getDialogPane().getScene().getRoot().getStyleClass().add(TRANSPARENT_STYLE);

        ThemeHelper.applyTheme(alert);
        StageUtility.centerStage(stage, parentStage);

        Optional<ButtonType> results = alert.showAndWait();
        return results.isPresent() && results.get() == ButtonType.OK;
    }

    /**
     * Creates a confirmation dialog that returns true if the user accepted the message.
     * This method uses "Yes" and "No" for the positive and negation.
     *
     * @param title       The title of the confirmation.
     * @param message     The message of the confirmation.
     * @param parentStage The parent stage.
     * @return Returns true if the user accepted the message.
     */
    public static boolean showYesNoDialog(String title, String message, Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setAlwaysOnTop(true);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.getDialogPane().setPadding(new Insets(10, 10, 10, 10));
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        alert.getDialogPane().getScene().getRoot().getStyleClass().add(TRANSPARENT_STYLE);

        ThemeHelper.applyTheme(alert);
        StageUtility.centerStage(stage, parentStage);

        Optional<ButtonType> results = alert.showAndWait();
        return results.isPresent() && results.get() == ButtonType.YES;
    }

    /**
     * Creates a confirmation dialog with three options: Yes, No, and Cancel.
     *
     * @param title       The title of the confirmation.
     * @param message     THe message of the confirmation
     * @param parentStage The parent stage.
     * @return The user results based on provided input.
     */
    public static UserResult showYesNoCancelDialog(String title, String message, Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setAlwaysOnTop(true);
        alert.setTitle(title);
        Text text = new Text(message);
        text.setWrappingWidth(300);
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPadding(new Insets(10, 10, 10, 10));
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        alert.getDialogPane().getScene().getRoot().getStyleClass().add(TRANSPARENT_STYLE);

        ThemeHelper.applyTheme(alert);
        StageUtility.centerStage(stage, parentStage);

        Optional<ButtonType> results = alert.showAndWait();

        if (results.isPresent()) {
            if (results.get() == ButtonType.YES) {
                return UserResult.YES;
            } else if (results.get() == ButtonType.NO) {
                return UserResult.NO;
            } else {
                return UserResult.CANCEL;
            }
        } else {
            return UserResult.CANCEL;
        }
    }
}
