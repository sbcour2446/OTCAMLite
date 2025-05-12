package gov.mil.otc._3dvis.playback.dataset.bft;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UrnFilterImportObject extends ImportObject<File> {

    public static String NAME = "urnFilter.txt";

    public static UrnFilterImportObject scanAndCreate(File file) {
        UrnFilterImportObject importObject = new UrnFilterImportObject(file);
        if (!importObject.processFile()) {
            importObject.setMissing(true);
        }
        importObject.determineIsNew();
        return importObject;
    }

    private final List<Integer> urnList = new ArrayList<>();

    public UrnFilterImportObject(File object) {
        super(object, "urn filter");
    }

    private boolean processFile() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(getObject().getAbsolutePath()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                int urn = Integer.parseInt(line);
                urnList.add(urn);
            }
        } catch (Exception e) {
            String message = String.format("UrnFilterImportObject::processFile:Error processing file %s",
                    getObject().getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return true;
    }

    public List<Integer> getUrnFilterList() {
        return urnList;
    }

    @Override
    public VBox getDisplayPane() {
        ListView<Integer> listView = new ListView<>();
        for (int urn : urnList) {
            listView.getItems().add(urn);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        // not implemented
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }
}
