package gov.mil.otc._3dvis.playback.dataset.shadow;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityConfiguration;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.csv.CsvTspiImportFolder;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityConfigurationImportObject;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityImportFolder;
import gov.mil.otc._3dvis.playback.dataset.media.MediaImportFolder;
import gov.mil.otc._3dvis.playback.dataset.mrwr.ApacheTspiImportFolder;
import gov.mil.otc._3dvis.playback.dataset.stanag.StanagImportFolder;
import gov.mil.otc._3dvis.project.mrwr.ApacheEntity;
import gov.mil.otc._3dvis.project.mrwr.WeaponSystemResponseFile;
import gov.mil.otc._3dvis.project.shadow.ShadowEntity;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShadowEntityImportFolder extends EntityImportFolder {

    /* Directory Layout
        {entity name}
        |  'stanag' //stanag files
        |  'video' //media folder set
     */

    private StanagImportFolder stanagImportFolder = null;

    public ShadowEntityImportFolder(File folder, EntityConfigurationImportObject entityConfigurationImportObject) {
        super(folder, entityConfigurationImportObject);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(StanagImportFolder.FOLDER_NAME)) {
                stanagImportFolder = new StanagImportFolder(file);
                stanagImportFolder.validateAndScan(this);
            } else if (MediaImportFolder.isMediaFolder(file)) {
                mediaImportFolder = new MediaImportFolder(file, MediaTimestampFormat.DAY_OF_YEAR);
                mediaImportFolder.validateAndScan(this);
            }
        }

        if (entityConfigurationImportObject == null || stanagImportFolder == null) {
            setMissing(true);
        }

        return true;
    }

    @Override
    public void doImport() {
        EntityConfiguration entityConfiguration = entityConfigurationImportObject.getObject();
        ShadowEntity shadowEntity = (ShadowEntity) getOrCreateEntity(getName(), ShadowEntity.class);

        if (shadowEntity == null) {
            return;
        }

        stanagImportFolder.importFolder(shadowEntity, importStatusLine);
//        long startTime = stanagImportFolder.getFirstTimestamp();
//        long stopTime = stanagImportFolder.getLastTimestamp();

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

        shadowEntity.addEntityDetail(new EntityDetail.Builder()
                .setTimestamp(entityConfiguration.getStartTime())
                .setName(getName())
                .setSource("Shadow")
                .setAffiliation(affiliation)
                .setEntityType(entityType)
                .setMilitarySymbol(militarySymbol)
                .setUrn(entityConfiguration.getUrn())
                .build());

        addEntityScopes(shadowEntity);

        stanagImportFolder.doImport(shadowEntity);

        if (mediaImportFolder != null) {
            mediaImportFolder.importFolder(shadowEntity, importStatusLine);
        }

        setImported(true);
    }
}
