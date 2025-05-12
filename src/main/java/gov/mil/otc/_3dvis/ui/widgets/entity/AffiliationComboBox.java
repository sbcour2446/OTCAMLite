package gov.mil.otc._3dvis.ui.widgets.entity;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class AffiliationComboBox extends ComboBox<Affiliation> {

    public AffiliationComboBox() {
        super(FXCollections.observableArrayList(Affiliation.values()));
        setCellFactory(new ComboBoxCellFactory());
    }

    private static final class ComboBoxCellFactory implements Callback<ListView<Affiliation>, ListCell<Affiliation>> {
        @Override
        public ListCell<Affiliation> call(ListView<Affiliation> param) {
            return new ListCell<>() {
                @Override
                public void updateItem(Affiliation item,
                                       boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setGraphic(null);
                    } else {
                        setText(item.getName());
                    }
                }
            };
        }
    }
}
