package gov.mil.otc._3dvis.ui;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public abstract class UtilityPane {

    private Pane mainPane;

    protected UtilityPane() {
    }

    public void createAndShow(Pos pos) {
        Pane contentPane = createContentPane();
        contentPane.getStyleClass().add("root");

        BorderPane borderPane = new BorderPane(contentPane, createTitleLabel(), null, null, null);

        mainPane = new AnchorPane(borderPane);
        mainPane.setPickOnBounds(false);

        if (pos == Pos.TOP_RIGHT || pos == Pos.TOP_CENTER || pos == Pos.TOP_LEFT) {
            AnchorPane.setTopAnchor(borderPane, 0.0);
        }
        if (pos == Pos.TOP_LEFT || pos == Pos.CENTER_LEFT || pos == Pos.BOTTOM_LEFT) {
            AnchorPane.setLeftAnchor(borderPane, 0.0);
        }
        if (pos == Pos.TOP_RIGHT || pos == Pos.CENTER_RIGHT || pos == Pos.BOTTOM_RIGHT) {
            AnchorPane.setRightAnchor(borderPane, 0.0);
        }
        if (pos == Pos.BOTTOM_LEFT || pos == Pos.BOTTOM_CENTER || pos == Pos.BOTTOM_RIGHT) {
            AnchorPane.setBottomAnchor(borderPane, 0.0);
        }

        if (!onShowing()) {
            return;
        }

        MainApplication.getInstance().addPane(mainPane);
    }

    public void close() {
        if (closeRequested()) {
            MainApplication.getInstance().removePane(mainPane);
        }
    }

    protected boolean onShowing() {
        return true;
    }

    protected boolean closeRequested() {
        return true;
    }

    protected Pane createTitleLabel() {
        Label titleLabel = new Label(getTitle());
        titleLabel.setFont(Font.font(UiConstants.FONT_NAME, FontWeight.BOLD, 18));
        titleLabel.setPadding(new Insets(UiConstants.SPACING / 2.0));
        VBox vBox = new VBox(titleLabel);
        vBox.setAlignment(Pos.CENTER);
        vBox.getStyleClass().add("title-pane");
        return vBox;
    }

    protected abstract String getTitle();

    protected abstract Pane createContentPane();
}
