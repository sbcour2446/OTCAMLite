package gov.mil.otc._3dvis.playback.dataset.rpuas;

import gov.mil.otc._3dvis.data.ulog.ULogFile;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.playback.dataset.tspi.TspiImportObject;
import gov.mil.otc._3dvis.project.rpuas.BusDataCsv;
import gov.mil.otc._3dvis.project.rpuas.RpuasEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BusImportFolder extends ImportFolder {

    public static boolean isImportFolder(File folder) {
        return folder.isDirectory() &&
                (folder.getName().equalsIgnoreCase("bus") ||
                        folder.getName().equalsIgnoreCase("flight-logs"));
    }

    public static BusImportFolder scanAndCreate(File folder) {
        BusImportFolder busImportFolder = new BusImportFolder(folder);
        if (busImportFolder.validateAndScan()) {
            return busImportFolder;
        }
        return null;
    }

    private final List<TspiImportObject> tspiImportObjectList = new ArrayList<>();
    private long startTime = Long.MAX_VALUE;
    private long stopTime = 0;

    protected BusImportFolder(File folder) {
        super(folder);
    }

    public List<TspiImportObject> getTspiImportObjectList() {
        return tspiImportObjectList;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    @Override
    protected boolean scanFiles(File[] files) {
        getFiles(getObject());
        return true;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (TspiImportObject tspiImportObject : tspiImportObjectList) {
            listView.getItems().add(tspiImportObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        // requires entity
    }

    @Override
    public void doImport(IEntity entity) {
        startTime = Long.MAX_VALUE;

        if (!(entity instanceof RpuasEntity rpuasEntity)) {
            return;
        }

        for (TspiImportObject tspiImportObject : tspiImportObjectList) {
            tspiImportObject.importObject(entity, scanStatusLine);
            long newTime = tspiImportObject.getFirstTimestamp();
            if (startTime > newTime) {
                startTime = newTime;
            }
            if (stopTime < newTime) {
                stopTime = newTime;
            }

            if (tspiImportObject instanceof BusDataCsvImportObject busDataCsvImportObject) {
                rpuasEntity.addValuePairTimedData(busDataCsvImportObject.getBusDataCsv().getValueMapList());
            } else if (tspiImportObject instanceof ULogImportObject uLogImportObject) {
                rpuasEntity.addValuePairTimedData(uLogImportObject.getULogFile().getOtherGpsData());
            }
        }
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        return null;
    }

    private void getFiles(File parentFile) {
        File[] files = parentFile.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.isDirectory()) {
                getFiles(file);
            } else if (file.getName().toLowerCase().endsWith(".csv")) {
                BusDataCsv busDataCsv = new BusDataCsv(file);
                TspiImportObject tspiImportObject = new BusDataCsvImportObject(busDataCsv, file.getName());
                if (tspiImportObject.determineIsNew()) {
                    setModified(true);
                    if (!busDataCsv.processFile() || busDataCsv.getTspiDataList().isEmpty()) {
                        tspiImportObject.setMissing(true);
                    }
                }
                tspiImportObjectList.add(tspiImportObject);
            } else if (file.getName().toLowerCase().endsWith(".ulg")) {
                ULogFile uLogFile = new ULogFile(file);
                TspiImportObject tspiImportObject = new ULogImportObject(uLogFile, file.getName());
                if (tspiImportObject.determineIsNew()) {
                    setModified(true);
                    if (!uLogFile.process() || uLogFile.getTspiDataList().isEmpty()) {
                        tspiImportObject.setMissing(true);
                    }
                }
                tspiImportObjectList.add(tspiImportObject);
            }
        }

        if (tspiImportObjectList.isEmpty()) {
            setMissing(true);
        }
    }
}
