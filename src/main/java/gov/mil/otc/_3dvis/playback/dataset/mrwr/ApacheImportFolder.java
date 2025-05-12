package gov.mil.otc._3dvis.playback.dataset.mrwr;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.base.EntityConfiguration;
import gov.mil.otc._3dvis.playback.ConfigurationManager;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityConfigurationFile;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityConfigurationFileImportObject;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityConfigurationImportObject;

import java.io.File;
import java.util.List;

public class ApacheImportFolder extends ImportFolder {

    /* Directory Layout
        'apache'
        |  {entity name}
        |  |  'tspi_data_csv' //1553 data files, csv
        |  |  'video' //media folder set
     */

    public static final String FOLDER_NAME = "apache";

    public static ApacheImportFolder scanAndCreate(File folder) {
        ApacheImportFolder importFolder = new ApacheImportFolder(folder);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private EntityConfigurationFileImportObject entityConfigurationFileImportObject = null;

    public ApacheImportFolder(File folder) {
        super(folder);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getName().equalsIgnoreCase(EntityConfigurationFile.NAME)) {
                entityConfigurationFileImportObject = EntityConfigurationFileImportObject.scanAndCreate(file);
            } else if (file.getName().equalsIgnoreCase(ThreatNameMapFile.NAME)) {
                ThreatNameMapFile threatNameMapFile = new ThreatNameMapFile(file);
                threatNameMapFile.processFile();
            }
        }

        if (entityConfigurationFileImportObject == null) {
            EntityConfigurationFile entityConfigurationFile = new EntityConfigurationFile();
            EntityConfiguration entityConfiguration = new EntityConfiguration("entity_name_here", "SHADOW",
                    EntityType.fromString("1.2.225.50.0.0.0"), Affiliation.FRIENDLY,
                    "", 0, 0, "2025-01-01 0600", "2025-01-02 0600",
                    List.of(new EntityConfiguration.ManualPosition("2025-01-01 0600", 0.0, 0.0)));
            entityConfigurationFile.addEntityConfiguration(entityConfiguration);
            String filename = getObject().getAbsolutePath() + File.separator + EntityConfigurationFile.NAME;
            ConfigurationManager.safeSave(entityConfigurationFile, filename);

            entityConfigurationFileImportObject = new EntityConfigurationFileImportObject(entityConfigurationFile);
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            EntityConfigurationImportObject entityConfigurationImportObject =
                    entityConfigurationFileImportObject.getEntityConfigurationImportObject(file.getName());
            ApacheEntityImportFolder entityImportFolder = new ApacheEntityImportFolder(file, entityConfigurationImportObject);
            entityImportFolder.startScan(scanStatusLine);
            importFolderList.add(entityImportFolder);
        }

        if (importFolderList.isEmpty()) {
            scanStatusLine.addError("missing entity folder");
            setMissing(true);
        }

        for (ImportFolder importFolder : importFolderList) {
            importFolder.waitForScanComplete();
        }

        return true;
    }
}
