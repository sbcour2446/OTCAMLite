package gov.mil.otc._3dvis.ui.data.dataimport.bft;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class UrnFilterList extends TransparentWindow {

    private final List<String> initialValue;

    public static synchronized List<String> show(Stage parentStage, List<String> initialValue) {
        UrnFilterList urnFilterList = new UrnFilterList(parentStage, initialValue);
        urnFilterList.createAndShow(true);
        return urnFilterList.initialValue;
    }

    private UrnFilterList(Stage parentStage, List<String> initialValue) {
        super(parentStage);
        this.initialValue = initialValue;
    }

    @Override
    protected Pane createContentPane() {
        TextField urnTextField = new TextField();
        Button addButton = new Button("Add");
        Button importFileButton = new Button("Import File");
        Button removeSelectedButton = new Button("Remove Selected");

        HBox addHBox = new HBox(UiConstants.SPACING, new Label("URN:"), urnTextField, addButton,
                new Separator(Orientation.VERTICAL), importFileButton,
                new Separator(Orientation.VERTICAL), removeSelectedButton);

        addHBox.setAlignment(Pos.CENTER);

        ListView<String> urnListView = new ListView<>();

        Button okButton = new Button("OK");
        okButton.setOnAction(event -> close());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        HBox closeButtonsHBox = new HBox(UiConstants.SPACING, okButton, cancelButton);
        closeButtonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING, createTitleLabel("URN Filter List"), new Separator(),
                addHBox, urnListView, new Separator(), closeButtonsHBox);
        mainVBox.setMinWidth(600);
        mainVBox.setAlignment(Pos.CENTER);

        return mainVBox;
    }
}
