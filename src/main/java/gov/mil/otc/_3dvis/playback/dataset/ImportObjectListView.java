package gov.mil.otc._3dvis.playback.dataset;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

public class ImportObjectListView extends ListView<ImportObject<?>> {

    public ImportObjectListView() {
        setCellFactory(timestampFileListView -> new ImportObjectListCell());
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public static class ImportObjectListCell extends ListCell<ImportObject<?>> {

        public ImportObjectListCell() {
            getStyleClass().add("list-view-transparent");
        }

        @Override
        public void updateItem(ImportObject<?> item, boolean empty) {
            super.updateItem(item, empty);

            setGraphic(null);
            setStyle(null);
            setText(item == null ? null : item.toString());

            if (item != null) {
                setStyle(ImportDataColor.getStyle(item));
            }
        }
    }
}
