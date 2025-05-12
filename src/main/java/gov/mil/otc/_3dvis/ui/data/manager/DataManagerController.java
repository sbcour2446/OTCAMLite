package gov.mil.otc._3dvis.ui.data.manager;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.utility.StageUtility;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.List;

public class DataManagerController {

    private static final int MIN_WIDTH = 500;
    private static DataManagerController dataManagerWindow = null;
    private final Stage stage = new Stage();
    private final CheckBox useRemoteDatabaseCheckBox = new CheckBox("use remote database");
    private final TextField remoteDatabaseTextField = new TextField();
    private final CheckBox enableLoggingCheckBox = new CheckBox("enable real-time logging");
    private final CheckBox loadOnStartupCheckBox = new CheckBox("load database on startup");
    private final Button applyButton = new Button("Apply");
    private final TabPane tabPane = new TabPane();

    public static synchronized void show() {
        if (dataManagerWindow == null) {
            dataManagerWindow = new DataManagerController();
        }
        dataManagerWindow.stage.show();
    }

    private static void dispose() {
        dataManagerWindow = null;
    }

    private DataManagerController() {
        Label optionsLabel = new Label("Options");
        optionsLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        useRemoteDatabaseCheckBox.setSelected(SettingsManager.getSettings().useRemoteDatabase());
        useRemoteDatabaseCheckBox.setOnAction(event -> updateButtons());

        remoteDatabaseTextField.setText(SettingsManager.getSettings().getRemoteDatabase());
        HBox.setHgrow(remoteDatabaseTextField, Priority.ALWAYS);

        Button remoteDatabaseButton = new Button("...");
        remoteDatabaseButton.setOnAction(event -> selectRemoteDatabase());

        HBox useRemoteDatabaseHBox = new HBox(UiConstants.SPACING, useRemoteDatabaseCheckBox, remoteDatabaseTextField,
                remoteDatabaseButton);
        useRemoteDatabaseHBox.setAlignment(Pos.CENTER_LEFT);

        enableLoggingCheckBox.setSelected(SettingsManager.getSettings().isRealTimeLogging());
        enableLoggingCheckBox.setOnAction(event -> updateButtons());

        loadOnStartupCheckBox.setSelected(SettingsManager.getSettings().isLoadDatabaseOnStartup());
        loadOnStartupCheckBox.setOnAction(event -> updateButtons());

        applyButton.setOnAction(event -> applyChanges());

        GridPane optionsGridPane = new GridPane();
        optionsGridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        optionsGridPane.setHgap(UiConstants.SPACING);
        optionsGridPane.setVgap(UiConstants.SPACING);
        optionsGridPane.getColumnConstraints().add(new ColumnConstraints());
        optionsGridPane.getColumnConstraints().get(0).setHgrow(Priority.ALWAYS);
        int index = 0;
        optionsGridPane.addRow(index++, useRemoteDatabaseHBox);
        optionsGridPane.addRow(index++, enableLoggingCheckBox);
        optionsGridPane.addRow(index++, loadOnStartupCheckBox);
        optionsGridPane.addRow(index++, applyButton);
        for (int i = 0; i < index; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / index);
            optionsGridPane.getRowConstraints().add(rowConstraints);
        }

        Label dataSourceLabel = new Label("Data Sources");
        dataSourceLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        VBox topVBox = new VBox(UiConstants.SPACING,
                optionsLabel,
                optionsGridPane,
                new Separator(),
                dataSourceLabel);
        topVBox.setPadding(new Insets(0, 0, UiConstants.SPACING, 0));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> {
            stage.close();
            dispose();
        });

        HBox closeHBox = new HBox(closeButton);
        closeHBox.setAlignment(Pos.CENTER_RIGHT);
        closeHBox.setPadding(new Insets(UiConstants.SPACING, 0, 0, 0));

        BorderPane borderPane = new BorderPane(createDataSourcePane(), topVBox, null, closeHBox, null);
        borderPane.setPadding(new Insets(UiConstants.SPACING));
        borderPane.setMinWidth(MIN_WIDTH);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setTitle("Data Manager");

        Scene scene = new Scene(borderPane, 800, 600);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageUtility.centerStage(stage, MainApplication.getInstance().getStage());

        stage.setOnCloseRequest(event -> dispose());
    }

    public Stage getStage() {
        return stage;
    }

    protected void loadDataSources() {
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof DataSourceTab) {
                ((DataSourceTab) tab).load();
            } else if (tab instanceof ByMissionDataSourcePane) {
                ((ByMissionDataSourcePane) tab).load();
            }
        }
    }

    private TabPane createDataSourcePane() {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        DataSourceTab dataSourceTab = new DataSourceTab("All", this);
        dataSourceTab.load();
        tabPane.getTabs().add(dataSourceTab);

        List<Mission> missions = DataManager.getMissions();
        if (!DataManager.getMissions().isEmpty()) {
            tabPane.getTabs().add(new ByMissionDataSourcePane(missions, this));
        }

        return tabPane;
    }

    private void updateButtons() {
        if (useRemoteDatabaseCheckBox.isSelected() != SettingsManager.getSettings().useRemoteDatabase() ||
                !remoteDatabaseTextField.getText().equalsIgnoreCase(SettingsManager.getSettings().getRemoteDatabase()) ||
                enableLoggingCheckBox.isSelected() != SettingsManager.getSettings().isRealTimeLogging() ||
                loadOnStartupCheckBox.isSelected() != SettingsManager.getSettings().isLoadDatabaseOnStartup()) {
            applyButton.setDisable(false);
        } else {
            applyButton.setDisable(true);
        }
    }

    private void applyChanges() {
        if (useRemoteDatabaseCheckBox.isSelected() != SettingsManager.getSettings().useRemoteDatabase() ||
                !remoteDatabaseTextField.getText().equalsIgnoreCase(SettingsManager.getSettings().getRemoteDatabase())) {
            if (new File(remoteDatabaseTextField.getText()).exists()) {
                SettingsManager.getSettings().setUseRemoteDatabase(useRemoteDatabaseCheckBox.isSelected());
                SettingsManager.getSettings().setRemoteDatabase(remoteDatabaseTextField.getText());
                DataManager.shutdown();
                DataManager.initialize();
            } else {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Could not find database file.", stage);
            }
        }

        if (enableLoggingCheckBox.isSelected() != SettingsManager.getSettings().isRealTimeLogging()) {
            SettingsManager.getSettings().setRealTimeLogging(enableLoggingCheckBox.isSelected());
            DataManager.startStopRealTimeLogging(enableLoggingCheckBox.isSelected());
        }

        if (loadOnStartupCheckBox.isSelected() != SettingsManager.getSettings().isLoadDatabaseOnStartup()) {
            SettingsManager.getSettings().setLoadDatabaseOnStartup(loadOnStartupCheckBox.isSelected());
        }

        updateButtons();
    }

    private void selectRemoteDatabase() {
        File initialDirectory;
        File remoteDatabase = new File(SettingsManager.getSettings().getRemoteDatabase());
        if (remoteDatabase.exists()) {
            initialDirectory = remoteDatabase.getParentFile();
        } else {
            initialDirectory = new File(System.getProperty("user.home"));
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select remote database");
        fileChooser.setInitialDirectory(initialDirectory);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("database(*.db)", "*.db"));
        File file = fileChooser.showOpenDialog(MainApplication.getInstance().getStage());
        if (file != null) {
            remoteDatabaseTextField.setText(file.getAbsolutePath());
            updateButtons();
        }
    }
}
