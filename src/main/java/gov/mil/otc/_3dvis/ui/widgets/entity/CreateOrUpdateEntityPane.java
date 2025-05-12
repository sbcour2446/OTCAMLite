package gov.mil.otc._3dvis.ui.widgets.entity;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.datamodel.EntityTypeUtility;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.entity.entitytype.EntityTypePicker;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerValidationListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateOrUpdateEntityPane extends BorderPane {

    private final Stage parentStage;
    private final RadioButton createNewRadioButton = new RadioButton("create new");
    private final RadioButton updateExistingRadioButton = new RadioButton("update existing");
    private final Label entityIdLabel = new Label("generated");
    private final Hyperlink entityIdHyperlink = new Hyperlink("select");
    private final Label nameLabel = new Label();
    private final TextField nameTextField = new TextField();
    private final Label entityTypeLabel = new Label();
    private final Hyperlink entityTypeHyperlink = new Hyperlink("select");
    private final Label descriptionLabel = new Label();
    private final Label affiliationLabel = new Label();
    private final AffiliationComboBox affiliationComboBox = new AffiliationComboBox();
    private final Label urnLabel = new Label();
    private final TextField urnTextField = new TextField();
    private IEntity entity = null;
    private EntityType entityType = null;
    private List<Class<?>> entityClassFilter = null;

    public CreateOrUpdateEntityPane(Stage parentStage) {
        this.parentStage = parentStage;
        initialize();
    }

    public void setEntityClassFilter(List<Class<?>> entityClassFilter) {
        this.entityClassFilter = entityClassFilter;
    }

    public void setName(String name) {
        nameLabel.setText(name);
        nameTextField.setText(name);
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
        entityTypeLabel.setText(entityType.toString());
        entityTypeHyperlink.setText(entityType.toString());
        descriptionLabel.setText(entityType.getDescription());
    }

    public void setAffiliation(Affiliation affiliation) {
        affiliationLabel.setText(affiliation.getName());
        affiliationComboBox.getSelectionModel().select(affiliation);
    }

    public boolean isCreateNew() {
        return createNewRadioButton.isSelected();
    }

    public IEntity getEntity() {
        if (createNewRadioButton.isSelected()) {
            return null;
        } else {
            return entity;
        }
    }

    public String getName() {
        return nameTextField.getText();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Affiliation getAffiliation() {
        return affiliationComboBox.getSelectionModel().getSelectedItem();
    }

    public int getUrn() {
        int urn = 0;
        if (!urnTextField.getText().isBlank()) {
            try {
                urn = Integer.parseInt(urnTextField.getText());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.INFO, null, e);
            }
        }
        return urn;
    }

    private void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(createNewRadioButton);
        toggleGroup.getToggles().add(updateExistingRadioButton);
        createNewRadioButton.setSelected(true);
        onCreateNewAction();

        createNewRadioButton.setOnAction(event -> onCreateNewAction());
        updateExistingRadioButton.setOnAction(event -> onUpdateExistingAction());

        HBox hBox = new HBox(UiConstants.SPACING, createNewRadioButton, updateExistingRadioButton);
        hBox.setPadding(new Insets(0, UiConstants.SPACING, UiConstants.SPACING, UiConstants.SPACING));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
        gridPane.setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, .1),
                CornerRadii.EMPTY, Insets.EMPTY)));

        int rowIndex = 0;

        entityIdHyperlink.setOnAction(event -> selectEntity());

        StackPane stackPane = new StackPane(entityIdLabel, entityIdHyperlink);
        StackPane.setAlignment(entityIdLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(entityIdHyperlink, Pos.CENTER_LEFT);

        gridPane.add(new Label("Entity ID:"), 0, rowIndex);
        gridPane.add(stackPane, 1, rowIndex);

        rowIndex++;

        nameTextField.setMaxWidth(Double.MAX_VALUE);

        stackPane = new StackPane(nameLabel, nameTextField);
        StackPane.setAlignment(nameLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(nameTextField, Pos.CENTER_LEFT);

        gridPane.add(new Label("Name:"), 0, rowIndex);
        gridPane.add(stackPane, 1, rowIndex);

        rowIndex++;

        entityTypeHyperlink.setOnAction(event -> selectEntityType());

        stackPane = new StackPane(entityTypeLabel, entityTypeHyperlink);
        StackPane.setAlignment(entityTypeLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(entityTypeHyperlink, Pos.CENTER_LEFT);

        gridPane.add(new Label("Entity Type:"), 0, rowIndex);
        gridPane.add(stackPane, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Description:"), 0, rowIndex);
        gridPane.add(descriptionLabel, 1, rowIndex);

        rowIndex++;

        affiliationComboBox.setMaxWidth(Double.MAX_VALUE);
        affiliationComboBox.getSelectionModel().selectFirst();

        stackPane = new StackPane(affiliationLabel, affiliationComboBox);
        StackPane.setAlignment(affiliationLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(affiliationComboBox, Pos.CENTER_LEFT);

        gridPane.add(new Label("Affiliation:"), 0, rowIndex);
        gridPane.add(stackPane, 1, rowIndex);

        rowIndex++;

        urnLabel.setText("0");
        urnTextField.setText("0");
        urnTextField.setMaxWidth(Double.MAX_VALUE);
        urnTextField.textProperty().addListener(new IntegerValidationListener(urnTextField,
                Integer.MIN_VALUE, Integer.MAX_VALUE));

        stackPane = new StackPane(urnLabel, urnTextField);
        StackPane.setAlignment(urnLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(urnTextField, Pos.CENTER_LEFT);

        gridPane.add(new Label("URN:"), 0, rowIndex);
        gridPane.add(stackPane, 1, rowIndex);

        setTop(hBox);
        setCenter(gridPane);
    }

    private void onCreateNewAction() {
        entityIdLabel.setVisible(true);
        entityIdHyperlink.setVisible(false);
        nameLabel.setVisible(false);
        nameTextField.setVisible(true);
        entityTypeLabel.setVisible(false);
        entityTypeHyperlink.setVisible(true);
        affiliationLabel.setVisible(false);
        affiliationComboBox.setVisible(true);
        urnLabel.setVisible(false);
        urnTextField.setVisible(true);
    }

    private void onUpdateExistingAction() {
        entityIdLabel.setVisible(false);
        entityIdHyperlink.setVisible(true);
        nameLabel.setVisible(true);
        nameTextField.setVisible(false);
        entityTypeLabel.setVisible(true);
        entityTypeHyperlink.setVisible(false);
        affiliationLabel.setVisible(true);
        affiliationComboBox.setVisible(false);
        urnLabel.setVisible(true);
        urnTextField.setVisible(false);

        if (entity == null) {
            selectEntity();
        }
    }

    private void selectEntity() {
        entity = EntityPicker.show(parentStage, entityClassFilter);
        if (entity != null) {
            entityIdHyperlink.setText(entity.getEntityId().toString());
            EntityDetail entityDetail = entity.getLastEntityDetail();
            if (entityDetail != null) {
                nameLabel.setText(entityDetail.getName());
                nameTextField.setText(entityDetail.getName());
                entityType = entityDetail.getEntityType();
                entityTypeLabel.setText(entityType.toString());
                entityTypeHyperlink.setText(entityType.toString());
                descriptionLabel.setText(entityType.getDescription());
                affiliationLabel.setText(entityDetail.getAffiliation().toString());
                affiliationComboBox.getSelectionModel().select(entityDetail.getAffiliation());
                urnLabel.setText(String.valueOf(entityDetail.getUrn()));
                urnTextField.setText(String.valueOf(entityDetail.getUrn()));
            }
        }
    }

    private void selectEntityType() {
        EntityType selectedEntityType = EntityTypePicker.show(parentStage, entityType);
        if (selectedEntityType != null) {
            entityType = selectedEntityType;
            entityTypeHyperlink.setText(entityType.toString());
            entityTypeLabel.setText(EntityTypeUtility.getDescription(entityType));
            descriptionLabel.setText(entityType.getDescription());
        }
    }
}
