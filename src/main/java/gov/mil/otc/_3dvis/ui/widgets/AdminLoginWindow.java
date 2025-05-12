package gov.mil.otc._3dvis.ui.widgets;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class AdminLoginWindow extends TransparentWindow {

    private final PasswordField passwordField = new PasswordField();
    private boolean loginSuccessful = false;

    public static boolean show() {
        AdminLoginWindow adminLoginWindow = new AdminLoginWindow();
        adminLoginWindow.createAndShow(true);
        return adminLoginWindow.loginSuccessful;
    }

    @Override
    protected Pane createContentPane() {
        passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    login();
                }
            }
        });
        Button okButton = new Button("OK");
        okButton.setOnAction(event -> login());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Enter Admin Password"),
                new Separator(),
                passwordField,
                new Separator(),
                new HBox(UiConstants.SPACING, okButton, cancelButton));
        mainVBox.setMinWidth(300);
        mainVBox.setAlignment(Pos.CENTER);

        return mainVBox;
    }

    private void login() {
        if (passwordField.getText().equals("admin1234")) {
            loginSuccessful = true;
            close();
        }
    }
}
