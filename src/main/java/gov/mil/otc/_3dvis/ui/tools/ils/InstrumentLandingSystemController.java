package gov.mil.otc._3dvis.ui.tools.ils;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.project.ils.InstrumentationLandingSystemManager;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.mil.otc._3dvis.worldwindex.terrain.HighResolutionTerrainEx;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.pick.PickedObjectList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.event.MouseAdapter;

public class InstrumentLandingSystemController {

    protected static final int SPACING = 10;
    private static InstrumentLandingSystemController instrumentLandingSystemController;
    private final Stage stage = new Stage();
    private final TextField runwayEndpoint1TextField = new TextField();
    private final TextField runwayEndpoint2TextField = new TextField();

    public static synchronized void show() {
        if (instrumentLandingSystemController == null) {
            instrumentLandingSystemController = new InstrumentLandingSystemController();
        }
        instrumentLandingSystemController.stage.show();
    }

    private InstrumentLandingSystemController() {
        initialize();
    }

    private void initialize() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        TextField nameTextField = new TextField();

        gridPane.add(new Label("Name:"), 0, rowIndex);
        gridPane.add(nameTextField, 1, rowIndex);

        rowIndex++;

        Button runwayEndpoint1Button = new Button();
        runwayEndpoint1Button.setOnAction(event -> selectLocationFromMap(runwayEndpoint1TextField));

        gridPane.add(new Label("Endpoint:"), 0, rowIndex);
        gridPane.add(runwayEndpoint1TextField, 1, rowIndex);
        gridPane.add(runwayEndpoint1Button, 2, rowIndex);

        rowIndex++;

        Button runwayEndpoint2Button = new Button();
        runwayEndpoint2Button.setOnAction(event -> selectLocationFromMap(runwayEndpoint2TextField));

        gridPane.add(new Label("Endpoint:"), 0, rowIndex);
        gridPane.add(runwayEndpoint2TextField, 1, rowIndex);
        gridPane.add(runwayEndpoint2Button, 2, rowIndex);

        Button createButton = new Button("Create");
        createButton.setOnAction(event -> createInstrumentLandingSystem());

        Button deleteButton = new Button("Delete");
        HBox buttonHBox = new HBox(UiConstants.SPACING, createButton, deleteButton);

        VBox mainVBox = new VBox(UiConstants.SPACING, gridPane, buttonHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));

        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Instrument Landing System");
        Scene scene = new Scene(mainVBox);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Instrument Landing System");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
    }

    private void selectLocationFromMap(final TextField textField) {
        textField.setText("select location");
        textField.setStyle("-fx-text-fill: red;");
        WWController.getWorldWindowPanel().getInputHandler().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                PickedObjectList objects = WWController.getWorldWindowPanel().getObjectsAtCurrentPosition();
                boolean isTerrain = objects != null
                        && objects.getTopPickedObject() != null
                        && objects.getTopPickedObject().isTerrain();
                if (isTerrain) {
                    Position position = WWController.getWorldWindowPanel().getCurrentPosition();
                    String endpointText = "";
                    if (position != null) {
                        endpointText = MGRSCoord.fromLatLon(position.getLatitude(), position.getLongitude()).toString();
                    }
                    textField.setText(endpointText);
                    e.consume(); // Prevents globe moving in response to click
                }
                WWController.getWorldWindowPanel().getInputHandler().removeMouseListener(this);
                textField.setStyle(null);
            }
        });
    }

    private void createInstrumentLandingSystem() {
        HighResolutionTerrainEx terrain = new HighResolutionTerrainEx(WWController.getGlobe(), null);
        terrain.setUseCachedElevationsOnly(false);
        terrain.setCacheCapacity((long) 500e6);
        terrain.setTimeout(1000L);
        MGRSCoord mgrsCoord1 = MGRSCoord.fromString(runwayEndpoint1TextField.getText(), WWController.getGlobe());
        double elevation1 = terrain.getElevation(new LatLon(mgrsCoord1.getLatitude(), mgrsCoord1.getLongitude()));
        Position position1 = new Position(mgrsCoord1.getLatitude(), mgrsCoord1.getLongitude(), elevation1);
        MGRSCoord mgrsCoord2 = MGRSCoord.fromString(runwayEndpoint2TextField.getText(), WWController.getGlobe());
        double elevation2 = terrain.getElevation(new LatLon(mgrsCoord2.getLatitude(), mgrsCoord2.getLongitude()));
        Position position2 = new Position(mgrsCoord2.getLatitude(), mgrsCoord2.getLongitude(), elevation2);
        InstrumentationLandingSystemManager.create(position1, position2);
    }
}
