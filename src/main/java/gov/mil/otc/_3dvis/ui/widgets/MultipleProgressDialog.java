package gov.mil.otc._3dvis.ui.widgets;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MultipleProgressDialog extends TransparentWindow {

    private final List<Label> statusLabels = new ArrayList<>();
    private final List<ProgressBar> progressBars = new ArrayList<>();
    private final CancelListener cancelListener;
    private final int numberOfProgresses;

    public MultipleProgressDialog(Stage parentStage, CancelListener cancelListener, int numberOfProgresses) {
        super(parentStage);
        this.cancelListener = cancelListener;
        this.numberOfProgresses = numberOfProgresses;
    }

    @Override
    protected Pane createContentPane() {

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> cancel());

        VBox mainVBox = new VBox(UiConstants.SPACING);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < numberOfProgresses; i++) {
            Label label = new Label();
            ProgressBar progressBar = new ProgressBar();
            statusLabels.add(label);
            progressBars.add(progressBar);
            mainVBox.getChildren().add(label);
            mainVBox.getChildren().add(progressBar);
            mainVBox.getChildren().add(new Separator());
        }
        mainVBox.getChildren().add(cancelButton);

        return mainVBox;
    }

    public void setStatus(int index, String value) {
        if (index >= 0 && index < statusLabels.size()) {
            statusLabels.get(index).setText(value);
        }
    }

    public void setProgress(int index, double value) {
        if (index >= 0 && index < progressBars.size()) {
            progressBars.get(index).setProgress(value);
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
