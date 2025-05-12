package gov.mil.otc._3dvis.playback.dataset.shadow;

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

public class ShadowImportFolder extends ImportFolder {

    /* Directory Layout
        'shadow'
        |  {entity name} //tail number
        |  |  'stanag' //stanag files
        |  |  'video' //media folder set
     */

    public static final String FOLDER_NAME = "shadow";

    private EntityConfigurationFileImportObject entityConfigurationFileImportObject = null;

    public ShadowImportFolder(File folder) {
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
            }
        }

        if (entityConfigurationFileImportObject == null) {
            EntityConfigurationFile entityConfigurationFile = new EntityConfigurationFile();
            EntityConfiguration entityConfiguration = new EntityConfiguration("4007", "Shadow",
                    EntityType.fromString("1.2.225.50.0.0.0"), Affiliation.FRIENDLY,
                    "", 0, 0, "2020-08-26 0600", "2020-08-29 0600", List.of());
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
            ShadowEntityImportFolder entityImportFolder = new ShadowEntityImportFolder(file, entityConfigurationImportObject);
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
