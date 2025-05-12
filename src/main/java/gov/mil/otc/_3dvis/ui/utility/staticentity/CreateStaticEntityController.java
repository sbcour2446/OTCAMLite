package gov.mil.otc._3dvis.ui.utility.staticentity;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.IconImageHelper;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.staticentity.StaticEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.UtilityPane;
import gov.mil.otc._3dvis.ui.widgets.DateTimePicker2;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import gov.mil.otc._3dvis.ui.widgets.coordinates.PositionPicker;
import gov.mil.otc._3dvis.ui.widgets.entity.entitytype.EntityTypePicker;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerValidationListener;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateStaticEntityController extends UtilityPane {

    public static void show(Position position) {
        show(null, position);
    }

    public static void show(IEntity entity) {
        show(entity, entity.getPosition());
    }

    private static synchronized void show(IEntity entity, Position position) {
        if (singleton == null) {
            singleton = new CreateStaticEntityController(entity, position);
            singleton.createAndShow(Pos.TOP_RIGHT);
        }
    }

    private static void dispose() {
        singleton = null;
    }

    private static final String STATIC = "Static";
    private static CreateStaticEntityController singleton;
    private final Label entityIdLabel = new Label("auto generated");
    private final TextField nameTextField = new TextField();
    private final Hyperlink entityTypeHyperlink = new Hyperlink();
    private final TextField militarySymbolTextField = new TextField();
    private final ComboBox<Affiliation> affiliationComboBox = new ComboBox<>();
    private final TextField urnTextField = new TextField("0");
    private final Hyperlink positionHyperlink = new Hyperlink();
    private final CheckBox scopeCheckBox = new CheckBox("enable");
    private final DateTimePicker2 startDateTimePicker = new DateTimePicker2(0);
    private final DateTimePicker2 stopDateTimePicker = new DateTimePicker2(0);
    private final ToggleGroup iconToggleGroup = new ToggleGroup();
    private final RadioButton customImageRadioButton = new RadioButton();
    private final ImageView iconImageView;
    private final ImageView customImageView;
    private final ImageView pinIcon;
    private final ImageView squareIcon;
    private final ColorPicker otherColorPicker = new ColorPicker();
    private Position position;
    private IEntity entity = null;
    private EntityType entityType = EntityType.createUnknown();

    private CreateStaticEntityController(IEntity entity, Position position) {
        this.entity = entity;
        this.position = position;

        Image image = SwingFXUtils.toFXImage(IconImageHelper.getIcon(Affiliation.UNKNOWN), null);
        iconImageView = new ImageView(image);
        customImageView = new ImageView();
        pinIcon = new ImageView();
        squareIcon = new ImageView();
    }

    @Override
    protected String getTitle() {
        return "Create Static Entity";
    }

    @Override
    protected Pane createContentPane() {
        GridPane gridPane = new GridPane();
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().getFirst().setHalignment(HPos.RIGHT);

        int rowIndex = 0;

        gridPane.add(createTitleCell(new TextWithStyleClass("Entity ID:")), 0, rowIndex);
        gridPane.add(createCell(entityIdLabel), 1, rowIndex);

        rowIndex++;

        gridPane.add(createTitleCell(new TextWithStyleClass("Name:")), 0, rowIndex);
        gridPane.add(createCell(nameTextField), 1, rowIndex);

        rowIndex++;

        entityTypeHyperlink.setText(entityType.toString());
        entityTypeHyperlink.setOnAction(event -> selectEntityType());

        gridPane.add(createTitleCell(new TextWithStyleClass("Entity Type:")), 0, rowIndex);
        gridPane.add(createCell(entityTypeHyperlink), 1, rowIndex);

        rowIndex++;

        Hyperlink updateImageHyperlink = new Hyperlink("update image");
        updateImageHyperlink.setOnAction(event -> updateIconImage());
        HBox militarySymbolHBox = new HBox(UiConstants.SPACING, militarySymbolTextField, updateImageHyperlink);

        gridPane.add(createTitleCell(new TextWithStyleClass("Military Symbol:")), 0, rowIndex);
        gridPane.add(createCell(militarySymbolHBox), 1, rowIndex);

        rowIndex++;

        for (Affiliation affiliation : Affiliation.values()) {
            affiliationComboBox.getItems().add(affiliation);
            affiliationComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateMilitarySymbol());
        }
        affiliationComboBox.getSelectionModel().select(Affiliation.NONPARTICIPANT);

        gridPane.add(createTitleCell(new TextWithStyleClass("Affiliation:")), 0, rowIndex);
        gridPane.add(createCell(affiliationComboBox), 1, rowIndex);

        rowIndex++;

        urnTextField.setMaxWidth(Double.MAX_VALUE);
        urnTextField.textProperty().addListener(new IntegerValidationListener(urnTextField, 0,
                Integer.MAX_VALUE));

        gridPane.add(createTitleCell(new TextWithStyleClass("URN:")), 0, rowIndex);
        gridPane.add(createCell(urnTextField), 1, rowIndex);

        rowIndex++;

        positionHyperlink.setText(formatPosition());
        positionHyperlink.setOnAction(event -> selectPosition());

        gridPane.add(createTitleCell(new TextWithStyleClass("Location:")), 0, rowIndex);
        gridPane.add(createCell(positionHyperlink), 1, rowIndex);

        rowIndex++;

        gridPane.add(createTitleCell(new TextWithStyleClass("Scope:")), 0, rowIndex);
        gridPane.add(createCell(scopeCheckBox), 1, rowIndex);

        rowIndex++;

        startDateTimePicker.setDisable(true);
        gridPane.add(createTitleCell(new Pane()), 0, rowIndex);
        gridPane.add(createCell(startDateTimePicker), 1, rowIndex);

        rowIndex++;

        stopDateTimePicker.setDisable(true);
        gridPane.add(createTitleCell(new Pane()), 0, rowIndex);
        gridPane.add(createCell(stopDateTimePicker), 1, rowIndex);

        rowIndex++;

        Pane imageTilePane = createImageSelectionPane();

        gridPane.add(createTitleCell(new TextWithStyleClass("Icon:")), 0, rowIndex, 1, 2);
        gridPane.add(createCell(imageTilePane), 1, rowIndex);

        Button createButton = new Button("Create");
        createButton.setOnAction(event -> onCreate());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        HBox closeButtonHBox = new HBox(UiConstants.SPACING, createButton, cancelButton);
        closeButtonHBox.setAlignment(Pos.BASELINE_RIGHT);
        closeButtonHBox.setPadding(new Insets(UiConstants.SPACING));

        VBox mainVBox = new VBox(gridPane, new Separator(), closeButtonHBox);

        initializeFromEntity();

        return mainVBox;
    }

    @Override
    protected boolean closeRequested() {
        dispose();
        return true;
    }

    private Pane createTitleCell(Node node) {
        StackPane stackPane = new StackPane(node);
        stackPane.getStyleClass().add("title-cell");
        StackPane.setAlignment(node, Pos.CENTER_RIGHT);
        stackPane.setPadding(new Insets(UiConstants.SPACING / 2.0, UiConstants.SPACING, UiConstants.SPACING / 2.0, UiConstants.SPACING));
        return stackPane;
    }

    private Pane createCell(Node node) {
        StackPane stackPane = new StackPane(node);
        stackPane.getStyleClass().add("cell");
        StackPane.setAlignment(node, Pos.CENTER_LEFT);
        stackPane.setPadding(new Insets(UiConstants.SPACING / 2.0, UiConstants.SPACING, UiConstants.SPACING / 2.0, UiConstants.SPACING));
        return stackPane;
    }

    private Pane createImageSelectionPane() {
        String[] staticImages = {
                "/images/static/static_0.png",
                "/images/static/static_1.png",
                "/images/static/static_2.png",
                "/images/static/static_3.png",
                "/images/static/static_4.png",
                "/images/static/static_5.png",
                "/images/static/static_6.png",
                "/images/static/static_7.png",
                "/images/static/static_8.png",
                "/images/static/static_9.png",
        };

        TilePane imageTilePane = new TilePane();
        imageTilePane.setPrefColumns(6);
        imageTilePane.setVgap(5);
        ImageView imageView;
        RadioButton radioButton;

        if (entity instanceof StaticEntity) {
            BufferedImage bufferedImage = entity.getIcon();
            imageView = new ImageView(SwingFXUtils.toFXImage(bufferedImage, null));
            radioButton = new RadioButton();
            radioButton.setToggleGroup(iconToggleGroup);
            radioButton.setUserData("");
            radioButton.setAlignment(Pos.CENTER);
            VBox vBox = new VBox(0, imageView, radioButton);
            vBox.setAlignment(Pos.BOTTOM_CENTER);
            imageTilePane.getChildren().add(vBox);
        }

        for (String imageName : staticImages) {
            imageView = new ImageView(ImageLoader.getFxImage(imageName));
            radioButton = new RadioButton();
            radioButton.setToggleGroup(iconToggleGroup);
            radioButton.setUserData(imageName);
            radioButton.setAlignment(Pos.CENTER);
            VBox vBox = new VBox(0, imageView, radioButton);
            vBox.setAlignment(Pos.BOTTOM_CENTER);
            imageTilePane.getChildren().add(vBox);
        }

        Image image = SwingFXUtils.toFXImage(IconImageHelper.getPinIcon(java.awt.Color.BLACK, false), null);
        pinIcon.setImage(image);
//        imageView = new ImageView(image);
        radioButton = new RadioButton();
        radioButton.setToggleGroup(iconToggleGroup);
        radioButton.setUserData(pinIcon);
        radioButton.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(0, pinIcon, radioButton);
        vBox.setAlignment(Pos.BOTTOM_CENTER);
        imageTilePane.getChildren().add(vBox);

        image = SwingFXUtils.toFXImage(IconImageHelper.getSquareIcon(java.awt.Color.BLACK, false), null);
        squareIcon.setImage(image);
        radioButton = new RadioButton();
        radioButton.setToggleGroup(iconToggleGroup);
        radioButton.setUserData(squareIcon);
        radioButton.setAlignment(Pos.CENTER);
        vBox = new VBox(0, squareIcon, radioButton);
        vBox.setAlignment(Pos.BOTTOM_CENTER);
        imageTilePane.getChildren().add(vBox);

        radioButton = new RadioButton();
        radioButton.setToggleGroup(iconToggleGroup);
        radioButton.setUserData(iconImageView);
        radioButton.setAlignment(Pos.CENTER);
        vBox = new VBox(0, iconImageView, radioButton);
        vBox.setAlignment(Pos.BOTTOM_CENTER);
        imageTilePane.getChildren().add(vBox);

        Hyperlink hyperlink = new Hyperlink("add");
        hyperlink.setOnAction(event -> selectCustomImage());
        customImageRadioButton.setToggleGroup(iconToggleGroup);
        customImageRadioButton.setUserData("");
        customImageRadioButton.setAlignment(Pos.CENTER);
        vBox = new VBox(0, customImageView, hyperlink, customImageRadioButton);
        vBox.setAlignment(Pos.BOTTOM_CENTER);
        imageTilePane.getChildren().add(vBox);

        iconToggleGroup.selectToggle(iconToggleGroup.getToggles().getFirst());

        otherColorPicker.setOnAction(actionEvent -> {
            if (otherColorPicker.getValue() == null)  {
                return;
            }
            Color color = otherColorPicker.getValue();
            java.awt.Color awtColor = new java.awt.Color((float) color.getRed(),
                    (float) color.getGreen(),
                    (float) color.getBlue(),
                    (float) color.getOpacity());
            Image newImage = SwingFXUtils.toFXImage(IconImageHelper.getPinIcon(
                    awtColor, false), null);
            pinIcon.setImage(newImage);
            newImage = SwingFXUtils.toFXImage(IconImageHelper.getSquareIcon(
                    awtColor, false), null);
            squareIcon.setImage(newImage);
        });
        otherColorPicker.setValue(Color.BLACK);

        return new VBox(UiConstants.SPACING, imageTilePane,
                new HBox(UiConstants.SPACING,new TextWithStyleClass("Other Color:"), otherColorPicker));
        //return imageTilePane;
    }

    private void initializeFromEntity() {
        if (entity != null) {
            entityIdLabel.setText(entity.getEntityId().toString());
            nameTextField.setText(entity.getName());
            EntityDetail entityDetail = entity.getEntityDetail();
            if (entityDetail != null) {
                entityType = entityDetail.getEntityType();
                entityTypeHyperlink.setText(entityType.toString());
                militarySymbolTextField.setText(entityDetail.getMilitarySymbol());
            }
        }
    }

    private void selectEntityType() {
        EntityType selectedEntityType = EntityTypePicker.show(MainApplication.getInstance().getStage(), entityType);
        if (selectedEntityType != null) {
            entityType = selectedEntityType;
            entityTypeHyperlink.setText(entityType.toString());
            String tacticalSymbol = EntityTypeUtility.getTacticalSymbol(entityType);
            if (!tacticalSymbol.isBlank()) {
                militarySymbolTextField.setText(tacticalSymbol);
                updateMilitarySymbol();
            }
        }
    }

    private String formatPosition() {
        return String.format("%3.6f\u00b0, %3.6f\u00b0, %,dm",
                position.getLatitude().degrees, position.getLongitude().degrees,
                (int) Math.round(position.getElevation()));
    }

    private void selectPosition() {
        Position newPosition = PositionPicker.show(MainApplication.getInstance().getStage(), position);
        if (newPosition != null) {
            position = newPosition;
            positionHyperlink.setText(formatPosition());
        }
    }

    private void selectCustomImage() {
        Image imageForSize = SwingFXUtils.toFXImage(IconImageHelper.getIcon(Affiliation.UNKNOWN), null);
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(MainApplication.getInstance().getStage());
        if (file != null) {
            Image image = new Image(file.toURI().toString(), imageForSize.getWidth(), imageForSize.getHeight(),
                    true, true);
            customImageView.setImage(image);
            customImageRadioButton.setUserData(image);
            customImageRadioButton.setSelected(true);
        }
    }

    private void updateMilitarySymbol() {
        String militarySymbol = militarySymbolTextField.getText();
        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, affiliationComboBox.getValue());
        militarySymbolTextField.setText(militarySymbol);
        updateIconImage();
    }

    private void updateIconImage() {
        BufferedImage bufferedImage = null;
        if (!militarySymbolTextField.getText().isBlank()) {
            bufferedImage = IconImageHelper.getIcon(militarySymbolTextField.getText());
        }
        if (bufferedImage == null) {
            bufferedImage = IconImageHelper.getIcon(affiliationComboBox.getValue());
        }
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        iconImageView.setImage(image);
    }

    private void onCreate() {
        int urn = getUrn();
        long startTime = startDateTimePicker.getTimestamp();

        StaticEntity staticEntity;
        if (iconToggleGroup.getSelectedToggle().getUserData() instanceof ImageView imageView) {
            Image image = imageView.getImage();
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            staticEntity = new StaticEntity(DataManager.getNextAvailableEntityId(STATIC), bufferedImage);
        } else {
            staticEntity = new StaticEntity(DataManager.getNextAvailableEntityId(STATIC),
                    iconToggleGroup.getSelectedToggle().getUserData().toString());
        }

        EntityManager.addEntity(staticEntity, true);

        String dataSourceName = "create static " + nameTextField.getText() + " at " + Utility.formatTime(System.currentTimeMillis());
        DataSource dataSource = DataManager.createDataSource(dataSourceName, startTime, -1);

        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(startTime)
                .setEntityType(entityType)
                .setAffiliation(affiliationComboBox.getValue())
                .setName(nameTextField.getText())
                .setSource(STATIC)
                .setMilitarySymbol(militarySymbolTextField.getText())
                .setUrn(urn)
                .build();
        staticEntity.addEntityDetail(entityDetail);
        DatabaseLogger.addEntityDetail(entityDetail, staticEntity.getEntityId(), dataSource.getId());

        EntityScope entityScope = new EntityScope(startTime, Long.MAX_VALUE);
        staticEntity.addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, staticEntity.getEntityId(), dataSource.getId());

        TspiData tspiData = new TspiData(startTime, position);
        staticEntity.addTspi(tspiData);
        DatabaseLogger.addTspiData(tspiData, staticEntity.getEntityId(), dataSource.getId());

        close();
    }

    private int getUrn() {
        try {
            return Integer.parseInt(urnTextField.getText());
        } catch (Exception e) {
            Logger.getGlobal().log(Level.INFO, null, e);
        }
        return 0;
    }
}
