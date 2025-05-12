package gov.mil.otc._3dvis.tir;

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;

public class TirFileCell extends ListCell<TirFileView> {

    final Tooltip tooltip = new Tooltip();

    public TirFileCell() {
        getStyleClass().add("list-view-transparent");
    }

    @Override
    public void updateItem(TirFileView tirFileView, boolean empty) {
        super.updateItem(tirFileView, empty);

        if (tirFileView == null) {
            setGraphic(null);
            setStyle(null);
            setText(null);
        } else {
//            File currentFile = ImageViewerManager.getCurrent(entity);
//
//            if (timestampFile.getFile().equals(currentFile)) {
//                setStyle("-fx-background-color: red");
//            } else {
//                setStyle("");
//            }

            setText(tirFileView.getTimestampString());
            tooltip.setText(tirFileView.getFile().getAbsolutePath());
            setTooltip(tooltip);
        }
    }
}
