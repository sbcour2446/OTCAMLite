package gov.mil.otc._3dvis.playback.dataset.entity;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.entity.base.EntityConfiguration;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class EntityConfigurationImportObject extends ImportObject<EntityConfiguration> {

    private final EntityConfiguration entityConfiguration;

    public EntityConfigurationImportObject(EntityConfiguration entityConfiguration) {
        super(entityConfiguration, "Entity Configuration");
        this.entityConfiguration = entityConfiguration;
    }

    @Override
    public VBox getDisplayPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().getFirst().setHalignment(HPos.RIGHT);

        int rowIndex = 0;

        gridPane.add(new Label("Name:"), 0, rowIndex);
        gridPane.add(new Label(entityConfiguration.getName()), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Description:"), 0, rowIndex);
        gridPane.add(new Label(entityConfiguration.getDescription()), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Entity Type:"), 0, rowIndex);
        gridPane.add(new Label(entityConfiguration.getEntityType().toString()), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Affiliation:"), 0, rowIndex);
        gridPane.add(new Label(entityConfiguration.getAffiliation().toString()), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Urn:"), 0, rowIndex);
        gridPane.add(new Label(String.valueOf(entityConfiguration.getUrn())), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Miles Pid:"), 0, rowIndex);
        gridPane.add(new Label(String.valueOf(entityConfiguration.getMilesPid())), 1, rowIndex);

        if (!entityConfiguration.getManualPositionList().isEmpty()) {
            for (EntityConfiguration.ManualPosition manualPosition : entityConfiguration.getManualPositionList()) {
                rowIndex++;

                gridPane.add(new Label("Position:"), 0, rowIndex);
                gridPane.add(new Label(String.format("%s, %s, %s",
                        Utility.formatTime(manualPosition.getTimestamp(), Common.DATE_TIME),
                        manualPosition.getLatitude(), manualPosition.getLongitude())), 1, rowIndex);
            }
        }

        VBox vBox = new VBox(UiConstants.SPACING, gridPane);
        vBox.setPadding(new Insets(UiConstants.SPACING));
        return vBox;
    }

    @Override
    public void doImport() {
        //not implemented
    }

    @Override
    public void doImport(IEntity entity) {
        //not implemented
    }
}
