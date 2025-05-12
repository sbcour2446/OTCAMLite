package gov.mil.otc._3dvis.playback.dataset.mrwr;

import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityConfiguration;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityConfigurationImportObject;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityImportFolder;
import gov.mil.otc._3dvis.playback.dataset.media.MediaImportFolder;
import gov.mil.otc._3dvis.project.mrwr.ApacheEntity;
import gov.mil.otc._3dvis.project.mrwr.CcmLogFile;
import gov.mil.otc._3dvis.project.mrwr.ThreatDataFile;
import gov.mil.otc._3dvis.project.mrwr.ThreatEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreatEntityImportFolder extends EntityImportFolder {

    /* Directory Layout
        {entity name}
        |  'data' //threat data files, tab delimited
        |  'video' //media folder set
     */

    private final List<ThreatDataFile> threatDataFileList = new ArrayList<>();
    private final List<CcmLogFile> ccmLogFileList = new ArrayList<>();

    public ThreatEntityImportFolder(File folder, EntityConfigurationImportObject entityConfigurationImportObject) {
        super(folder, entityConfigurationImportObject);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                if (MediaImportFolder.isMediaFolder(file)) {
                    mediaImportFolder = new MediaImportFolder(file, MediaTimestampFormat.DAY_OF_YEAR_MILLI);
                    mediaImportFolder.validateAndScan(this);
                } else {
                    loadThreatDataFiles(file);
                }
            }
        }

        if (entityConfigurationImportObject == null) {
            setMissing(true);
        }

        return true;
    }

    private void loadThreatDataFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                loadThreatDataFiles(file);
            } else if (file.getName().toLowerCase().endsWith("txt")) {
                ThreatDataFile threatDataFile = new ThreatDataFile(file);
                if (threatDataFile.processFile()) {
                    threatDataFileList.add(threatDataFile);
                }
            } else if (file.getName().toLowerCase().endsWith("csv")) {
                CcmLogFile ccmLogFile = new CcmLogFile(file);
                if (ccmLogFile.processFile()) {
                    ccmLogFileList.add(ccmLogFile);
                }
            }
        }
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        if (mediaImportFolder != null) {
            treeItems.add(mediaImportFolder.getTreeItem());
        }
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        ListView<String> listView = new ListView<>();
        for (ThreatDataFile threatDataFile : threatDataFileList) {
            listView.getItems().add(threatDataFile.getFile().getName());
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        StringBuilder infoStringBuilder = new StringBuilder("ThreatEntityImportFolder::doImport:");

        EntityConfiguration entityConfiguration = entityConfigurationImportObject.getObject();
        ThreatEntity entity = (ThreatEntity) getOrCreateEntity(getName(), ThreatEntity.class);

        if (entity == null) {
            return;
        }

        EntityType entityType = entityConfiguration.getEntityType();
        if (entityType == null) {
            entityType = EntityType.createUnknown();
        }

        Affiliation affiliation = entityConfiguration.getAffiliation();

        String militarySymbol = entityConfiguration.getMilitarySymbol();
        if (militarySymbol.isBlank()) {
            militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
        }
        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, affiliation);

        entity.addEntityDetail(new EntityDetail.Builder()
                .setTimestamp(entityConfiguration.getStartTime())
                .setName(getName())
                .setSource("MRWR")
                .setAffiliation(affiliation)
                .setEntityType(entityType)
                .setMilitarySymbol(militarySymbol)
                .setUrn(entityConfiguration.getUrn())
                .build());

        addManualPositions(entity);
        addEntityScopes(entity);

        if (entityConfiguration.getOtherConfigurations() != null &&
                entityConfiguration.getOtherConfigurations().containsKey("range")) {
            String rangeString = entityConfiguration.getOtherConfigurations().get("range");
            try {
                int range = Integer.parseInt(rangeString);
                entity.setRange(range);
                entity.setRangeColor(Color.RED);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ThreatEntityImportFolder::doImport", e);
            }
        }

        if (mediaImportFolder != null) {
            mediaImportFolder.importFolder(entity, importStatusLine);
        }

        infoStringBuilder.append("entity:").append(entity.getName());
        infoStringBuilder.append(", threatDataFileList:").append(threatDataFileList.size());

        for (ThreatDataFile threatDataFile : threatDataFileList) {
            infoStringBuilder.append(", getValuePairList:").append(threatDataFile.getValuePairList().size());
            if (!threatDataFile.getValuePairList().isEmpty()) {
                infoStringBuilder.append(", start:");
                infoStringBuilder.append(Utility.formatTime(threatDataFile.getValuePairList().getFirst().getTimestamp()));
                infoStringBuilder.append(", stop:");
                infoStringBuilder.append(Utility.formatTime(threatDataFile.getValuePairList().getLast().getTimestamp()));
            }
            infoStringBuilder.append(", getAzElDataList:").append(threatDataFile.getValuePairList().size());
            if (!threatDataFile.getValuePairList().isEmpty()) {
                infoStringBuilder.append(", start:");
                infoStringBuilder.append(Utility.formatTime(threatDataFile.getValuePairList().getFirst().getTimestamp()));
                infoStringBuilder.append(", stop:");
                infoStringBuilder.append(Utility.formatTime(threatDataFile.getValuePairList().getLast().getTimestamp()));
            }
            entity.addStatusData(threatDataFile.getValuePairList());
            entity.addAzElData(threatDataFile.getAzElDataList());
        }

        infoStringBuilder.append(", ccmLogFileList:").append(ccmLogFileList.size());
        for (CcmLogFile ccmLogFile : ccmLogFileList) {
            infoStringBuilder.append(", getValuePairList:").append(ccmLogFile.getValuePairList().size());
            if (!ccmLogFile.getValuePairList().isEmpty()) {
                infoStringBuilder.append(", start:");
                infoStringBuilder.append(Utility.formatTime(ccmLogFile.getValuePairList().getFirst().getTimestamp()));
                infoStringBuilder.append(", stop:");
                infoStringBuilder.append(Utility.formatTime(ccmLogFile.getValuePairList().getLast().getTimestamp()));
            }
            entity.addStatusData(ccmLogFile.getValuePairList());
        }

        addToApache(entity);

        Logger.getGlobal().log(Level.INFO, infoStringBuilder.toString());
    }

    private void addToApache(ThreatEntity threatEntity) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof ApacheEntity apacheEntity) {
                apacheEntity.addThreatEntity(threatEntity);
            }
        }
    }
}
