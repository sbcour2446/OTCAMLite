package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.IEntityListener;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SensorStatusWidgetPane implements IWidgetPane, IEntityListener {

    private static SensorStatusWidgetPane instance = null;

    public static void show() {
        if (instance == null) {
            instance = new SensorStatusWidgetPane();
            WidgetPaneContainer.addWidgetPane(instance);
        } else {
            WidgetPaneContainer.showWidgetPane(instance);
        }
    }

    private final ListView<EntityView> entityViewListView = new ListView<>();
    private final StatusPane statusPane = new StatusPane();
    private final VBox pane = new VBox();
    private final Timer updateTimer = new Timer("NbcrvRadNucWidgetPane:updateTimer");

    private SensorStatusWidgetPane() {
        initialize();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 1000, 100);
    }

    private void initialize() {
        VBox.setVgrow(entityViewListView, Priority.ALWAYS);
        entityViewListView.setMaxHeight(Double.MAX_VALUE);
        entityViewListView.setCellFactory(entityViewListView1 -> new EntityViewCell());
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof SensorEntity sensorEntity) {
                addEntityViewToList(new EntityView(sensorEntity));
            }
        }
        entityViewListView.getSelectionModel().selectedItemProperty().addListener((observableValue, entityView, t1) -> {
            SensorEntity sensorEntity = t1 == null ? null : t1.entity;
            statusPane.setEntity(sensorEntity);
        });

        pane.getChildren().addAll(entityViewListView, statusPane);

        EntityManager.addEntityListener(this);
    }

    private void addEntityViewToList(EntityView entityView) {
        for (int i = 0; i < entityViewListView.getItems().size(); i++) {
            if (entityViewListView.getItems().get(i).toString().compareToIgnoreCase(entityView.toString()) > 0) {
                entityViewListView.getItems().add(i, entityView);
                return;
            }
        }

        entityViewListView.getItems().add(entityView);
    }

    private void update() {
        boolean needRefresh = false;

        for (EntityView entityView : entityViewListView.getItems()) {
            needRefresh |= entityView.update();
        }

        if (needRefresh) {
            Platform.runLater(entityViewListView::refresh);
        }
    }

    @Override
    public String getName() {
        return "AVCAD";
    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public void dispose() {
        updateTimer.cancel();
        instance = null;
    }

    @Override
    public void onEntityAdded(IEntity entity) {
        //not implemented
    }

    @Override
    public void onEntityUpdated(IEntity entity) {
        //not implemented
    }

    @Override
    public void onEntityDisposed(IEntity entity) {
        EntityView entityViewFound = null;
        for (EntityView entityView : entityViewListView.getItems()) {
            if (entityView.entity.equals(entity)) {
                entityViewFound = entityView;
                break;
            }
        }
        if (entityViewFound != null) {
            removeEntityView(entityViewFound);
        }
    }

    private void removeEntityView(final EntityView entityView) {
        Platform.runLater(() -> {
            entityViewListView.getItems().remove(entityView);
            entityViewListView.refresh();
        });
    }

    public static final class EntityView {

        private final SensorEntity entity;
        private boolean inScope = false;
        private boolean isShutdown = true;
        private boolean isConnected = false;
        private boolean inAlert = false;
        private boolean inAlarm = false;

        public EntityView(SensorEntity entity) {
            this.entity = entity;
        }

        private Node createStatusCircle() {
            if (entity.isInScope() && !entity.isShutdown()) {
                if (entity.isInAlarm()) {
                    return new Circle(10, Color.RED);
                } else if (entity.isInAlert()) {
                    return new Circle(10, Color.YELLOW);
                } else {
                    return new Circle(10, Color.GREEN);
                }
            }
            return new Circle(10, Color.GRAY);
        }

        public Node getStatusCircle() {
            return createStatusCircle();
        }

        public boolean update() {
            boolean hasUpdate = false;

            if (inScope != entity.isInScope()) {
                inScope = entity.isInScope();
                hasUpdate = true;
            }

            if (isShutdown != entity.isShutdown()) {
                isShutdown = entity.isShutdown();
                hasUpdate = true;
            }

            if (isConnected != entity.isConnected()) {
                isConnected = entity.isConnected();
                hasUpdate = true;
            }

            if (inAlert != entity.isInAlert()) {
                inAlert = entity.isInAlert();
                hasUpdate = true;
            }

            if (inAlarm != entity.isInAlarm()) {
                inAlarm = entity.isInAlarm();
                hasUpdate = true;
            }

            return hasUpdate;
        }

        @Override
        public String toString() {
            return entity.getName();
        }
    }

    public static class EntityViewCell extends ListCell<EntityView> {

        public EntityViewCell() {
            getStyleClass().add("root");
            getStyleClass().add("widget-pane");
        }

        @Override
        public void updateItem(EntityView item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.toString());
                setGraphic(item.getStatusCircle());

                if (!item.entity.isInScope() || item.entity.isShutdown()) {
                    setStyle("-fx-font-weight: bold");
                } else if (item.entity.isConnected()) {
                    setStyle("-fx-text-fill:skyblue;-fx-font-weight: bold");
                } else {
                    setStyle("-fx-text-fill:violet;-fx-font-weight: bold");
                }
            }
        }
    }

    private static final class StatusPane extends VBox implements ISensorStatusListener {

        private final TextField timestampTextField = new TextField();
        private final TextArea resultTextArea = new TextArea();
        private final TextField conditionClearedTextField = new TextField();
        private final TextArea alarmAlertsTextArea = new TextArea();
        private final TextField scanTypeTextField = new TextField();
        private final VBox scanTypeVBox = new VBox();

        private StatusPane() {
            initialize();
            update();
        }

        private void initialize() {
            VBox vBox = new VBox();
            vBox.setSpacing(UiConstants.SPACING);
            vBox.setPadding(new Insets(UiConstants.SPACING / 2.0));
            vBox.setStyle("-fx-background-color: derive(-transparent-color, -20%)");

            setPadding(new Insets(UiConstants.SPACING / 2.0));
            getChildren().add(vBox);

            timestampTextField.setEditable(false);
            VBox dataVBox = new VBox(0, new Label("Last Event:"), timestampTextField);
            vBox.getChildren().add(dataVBox);

            resultTextArea.setEditable(false);
            resultTextArea.setPrefColumnCount(20);
            resultTextArea.setPrefRowCount(2);
            dataVBox = new VBox(0, new Label("Result:"), resultTextArea);
            vBox.getChildren().add(dataVBox);

            alarmAlertsTextArea.setEditable(false);
            alarmAlertsTextArea.setPrefColumnCount(20);
            alarmAlertsTextArea.setPrefRowCount(2);
            dataVBox = new VBox(0, new Label("Alarm/Alerts:"), alarmAlertsTextArea);
            vBox.getChildren().add(dataVBox);

            conditionClearedTextField.setEditable(false);
            dataVBox = new VBox(0, new Label("Condition Cleared:"), conditionClearedTextField);
            vBox.getChildren().add(dataVBox);

            scanTypeTextField.setEditable(false);
            scanTypeVBox.getChildren().addAll(new Label("Scan Type:"), scanTypeTextField);
            vBox.getChildren().add(scanTypeVBox);
        }

        private SensorEntity currentEntity = null;

        private void setEntity(SensorEntity sensorEntity) {
            if (currentEntity != null) {
                currentEntity.removeListener(this);
            }
            currentEntity = sensorEntity;
            if (currentEntity != null) {
                currentEntity.addListener(this);
                update();
            }
        }

        private void update() {
            SensorStatus sensorStatus = currentEntity == null ? null :
                    currentEntity.isInScope() ? currentEntity.getCurrentSensorStatus() : null;
            List<AlarmAlert> alarmAlerts = currentEntity == null ? null :
                    currentEntity.isInScope() ? currentEntity.getCurrentAlarmAlertList() : null;
            changed(sensorStatus, alarmAlerts, alarmAlerts != null);
        }

        @Override
        public void changed(final SensorStatus sensorStatus, final List<AlarmAlert> alarmAlerts, final boolean alarmAlertChange) {
            Platform.runLater(() -> {
                setVisible(sensorStatus != null);
                if (sensorStatus != null) {
                    timestampTextField.setText(Utility.formatTime(sensorStatus.getTimestamp()));
                    resultTextArea.setText(sensorStatus.getResult());
                    conditionClearedTextField.setText(sensorStatus.getConditionCleared());
                    if (alarmAlertChange) {
                        StringBuilder text = new StringBuilder();
                        String prefix = "";
                        for (AlarmAlert alarmAlert : alarmAlerts) {
                            text.append(prefix);
                            text.append(alarmAlert.toString());
                            prefix = ", ";

                        }
                        alarmAlertsTextArea.setText(text.toString());
                    }
                    scanTypeVBox.setVisible(!sensorStatus.getScanType().equalsIgnoreCase("none"));
                    scanTypeTextField.setText(sensorStatus.getScanType());
                }
            });
        }
    }
}
