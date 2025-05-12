package gov.mil.otc._3dvis.ui.widgets;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ProgressDialog extends TransparentWindow {

    private final ListView<TextWithStyleClass> statusListView = new ListView<>();
    private final ProgressBar progressBar = new ProgressBar();
    private final Button cancelButton = new Button("Cancel");
    private CancelListener cancelListener;

    public ProgressDialog(Stage parentStage) {
        this(parentStage, null);
    }

    public ProgressDialog(Stage parentStage, CancelListener cancelListener) {
        super(parentStage);
        this.cancelListener = cancelListener;
    }

    @Override
    protected Pane createContentPane() {
        VBox mainVBox = new VBox(UiConstants.SPACING, statusListView, progressBar);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        if (cancelListener != null) {
            cancelButton.setOnAction(event -> cancel());
            mainVBox.getChildren().add(cancelButton);
        }
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setAlignment(Pos.CENTER);
        mainVBox.setMinWidth(600);

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        getStage().getScene().getRoot().setStyle("-fx-border-color: grey; -fx-border-width: 5;");
        return true;
    }

    public void setCancelListener(CancelListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public void addStatus(String value) {
        statusListView.getItems().add(new TextWithStyleClass(value));
        int lastIndex = statusListView.getItems().size() - 1;
        statusListView.scrollTo(lastIndex);
    }

    public void addError(String value) {
        TextWithStyleClass textWithStyleClass = new TextWithStyleClass(value);
        textWithStyleClass.setFill(Color.RED);
        statusListView.getItems().add(new TextWithStyleClass(value));
        int lastIndex = statusListView.getItems().size() - 1;
        statusListView.scrollTo(lastIndex);
    }

    public void setProgress(double value) {
        progressBar.setProgress(value);
    }

    public void setComplete(boolean complete) {
        if (complete) {
            addStatus("complete");
            setProgress(1);
            cancelButton.setText("Close");
        } else {
            cancelButton.setText("Cancel");
        }
    }

    private void cancel() {
        if (cancelListener != null) {
            cancelListener.onCancel();
        }
    }

    public interface CancelListener {
        void onCancel();
    }
}
