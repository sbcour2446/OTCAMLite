package gov.mil.otc._3dvis.ui.data.dataimport.tapets;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.stage.Stage;

public class EntityLinkCell<T> extends TableCell<T, Void> {

    private final Hyperlink hyperlink = new Hyperlink("select entity");

    public EntityLinkCell(final Stage parentStage) {
        hyperlink.setOnAction(event -> {
            IEntity entity = EntityPicker.show(parentStage);
            if (entity != null) {
                hyperlink.setText(entity.getEntityId().toString());
            }
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : hyperlink);
    }
}
