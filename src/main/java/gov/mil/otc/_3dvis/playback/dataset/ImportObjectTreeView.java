package gov.mil.otc._3dvis.playback.dataset;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class ImportObjectTreeView extends TreeView<ImportObject<?>> {

    private PlaybackImportFolder playbackImportFolder;
    private final ContextMenu treeViewContextMenu = new ContextMenu();

    public ImportObjectTreeView() {
        setRoot(new TreeItem<>(new NoImportObject("root", "root")));
        setShowRoot(false);
        setCellFactory(param -> new ImportObjectTreeCell());
    }

    public void setPlaybackImportFolder(PlaybackImportFolder playbackImportFolder) {
        this.playbackImportFolder = playbackImportFolder;
        reload();
    }

    public void reload() {
        getRoot().getChildren().clear();
        if (playbackImportFolder != null) {
            getRoot().getChildren().add(playbackImportFolder.getTreeItem());
        }
    }

    public static final class ImportObjectTreeCell extends TreeCell<ImportObject<?>> {

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
