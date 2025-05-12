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
import java.util.ArrayList;
import java.util.List;

public class ThreatImportFolder extends ImportFolder {

    /* Directory Layout
        'threat'
        |   {entity name}
        |  |  'data' //threat data files, tab delimited
        |  |  'video' //media folder set
     */

    public static final String FOLDER_NAME = "threat";

    public static ThreatImportFolder scanAndCreate(File folder) {
        ThreatImportFolder importFolder = new ThreatImportFolder(folder);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private EntityConfigurationFileImportObject entityConfigurationFileImportObject;

    public ThreatImportFolder(File folder) {
        super(folder);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (!file.isDirectory() && file.getName().equalsIgnoreCase(EntityConfigurationFile.NAME)) {
                entityConfigurationFileImportObject = EntityConfigurationFileImportObject.scanAndCreate(file);
                break;
            }
        }

        if (entityConfigurationFileImportObject == null) {
            EntityConfigurationFile entityConfigurationFile = new EntityConfigurationFile();
            EntityConfiguration entityConfiguration = new EntityConfiguration("entity name",
                    "description", EntityType.createUnknown(), Affiliation.UNKNOWN,
                    "military  symbol", 0, 0, "2024-01-01 1200",
                    "2024-01-02 1200", new ArrayList<EntityConfiguration.ManualPosition>(List.of(
                    new EntityConfiguration.ManualPosition("2024-01-01 1200", 0, 0))));
            entityConfiguration.getOtherConfigurations().put("range", "1000");
            entityConfigurationFile.addEntityConfiguration(entityConfiguration);
            entityConfigurationFile.addEntityConfiguration(new EntityConfiguration("entity 2",
                    "description", EntityType.createUnknown(), Affiliation.UNKNOWN,
                    "military  symbol", 0, 0, "2024-01-01 1200",
                    "2024-01-02 1200", new ArrayList<EntityConfiguration.ManualPosition>(List.of(
                    new EntityConfiguration.ManualPosition("2024-01-01 1200", 0, 0)))));

            String filename = getObject().getAbsolutePath() + File.separator + EntityConfigurationFile.NAME;
            ConfigurationManager.safeSave(entityConfigurationFile, filename);

            entityConfigurationFileImportObject = new EntityConfigurationFileImportObject(entityConfigurationFile);
            setMissing(true);
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            EntityConfigurationImportObject entityConfigurationImportObject =
                    entityConfigurationFileImportObject.getEntityConfigurationImportObject(file.getName());
            if (entityConfigurationImportObject == null) {
                scanStatusLine.addError("missing configuration: " + file.getName());
            } else {
                ThreatEntityImportFolder threatEntityImportFolder =
                        new ThreatEntityImportFolder(file, entityConfigurationImportObject);
                threatEntityImportFolder.startScan(scanStatusLine);
                importFolderList.add(threatEntityImportFolder);
            }
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
