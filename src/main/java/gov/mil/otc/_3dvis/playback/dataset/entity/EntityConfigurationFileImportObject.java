package gov.mil.otc._3dvis.playback.dataset.entity;

import gov.mil.otc._3dvis.entity.base.EntityConfiguration;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.ConfigurationManager;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EntityConfigurationFileImportObject extends ImportObject<EntityConfigurationFile> {

    public static final String NAME = EntityConfigurationFile.NAME;

    public static EntityConfigurationFileImportObject scanAndCreate(File file) {
        EntityConfigurationFile entityConfigurationFile = ConfigurationManager.load(file, EntityConfigurationFile.class);
        if (entityConfigurationFile != null) {
            return new EntityConfigurationFileImportObject(entityConfigurationFile);
        }
        return null;
    }

    private final List<EntityConfigurationImportObject> entityConfigurationImportObjectList = new ArrayList<>();

    public EntityConfigurationFileImportObject(EntityConfigurationFile object) {
        super(object, "Entity Configuration");

        for (EntityConfiguration entityConfiguration : object.getEntityConfigurationList()) {
            entityConfigurationImportObjectList.add(new EntityConfigurationImportObject(entityConfiguration));
        }
    }

    public EntityConfigurationImportObject getEntityConfigurationImportObject(String name) {
        for (EntityConfigurationImportObject entityConfigurationImportObject : entityConfigurationImportObjectList) {
            if (entityConfigurationImportObject.getObject().getName().equalsIgnoreCase(name)) {
                return entityConfigurationImportObject;
            }
        }
        return null;
    }

    @Override
    public VBox getDisplayPane() {
        ListView<String> listView = new ListView<>();
        for (EntityConfigurationImportObject importObject : entityConfigurationImportObjectList) {
            listView.getItems().add(importObject.getName());
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        //not implemented
    }

    @Override
    public void doImport(IEntity entity) {
        //not implemented
    }
}
