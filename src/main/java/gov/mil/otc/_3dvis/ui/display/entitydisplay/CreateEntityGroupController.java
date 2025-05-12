package gov.mil.otc._3dvis.ui.display.entitydisplay;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class CreateEntityGroupController extends TransparentWindow {

    public static synchronized String show() {
        CreateEntityGroupController createEntityGroupController = new CreateEntityGroupController();
        createEntityGroupController.createAndShow(true);
        return createEntityGroupController.textField.getText();
    }

    private final TextField textField = new TextField();

    @Override
    protected Pane createContentPane() {
        Button createButton = new Button("Create");
        createButton.setOnAction(event -> {
            if (!textField.getText().isBlank()) {
                close();
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());
        HBox buttonHBox = new HBox(UiConstants.SPACING, createButton, cancelButton);
        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Enter Entity Group Name"),
                new Separator(),
                textField,
                buttonHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setFillWidth(true);
        mainVBox.setPrefWidth(400);
        return mainVBox;
    }
}
