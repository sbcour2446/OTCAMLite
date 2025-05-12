package gov.mil.otc._3dvis.ui.projects.blackhawk;

import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.ui.widgets.entity.entitytype.EntityTypePicker;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

public class ThreatView {

    private final AdHocEntity adHocEntity;
    private final String name;
    private final String system;
    private final Hyperlink entityTypeHyperlink = new Hyperlink();
    private final StringProperty description = new SimpleStringProperty();
    private final String videoCount;
    private EntityType entityType;

    public ThreatView(AdHocEntity adHocEntity, final Stage parentStage) {
        this.adHocEntity = adHocEntity;
        name = adHocEntity.getName();
        system = adHocEntity.getSource();
        entityType = adHocEntity.getEntityType();
        entityTypeHyperlink.setText(entityType.toString());
        entityTypeHyperlink.setOnAction(event -> {
            EntityType selectedEntityType = EntityTypePicker.show(parentStage, entityType);
            if (selectedEntityType != null) {
                entityType = selectedEntityType;
                entityTypeHyperlink.setText(entityType.toString());
                description.set(entityType.getDescription());
            }
        });
        description.set(entityType.getDescription());
        if (adHocEntity.getMediaSetList().isEmpty()) {
            videoCount = "0";
        } else {
            videoCount = String.valueOf(adHocEntity.getMediaSetList().get(0).getMediaFiles().size());
        }
    }

    public AdHocEntity getAdHocEntity() {
        return adHocEntity;
    }

    public String getName() {
        return name;
    }

    public String getSystem() {
        return system;
    }

    public Hyperlink getEntityTypeHyperlink() {
        return entityTypeHyperlink;
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getVideoCount() {
        return videoCount;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
