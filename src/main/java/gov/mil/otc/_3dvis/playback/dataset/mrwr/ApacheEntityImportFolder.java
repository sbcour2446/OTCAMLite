package gov.mil.otc._3dvis.playback.dataset.mrwr;

import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.base.EntityConfiguration;
import gov.mil.otc._3dvis.playback.dataset.csv.CsvTspiImportFolder;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityConfigurationImportObject;
import gov.mil.otc._3dvis.playback.dataset.entity.EntityImportFolder;
import gov.mil.otc._3dvis.playback.dataset.media.MediaImportFolder;
import gov.mil.otc._3dvis.project.mrwr.ApacheEntity;
import gov.mil.otc._3dvis.project.mrwr.WeaponSystemResponseFile;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApacheEntityImportFolder extends EntityImportFolder {

    /* Directory Layout
        {entity name}
        |  'tspi_data_csv' //1553 data files, csv
        |  'video' //media folder set
     */

    private ApacheTspiImportFolder apacheTspiImportFolder = null;
    private final List<WeaponSystemResponseFile> weaponSystemResponseFileList = new ArrayList<>();

    public ApacheEntityImportFolder(File folder, EntityConfigurationImportObject entityConfigurationImportObject) {
        super(folder, entityConfigurationImportObject);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(CsvTspiImportFolder.FOLDER_NAME)) {
                apacheTspiImportFolder = new ApacheTspiImportFolder(file);
                apacheTspiImportFolder.validateAndScan(this);
            } else if (MediaImportFolder.isMediaFolder(file)) {
                mediaImportFolder = new MediaImportFolder(file, MediaTimestampFormat.DAY_OF_YEAR_MILLI);
                mediaImportFolder.validateAndScan(this);
            } else if (file.getName().equalsIgnoreCase("data")) {
                getDataFiles(file);
            }
        }

        if (entityConfigurationImportObject == null || apacheTspiImportFolder == null) {
            setMissing(true);
        }

        return true;
    }

    private void getDataFiles(File folder) {
        if (!folder.isDirectory()) {
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            WeaponSystemResponseFile weaponSystemResponseFile = new WeaponSystemResponseFile(file);
            if (weaponSystemResponseFile.processFile()) {
                weaponSystemResponseFileList.add(weaponSystemResponseFile);
            }
        }
    }

    @Override
    public void doImport() {
        EntityConfiguration entityConfiguration = entityConfigurationImportObject.getObject();
        ApacheEntity entity = (ApacheEntity) getOrCreateEntity(getName(), ApacheEntity.class);

        if (entity == null) {
            return;
        }

        apacheTspiImportFolder.importFolder(entity, importStatusLine);
        long startTime = apacheTspiImportFolder.getFirstTimestamp();
        long stopTime = apacheTspiImportFolder.getLastTimestamp();

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
                .setTimestamp(startTime)
                .setName(getName())
                .setSource("MRWR")
                .setAffiliation(affiliation)
                .setEntityType(entityType)
                .setMilitarySymbol(militarySymbol)
                .setUrn(entityConfiguration.getUrn())
                .build());

        entity.addEntityScope(new EntityScope(startTime, stopTime));

        for (WeaponSystemResponseFile weaponSystemResponseFile : weaponSystemResponseFileList) {
            entity.addWeaponSystemResponses(weaponSystemResponseFile.getWeaponSystemResponseList());
        }

        if (mediaImportFolder != null) {
            mediaImportFolder.importFolder(entity, importStatusLine);
        }

        setImported(true);
    }
}
