package gov.mil.otc._3dvis.ui.widgets.tableview;

import gov.nasa.worldwind.geom.Position;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ElevationTableColumnCellFactory<T> implements Callback<TableColumn<T, Position>,
        TableCell<T, Position>> {
    @Override
    public TableCell<T, Position> call(TableColumn param) {
        return new TableCell<>() {
            @Override
            public void updateItem(Position item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(String.format("%,d", (int) item.getElevation()));
                } else {
                    setText("");
                    setGraphic(null);
                }
            }
        };
    }
}
