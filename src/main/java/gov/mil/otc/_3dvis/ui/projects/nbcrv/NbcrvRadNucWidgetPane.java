package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.entity.IconImageHelper;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvEntity;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvState;
import gov.mil.otc._3dvis.project.nbcrv.RadNucState;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class NbcrvRadNucWidgetPane implements IWidgetPane {

    public static void show(IEntity entity, boolean show) {
        if (show && nbcrvRadNucWidgetPane == null) {
            nbcrvRadNucWidgetPane = create(entity);
            if (nbcrvRadNucWidgetPane != null) {
                WidgetPaneContainer.showWidgetRightPane(nbcrvRadNucWidgetPane);
            }
        } else {
            if (nbcrvRadNucWidgetPane != null) {
                WidgetPaneContainer.hideWidgetRightPane(nbcrvRadNucWidgetPane);
                nbcrvRadNucWidgetPane = null;
            }
        }
    }

    public static boolean isShowing(IEntity entity) {
        if (nbcrvRadNucWidgetPane == null) {
            return false;
        } else {
            return nbcrvRadNucWidgetPane.entity.equals(entity);
        }
    }

    private static final String FONT_COLOR = "-fx-text-fill:black";
    private static final String MEASUREMENT_FORMAT = "%.2f\u00B5Gy/hr";
    private static final int OUTER_RADIUS = 90;
    private static NbcrvRadNucWidgetPane nbcrvRadNucWidgetPane = null;
    private final VBox mainVBox = new VBox();
    private final NbcrvEntity entity;
    private final Timer updateTimer = new Timer("NbcrvRadNucWidgetPane:updateTimer");
    private final Region region1 = new Region();
    private final Region region2 = new Region();
    private final Region region3 = new Region();
    private final Region region4 = new Region();
    private final Label label1 = new Label(String.format(MEASUREMENT_FORMAT, 0.0));
    private final Label label2 = new Label(String.format(MEASUREMENT_FORMAT, 0.0));
    private final Label label3 = new Label(String.format(MEASUREMENT_FORMAT, 0.0));
    private final Label label4 = new Label(String.format(MEASUREMENT_FORMAT, 0.0));
    private final ImageView imageView1 = new ImageView(ImageLoader.getFxImage("/images/nbcrv_icon.png"));
    private final GridPane gridPane = new GridPane();

    private static NbcrvRadNucWidgetPane create(IEntity entity) {
        if (entity instanceof NbcrvEntity) {
            return new NbcrvRadNucWidgetPane((NbcrvEntity) entity);
        }
        return null;
    }

    private NbcrvRadNucWidgetPane(NbcrvEntity entity) {
        this.entity = entity;
        initialize();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 1000, 100);
    }

    private void initialize() {
        BufferedImage bufferedImage = IconImageHelper.getImage(entity.getLastEntityDetail().getMilitarySymbol(), 40, 40);
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        ImageView imageView2 = new ImageView(image);

        gridPane.setHgap(1.0);
        gridPane.setVgap(1.0);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(0).setMinWidth(OUTER_RADIUS);
        gridPane.getColumnConstraints().get(0).setMaxWidth(OUTER_RADIUS);
        gridPane.getColumnConstraints().get(1).setMinWidth(OUTER_RADIUS);
        gridPane.getColumnConstraints().get(1).setMaxWidth(OUTER_RADIUS);
        gridPane.getColumnConstraints().get(1).setHalignment(HPos.LEFT);
        gridPane.getRowConstraints().add(new RowConstraints());
        gridPane.getRowConstraints().add(new RowConstraints());
        gridPane.getRowConstraints().get(0).setMinHeight(OUTER_RADIUS);
        gridPane.getRowConstraints().get(0).setMaxHeight(OUTER_RADIUS);
        gridPane.getRowConstraints().get(1).setMinHeight(OUTER_RADIUS);
        gridPane.getRowConstraints().get(1).setMaxHeight(OUTER_RADIUS);

        region1.setStyle(String.format("-fx-background-color:green; -fx-background-radius: 0 %d 0 0;", OUTER_RADIUS));
        region2.setStyle(String.format("-fx-background-color:green; -fx-background-radius: 0 0 %d 0;", OUTER_RADIUS));
        region3.setStyle(String.format("-fx-background-color:green; -fx-background-radius: 0 0 0 %d;", OUTER_RADIUS));
        region4.setStyle(String.format("-fx-background-color:green; -fx-background-radius: %d 0 0 0;", OUTER_RADIUS));
        label1.setStyle(FONT_COLOR);
        label2.setStyle(FONT_COLOR);
        label3.setStyle(FONT_COLOR);
        label4.setStyle(FONT_COLOR);
        label1.setRotate(45);
        label2.setRotate(135);
        label3.setRotate(225);
        label4.setRotate(-45);
        gridPane.add(new StackPane(region1, label1), 1, 0);
        gridPane.add(new StackPane(region2, label2), 1, 1);
        gridPane.add(new StackPane(region3, label3), 0, 1);
        gridPane.add(new StackPane(region4, label4), 0, 0);

        StackPane stackPane = new StackPane(gridPane, new Circle(50), imageView1, imageView2);
        stackPane.setPadding(new Insets(20, 20, 0, 0));

        mainVBox.getChildren().addAll(stackPane);
        mainVBox.setPickOnBounds(false);
    }

    private Double heading = null;
    private double measure1 = 0.0;
    private double measure2 = 0.0;
    private double measure3 = 0.0;
    private double measure4 = 0.0;

    private void update() {
        Double newHeading = null;
        double newMeasure1 = 0.0;
        double newMeasure2 = 0.0;
        double newMeasure3 = 0.0;
        double newMeasure4 = 0.0;

        RadNucState radNucState = entity.getCurrentRadNucState();
        if (radNucState != null) {
            newMeasure1 = radNucState.getQ1Measurement() * 60 * 60 * 1000000;
            newMeasure2 = radNucState.getQ2Measurement() * 60 * 60 * 1000000;
            newMeasure3 = radNucState.getQ3Measurement() * 60 * 60 * 1000000;
            newMeasure4 = radNucState.getQ4Measurement() * 60 * 60 * 1000000;
        }
        NbcrvState nbcrvState = entity.getCurrentState();
        if (nbcrvState != null) {
            newHeading = nbcrvState.getYaw();
        }

        if ((newHeading == null && heading != null) ||
                (newHeading != null && !newHeading.equals(heading)) ||
                newMeasure1 != measure1 ||
                newMeasure2 != measure2 ||
                newMeasure3 != measure3 ||
                newMeasure4 != measure4) {
            heading = newHeading;
            measure1 = newMeasure1;
            measure2 = newMeasure2;
            measure3 = newMeasure3;
            measure4 = newMeasure4;
            updateValues(heading, measure1, measure2, measure3, measure4);
        }
    }

    private void updateValues(final Double heading, final double measure1, final double measure2,
                              final double measure3, final double measure4) {
        Platform.runLater(() -> {
            if (heading == null) {
                imageView1.setRotate(180);
                gridPane.setRotate(0);
            } else {
                double viewPointHeading = WWController.getView() == null ? 0.0 : WWController.getView().getHeading().degrees;
                double headingWithView = heading + viewPointHeading;
                imageView1.setRotate(headingWithView + 180);
                gridPane.setRotate(headingWithView);
                String color1 = getColor(measure1);
                String color2 = getColor(measure2);
                String color3 = getColor(measure3);
                String color4 = getColor(measure4);
                region1.setStyle(String.format("-fx-background-color:%s; -fx-background-radius: 0 %d 0 0;", color1, OUTER_RADIUS));
                region2.setStyle(String.format("-fx-background-color:%s; -fx-background-radius: 0 0 %d 0;", color2, OUTER_RADIUS));
                region3.setStyle(String.format("-fx-background-color:%s; -fx-background-radius: 0 0 0 %d;", color3, OUTER_RADIUS));
                region4.setStyle(String.format("-fx-background-color:%s; -fx-background-radius: %d 0 0 0;", color4, OUTER_RADIUS));
                label1.setText(String.format(MEASUREMENT_FORMAT, measure1));
                label2.setText(String.format(MEASUREMENT_FORMAT, measure2));
                label3.setText(String.format(MEASUREMENT_FORMAT, measure3));
                label4.setText(String.format(MEASUREMENT_FORMAT, measure4));
            }
        });
    }

    private String getColor(double measurement) {
        if (measurement >= SettingsManager.getSettings().getNbcrvSettings().getRadNucThreshold("Critical")) {
            return "red";
        } else if (measurement >= SettingsManager.getSettings().getNbcrvSettings().getRadNucThreshold("Marginal")) {
            return "orange";
        } else if (measurement >= SettingsManager.getSettings().getNbcrvSettings().getRadNucThreshold("Negligible")) {
            return "yellow";
        } else {
            return "green";
        }
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public Pane getPane() {
        return mainVBox;
    }

    @Override
    public void dispose() {
        updateTimer.cancel();
    }
}
