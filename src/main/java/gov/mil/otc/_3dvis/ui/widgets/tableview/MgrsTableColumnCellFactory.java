package gov.mil.otc._3dvis.ui.widgets.tableview;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MgrsTableColumnCellFactory<T> implements Callback<TableColumn<T, Position>,
        TableCell<T, Position>> {
    @Override
    public TableCell<T, Position> call(TableColumn param) {
        return new TableCell<>() {
            @Override
            public void updateItem(Position item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    try {
                        MGRSCoord mgrsCoord = MGRSCoord.fromLatLon(item.getLatitude(), item.getLongitude());
                        setText(mgrsCoord.toString());
                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, "MGRS Conversion Failed", e);
                        setText("err");
                    }
                } else {
                    setText("");
                    setGraphic(null);
                }
            }
        };
    }
}
