package gov.mil.otc._3dvis.ui.widgets.status;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatusDialog extends TransparentWindow implements IStatusListener {

    private final ListView<HBox> statusListView = new ListView<>();
    private final Button cancelButton = new Button("Cancel");
    private final ICancelListener cancelListener;
    private final List<StatusLine> statusLineList = Collections.synchronizedList(new ArrayList<>());
    private boolean canceled = false;

    public StatusDialog(Stage parentStage) {
        this(parentStage, null);
    }

    public StatusDialog(Stage parentStage, ICancelListener cancelListener) {
        super(parentStage);
        this.cancelListener = cancelListener;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setComplete(boolean complete) {
        if (complete) {
            cancelButton.setText("Close");
        } else {
            cancelButton.setText("Cancel");
        }
    }

    @Override
    protected Pane createContentPane() {
        VBox mainVBox = new VBox(UiConstants.SPACING, statusListView);
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

    public StatusLine createStatusLine(String text) {
        StatusLine statusLine = new StatusLine(text, this);
        synchronized (statusLineList) {
            statusLineList.add(statusLine);
        }
        updateStatusList();
        return statusLine;
    }

    private void updateStatusList() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::doUpdateStatusList);
        } else {
            doUpdateStatusList();
        }
    }

    private void doUpdateStatusList() {
        synchronized (statusLineList) {
            statusListView.getItems().clear();
            for (StatusLine statusLine : statusLineList) {
                for (HBox hBox : statusLine.getStatusLines("")) {
                    statusListView.getItems().add(hBox);
                }
            }
        }

        int lastIndex = statusListView.getItems().size() - 1;
        statusListView.scrollTo(lastIndex);
    }

    private void cancel() {
        canceled = true;
        if (cancelListener != null) {
            cancelListener.onCancel();
        }
    }

    @Override
    public void onStatusUpdate() {
        updateStatusList();
    }
}
