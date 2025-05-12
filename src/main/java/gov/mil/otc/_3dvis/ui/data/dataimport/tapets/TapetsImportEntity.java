package gov.mil.otc._3dvis.ui.data.dataimport.tapets;

import gov.mil.otc._3dvis.data.tapets.TapetsLogFile;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.datamodel.EntityTypeUtility;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import gov.mil.otc._3dvis.ui.widgets.entity.entitytype.EntityTypePicker;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TapetsImportEntity {

    private final CheckBox importData = new CheckBox();
    private final RadioButton createNew = new RadioButton();
    private final RadioButton updateExisting = new RadioButton();
    private final Hyperlink entityHyperlink = new Hyperlink("generated");
    private final TextField name = new TextField();
    private final Hyperlink entityTypeHyperlink = new Hyperlink("select");
    private final Label description = new Label();
    private final ComboBox<Affiliation> affiliation = new ComboBox<>();
    private final TextField urn = new TextField();
    private final Stage parentStage;
    private final int unitId;
    private IEntity entity;
    private EntityType entityType;
    private List<TapetsLogFile> tapetsLogFileList = new ArrayList<>();
    private long startTime = Long.MAX_VALUE;
    private long stopTime = Long.MIN_VALUE;

    public TapetsImportEntity(Stage parentStage, int unitId) {
        this.parentStage = parentStage;
        this.unitId = unitId;
        importData.setSelected(true);
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(createNew, updateExisting);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (createNew.isSelected()) {
                entityHyperlink.setText("generated");
                entityHyperlink.setDisable(true);
                name.setDisable(false);
                entityTypeHyperlink.setDisable(false);
                affiliation.setDisable(false);
                urn.setEditable(true);
            } else {
                entityHyperlink.setText("select");
                entityHyperlink.setDisable(false);
                name.setDisable(true);
                entityTypeHyperlink.setDisable(true);
                affiliation.setDisable(true);
                urn.setEditable(false);
            }
        });
        createNew.setSelected(true);
        entityHyperlink.setOnAction(event -> selectEntity());
        name.setStyle("-fx-text-box-border: transparent;");
        entityTypeHyperlink.setOnAction(event -> selectEntityType());
        affiliation.getItems().addAll(Affiliation.values());
        affiliation.getSelectionModel().selectFirst();
        urn.setStyle("-fx-text-box-border: transparent;");
    }

    public void addFile(TapetsLogFile tapetsLogFile) {
        tapetsLogFileList.add(tapetsLogFile);

        if (tapetsLogFile.getStartTime() < startTime) {
            startTime = tapetsLogFile.getStartTime();
        }

        if (tapetsLogFile.getStopTime() > stopTime) {
            stopTime = tapetsLogFile.getStopTime();
        }
    }

    public List<TapetsLogFile> getTapetsLogFileList() {
        return tapetsLogFileList;
    }

    public int getUnitId() {
        return unitId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public CheckBox getImportData() {
        return importData;
    }

    public RadioButton getCreateNew() {
        return createNew;
    }

    public RadioButton getUpdateExisting() {
        return updateExisting;
    }

    public Hyperlink getEntityHyperlink() {
        return entityHyperlink;
    }

    public TextField getName() {
        return name;
    }

    public Hyperlink getEntityTypeHyperlink() {
        return entityTypeHyperlink;
    }

    public Label getDescription() {
        return description;
    }

    public ComboBox<Affiliation> getAffiliation() {
        return affiliation;
    }

    public TextField getUrn() {
        return urn;
    }

    public Stage getParentStage() {
        return parentStage;
    }

    public IEntity getEntity() {
        return entity;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    private void selectEntity() {
        entity = EntityPicker.show(parentStage);
        if (entity != null) {
            entityHyperlink.setText(entity.getEntityId().toString());
            EntityDetail entityDetail = entity.getLastEntityDetail();
            if (entityDetail != null) {
                name.setText(entityDetail.getName());
                entityType = entityDetail.getEntityType();
                entityTypeHyperlink.setText(entityDetail.getEntityType().toString());
                description.setText(EntityTypeUtility.getDescription(entityType));
                affiliation.getSelectionModel().select(entityDetail.getAffiliation());
                urn.setText(String.valueOf(entityDetail.getUrn()));
            }
        }
    }

    private void selectEntityType() {
        entityType = EntityTypePicker.show(parentStage, entityType);
        if (entityType != null) {
            entityTypeHyperlink.setText(entityType.toString());
            description.setText(EntityTypeUtility.getDescription(entityType));
        }
    }
}