package gov.mil.otc._3dvis.playback.dataset.csv;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvConfiguration;
import gov.mil.otc._3dvis.playback.ConfigurationManager;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.playback.dataset.tspi.TspiImportFolder;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CsvTspiImportFolder extends TspiImportFolder {

    /* Directory Layout
        'tspi_data_csv'
        |  (tspi csv files)
     */

    public static String FOLDER_NAME = "tspi_data_csv";

    public static CsvTspiImportFolder scanAndCreate(File folder) {
        CsvTspiImportFolder importFolder = new CsvTspiImportFolder(folder);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    public static CsvTspiImportFolder scanAndCreate(File folder, CsvConfiguration csvConfiguration) {
        CsvTspiImportFolder importFolder = new CsvTspiImportFolder(folder, csvConfiguration);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    protected CsvConfigurationImportObject csvConfigurationImportObject = null;
    protected CsvConfiguration csvConfiguration;

    public CsvTspiImportFolder(File folder) {
        super(folder);
    }

    public CsvTspiImportFolder(File folder, CsvConfiguration csvConfiguration) {
        super(folder);
        this.csvConfiguration = csvConfiguration;
    }

    @Override
    protected boolean scanFiles(File[] files) {
        if (csvConfiguration == null) {
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(CsvConfiguration.NAME)) {
                    csvConfiguration = ConfigurationManager.load(file, CsvConfiguration.class);
                    break;
                }
            }
        }

        if (csvConfiguration == null) {
            csvConfiguration = new CsvConfiguration(0, "start field (optional)",
                    1,"timestamp format (YYYY-MM-dd HH:mm:ss.SSS)",
                    false,2024);
            csvConfiguration.addFieldConfiguration(CsvConfiguration.Field.TIMESTAMP,
                    new CsvConfiguration.FieldConfiguration("header name", CsvConfiguration.ValueType.TIMESTAMP));
            String filename = getObject().getAbsolutePath() + File.separator + CsvConfiguration.NAME;
            ConfigurationManager.safeSave(csvConfiguration, filename);
            return false;
        }

        csvConfigurationImportObject = new CsvConfigurationImportObject(csvConfiguration);

        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.isDirectory()) {
                scanForImportObjects(file);
            } else if (file.getName().toLowerCase().endsWith(".csv")) {
                CsvTspiImportObject importObject = CsvTspiImportObject.scanAndCreate(file, csvConfigurationImportObject.getObject());
                importObjectList.add(importObject);
            }
        }
        return true;
    }

    private void scanForImportObjects(File folder) {
        File[] fileList = folder.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (isCancelRequested()) {
                    break;
                }
                if (file.isDirectory()) {
                    scanForImportObjects(file);
                } else if (file.getName().toLowerCase().endsWith(".csv")) {
                    CsvTspiImportObject importObject = CsvTspiImportObject.scanAndCreate(file, csvConfigurationImportObject.getObject());
                    importObjectList.add(importObject);
                }
            }
        }
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        treeItems.add(new TreeItem<>(csvConfigurationImportObject));
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        listView.getItems().add(csvConfigurationImportObject);
        for (ImportObject<?> importObject : importFolderList) {
            listView.getItems().add(importObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }
}
