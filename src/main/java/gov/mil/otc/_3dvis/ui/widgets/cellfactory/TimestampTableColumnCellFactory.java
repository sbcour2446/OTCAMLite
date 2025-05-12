package gov.mil.otc._3dvis.ui.widgets.cellfactory;

import gov.mil.otc._3dvis.utility.Utility;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public final class TimestampTableColumnCellFactory<T> implements Callback<TableColumn<T, Long>,
        TableCell<T, Long>> {
    @Override
    public TableCell<T, Long> call(TableColumn param) {
        return new TableCell<>() {
            @Override
            public void updateItem(Long item,
                                   boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(Utility.formatTime(item));
                } else {
                    setText("");
                }
            }
        };
    }
}
