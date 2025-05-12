package gov.mil.otc._3dvis.ui.widgets.status;

import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatusLine implements IStatusListener {

    private final String mainText;
    private final IStatusListener statusListener;
    private final List<StatusLine> childItemList = new ArrayList<>();
    private String appendText = "";
    private final List<String> errorList = new ArrayList<>();
    private Result result;

    protected StatusLine(String text, IStatusListener statusListener) {
        mainText = text;
        this.statusListener = statusListener;
    }

    public List<HBox> getStatusLines(String prefix) {
        List<HBox> statusLines = new ArrayList<>();
        HBox hBox = new HBox();
        statusLines.add(hBox);

        TextWithStyleClass textWithStyleClass = new TextWithStyleClass(prefix + mainText + appendText + " ");
        textWithStyleClass.setStyle("-fx-fill: lightblue;");
        hBox.getChildren().add(textWithStyleClass);

        if (result != null) {
            textWithStyleClass = new TextWithStyleClass(result.getText());
            if (result.isSuccessful()) {
                textWithStyleClass.setStyle("-fx-fill: green;");
            } else {
                textWithStyleClass.setStyle("-fx-fill: red;");
            }
            hBox.getChildren().add(textWithStyleClass);
        }

        if (!errorList.isEmpty()) {
            Label label = new Label(" (" + errorList.size() + " errors)");
            Tooltip tooltip = new Tooltip();
            StringBuilder tooltipText = new StringBuilder();
            String newLine = "";
            for (String error : errorList) {
                tooltipText.append(newLine).append(error);
                newLine = System.lineSeparator();
            }
            tooltip.setText(tooltipText.toString());
            label.setTooltip(tooltip);
            label.setStyle("-fx-text-fill:red");
            label.setAlignment(Pos.CENTER_LEFT);
            hBox.getChildren().add(label);
        }

        synchronized (childItemList) {
            for (StatusLine statusLine : childItemList) {
                statusLines.addAll(statusLine.getStatusLines(prefix + "  "));
            }
        }

        return statusLines;
    }

    public ListView<ListView<?>> getDisplay(String prefix) {
        ListView<ListView<?>> listView = new ListView<>();
        HBox hBox = new HBox();

        hBox.getChildren().add(new TextWithStyleClass(prefix + mainText + appendText));

        if (result != null) {
            TextWithStyleClass textWithStyleClass = new TextWithStyleClass(result.getText());
            if (!result.isSuccessful()) {
                textWithStyleClass.setFill(Color.RED);
            }
            hBox.getChildren().add(textWithStyleClass);
        }

        ListView<HBox> statusListView = new ListView<>();
        statusListView.getItems().add(hBox);
        listView.getItems().add(statusListView);

        synchronized (childItemList) {
            for (StatusLine statusLine : childItemList) {
                listView.getItems().add(statusLine.getDisplay(prefix + "  "));
            }
        }

        return listView;
    }

    public StatusLine createChildStatus(String text) {
        StatusLine statusLine = new StatusLine(text, this);
        synchronized (childItemList) {
            childItemList.add(statusLine);
        }
        statusUpdated();
        return statusLine;
    }

    private void statusUpdated() {
        if (statusListener != null) {
            statusListener.onStatusUpdate();
        }
    }

    public String getMainText() {
        return mainText;
    }

    public void appendText(String text) {
        appendText += text;
    }

    public void addError(String error) {
        errorList.add(error);
    }

    public void setComplete() {
        setResult(new Result("complete", true));
    }

    public void setFailed() {
        setResult(new Result("failed", false));
    }

    public void setCanceled() {
        setResult(new Result("canceled", false));
    }

    public void setResult(Result result) {
        this.result = result;
        statusUpdated();
    }

    public Result getResult() {
        return result;
    }

    public List<StatusLine> getChildItemList() {
        return childItemList;
    }

    @Override
    public void onStatusUpdate() {
        statusUpdated();
    }
}
