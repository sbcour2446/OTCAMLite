package gov.mil.otc._3dvis.project.rpuas;

import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class RpuasStatusWidgetPane implements IWidgetPane {

    private static final Map<EntityId, RpuasStatusWidgetPane> widgetPaneMap = new HashMap<>();

    public static void show(IEntity entity) {
        if (widgetPaneMap.containsKey(entity.getEntityId())) {
            WidgetPaneContainer.showWidgetPane(widgetPaneMap.get(entity.getEntityId()));
            return;
        }
        if (entity instanceof RpuasEntity rpuasEntity) {
            RpuasStatusWidgetPane widgetPane = new RpuasStatusWidgetPane(rpuasEntity);
            widgetPaneMap.put(entity.getEntityId(), widgetPane);
            WidgetPaneContainer.addWidgetPane(widgetPane);
        }
    }

    private final RpuasEntity rpuasEntity;
    private final VBox vBox = new VBox();
    private final Map<String, TextField> textFieldMap = new HashMap<>();
    private final GridPane gridPane = new GridPane();

    private RpuasStatusWidgetPane(RpuasEntity rpuasEntity) {
        this.rpuasEntity = rpuasEntity;
        initialize();
        rpuasEntity.addStatusListener(this::updateStatus);
    }

    private void initialize() {
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().getFirst().setHalignment(HPos.RIGHT);

        int rowIndex = 0;

        ValuePairTimedData otherData = rpuasEntity.getCurrentOtherData();
        if (otherData != null) {
            for (String key : otherData.getValueMap().keySet()) {
                TextField textField = new TextField();
                textField.setMaxWidth(Double.MAX_VALUE);
                textFieldMap.put(key, textField);
                gridPane.add(new Label(key + ":"), 0, rowIndex);
                gridPane.add(textField, 1, rowIndex);

                rowIndex++;
            }
        }

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        vBox.getChildren().add(scrollPane);
    }

    private void updateStatus(final IEntity entity) {
        final ValuePairTimedData otherData = rpuasEntity.getCurrentOtherData();
        if (otherData == null) {
            return;
        }
        Platform.runLater(() -> {
            for (Map.Entry<String, String> entry : otherData.getValueMap().entrySet()) {
                TextField textField = textFieldMap.get(entry.getKey());
                if (textField == null) {
                    textField = new TextField(entry.getKey());
                    textField.setMaxWidth(Double.MAX_VALUE);
                    gridPane.add(new Label(entry.getKey() + ":"), 0, textFieldMap.size());
                    gridPane.add(textField, 1, textFieldMap.size());
                    textFieldMap.put(entry.getKey(), textField);
                }
                textField.setText(entry.getValue());
            }
        });
    }

    @Override
    public String getName() {
        return rpuasEntity.getName();
    }

    @Override
    public Pane getPane() {
        return vBox;
    }

    @Override
    public void dispose() {
        // not implemented
    }
}
