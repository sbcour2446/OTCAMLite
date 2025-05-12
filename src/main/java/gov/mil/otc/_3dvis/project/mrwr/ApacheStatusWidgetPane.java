package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ApacheStatusWidgetPane implements IWidgetPane {

    private static final Map<EntityId, ApacheStatusWidgetPane> widgetPaneMap = new HashMap<>();

    public static void show(ApacheEntity apacheEntity) {
        if (widgetPaneMap.containsKey(apacheEntity.getEntityId())) {
            WidgetPaneContainer.showWidgetPane(widgetPaneMap.get(apacheEntity.getEntityId()));
            return;
        }
        ApacheStatusWidgetPane widgetPane = new ApacheStatusWidgetPane(apacheEntity);
        widgetPaneMap.put(apacheEntity.getEntityId(), widgetPane);
        WidgetPaneContainer.addWidgetPane(widgetPane);
    }

    public static void close(ApacheEntity apacheEntity) {
        ApacheStatusWidgetPane widgetPane = widgetPaneMap.get(apacheEntity.getEntityId());
        if (widgetPane != null) {
            WidgetPaneContainer.closeWidgetPane(widgetPane);
        }
    }

    private final ApacheEntity apacheEntity;
    private final VBox vBox = new VBox();
    private final Map<String, TextField> textFieldMap = new HashMap<>();
    private final GridPane gridPane = new GridPane();
    private final Map<EntityId, Map<String, TextWithStyleClass>> threatLineValueMap = new HashMap<>();
    private final Timer updateTimer = new Timer("ApacheStatusWidgetPane::updateTimer");

    private ApacheStatusWidgetPane(ApacheEntity apacheEntity) {
        this.apacheEntity = apacheEntity;
        initialize();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateStatus();
            }
        }, 1000, 100);
    }

    private void initialize() {
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setGridLinesVisible(true);

        createGridViewItems();

//        int rowIndex = 0;
//
//        TextWithStyleClass textWithStyleClass = new TextWithStyleClass("Threat    ");
//        textWithStyleClass.setStyle("-fx-font-weight: bold;");
//
//        gridPane.add(addPadding(textWithStyleClass), 0, rowIndex);
//
//        textWithStyleClass = new TextWithStyleClass("Angle \u00B0   ");
//        textWithStyleClass.setStyle("-fx-font-weight: bold;");
//
//        gridPane.add(addPadding(textWithStyleClass), 1, rowIndex);
//
//        textWithStyleClass = new TextWithStyleClass("Range (m) ");
//        textWithStyleClass.setStyle("-fx-font-weight: bold;");
//
//        gridPane.add(addPadding(textWithStyleClass), 2, rowIndex);
//
//        rowIndex++;
//
//        for (ThreatLine threatLine : apacheEntity.getThreatLineList()) {
//            if (threatLine.isValid()) {
//                TextWithStyleClass angleTextWithStyleClass = new TextWithStyleClass("");
//                TextWithStyleClass slantRangeTextWithStyleClass = new TextWithStyleClass("");
//                Map<String, TextWithStyleClass> itemMap = new HashMap<>();
//                itemMap.put("angleText", angleTextWithStyleClass);
//                itemMap.put("slantRangeText", slantRangeTextWithStyleClass);
//                threatLineValueMap.put(threatLine.getEntityId(), itemMap);
//
//                TextWithStyleClass threadNameTextWithStyleClass = new TextWithStyleClass(threatLine.getThreatName());
//                threadNameTextWithStyleClass.setStyle("-fx-font-weight: bold;");
//                gridPane.add(addPadding(threadNameTextWithStyleClass), 0, rowIndex);
//                gridPane.add(addPadding(angleTextWithStyleClass), 1, rowIndex);
//                gridPane.add(addPadding(slantRangeTextWithStyleClass), 2, rowIndex);
//
//                rowIndex++;
//            }
//        }

        vBox.getChildren().add(gridPane);
    }

    private void createGridViewItems() {
        gridPane.getChildren().clear();
        threatLineValueMap.clear();

        int rowIndex = 0;

        TextWithStyleClass textWithStyleClass = new TextWithStyleClass("Threat    ");
        textWithStyleClass.setStyle("-fx-font-weight: bold;");

        gridPane.add(addPadding(textWithStyleClass), 0, rowIndex);

        textWithStyleClass = new TextWithStyleClass("Angle \u00B0   ");
        textWithStyleClass.setStyle("-fx-font-weight: bold;");

        gridPane.add(addPadding(textWithStyleClass), 1, rowIndex);

        textWithStyleClass = new TextWithStyleClass("Range (m) ");
        textWithStyleClass.setStyle("-fx-font-weight: bold;");

        gridPane.add(addPadding(textWithStyleClass), 2, rowIndex);

        rowIndex++;

        for (ThreatLine threatLine : apacheEntity.getThreatLineList()) {
            if (threatLine.isValid()) {
                TextWithStyleClass angleTextWithStyleClass = new TextWithStyleClass("");
                TextWithStyleClass slantRangeTextWithStyleClass = new TextWithStyleClass("");
                Map<String, TextWithStyleClass> itemMap = new HashMap<>();
                itemMap.put("angleText", angleTextWithStyleClass);
                itemMap.put("slantRangeText", slantRangeTextWithStyleClass);
                threatLineValueMap.put(threatLine.getEntityId(), itemMap);

                TextWithStyleClass threatNameTextWithStyleClass = new TextWithStyleClass(threatLine.getThreatName());
                threatNameTextWithStyleClass.setStyle("-fx-font-weight: bold;");
                gridPane.add(addPadding(threatNameTextWithStyleClass), 0, rowIndex);
                gridPane.add(addPadding(angleTextWithStyleClass), 1, rowIndex);
                gridPane.add(addPadding(slantRangeTextWithStyleClass), 2, rowIndex);

                rowIndex++;
            }
        }
    }

    private Node addPadding(Node node) {
        HBox hBox = new HBox(node);
        hBox.setPadding(new Insets(UiConstants.SPACING));
        return hBox;
    }

    private void updateStatus() {
        boolean hasItemListChange = false;
        for (ThreatLine threatLine : apacheEntity.getThreatLineList()) {
            if (threatLine.isValid() && !threatLineValueMap.containsKey(threatLine.getEntityId())) {
                hasItemListChange = true;
            } else if (!threatLine.isValid() && threatLineValueMap.containsKey(threatLine.getEntityId())) {
                hasItemListChange = true;
            }
        }
        for (EntityId entityId : threatLineValueMap.keySet()) {
            boolean entityFound = false;
            for (ThreatLine threatLine : apacheEntity.getThreatLineList()) {
                if (entityId.equals(threatLine.getEntityId())) {
                    entityFound = true;
                    break;
                }
            }
            if (!entityFound) {
                hasItemListChange = true;
            }
        }

        final boolean recreateItems = hasItemListChange;

        Platform.runLater(() -> {
            if (recreateItems) {
                createGridViewItems();
            }

            for (ThreatLine threatLine : apacheEntity.getThreatLineList()) {
                if (threatLine.isValid()) {
                    String angleText = threatLine.isValid() ? String.format("%.2f", threatLine.getThreatAngle()) : "";
                    String slantRangeText = threatLine.isValid() ? String.format("%d", threatLine.getSlantRange()) : "";
                    Map<String, TextWithStyleClass> itemMap = threatLineValueMap.get(threatLine.getEntityId());
                    if (itemMap == null) {
                        continue;
                    }
                    itemMap.get("angleText").setText(angleText);
                    itemMap.get("slantRangeText").setText(slantRangeText);
                    if (threatLine.isInRange()) {
                        itemMap.get("slantRangeText").setStyle("-fx-fill:red;");
                    } else {
                        itemMap.get("slantRangeText").setStyle("");
                    }
                }
            }
        });
    }

    @Override
    public String getName() {
        return apacheEntity.getName();
    }

    @Override
    public Pane getPane() {
        return vBox;
    }

    @Override
    public void dispose() {
        updateTimer.cancel();
        widgetPaneMap.remove(apacheEntity.getEntityId());
    }
}
