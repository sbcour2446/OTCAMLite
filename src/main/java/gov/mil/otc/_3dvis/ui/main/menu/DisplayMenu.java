package gov.mil.otc._3dvis.ui.main.menu;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.tir.TirWidgetPane;
import gov.mil.otc._3dvis.ui.display.entitydisplay.EntityDisplayController;
import gov.mil.otc._3dvis.ui.display.MissionWidgetPane;
import gov.mil.otc._3dvis.ui.display.ShowLabelsController;
import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.layers.Earth.UTMGraticuleLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class DisplayMenu extends Menu {

    public DisplayMenu() {
        setText("Display");
        create();
    }

    private void create() {
        MenuItem showMissionMenuItem = new MenuItem("Show Mission List");
        showMissionMenuItem.setOnAction(event -> MissionWidgetPane.show());

        MenuItem showTirMenuItem = new MenuItem("Show TIR List");
        showTirMenuItem.setOnAction(event -> TirWidgetPane.show());

        CheckMenuItem showMgrsGraticuleCheckMenuItem = new CheckMenuItem("Show MGRS Graticule");
        showMgrsGraticuleCheckMenuItem.setOnAction(event ->
                showHideMgrsGraticuleLayer(showMgrsGraticuleCheckMenuItem.isSelected()));

        CheckMenuItem showLatLonGraticuleCheckMenuItem = new CheckMenuItem("Show Lat/Lon Graticule");
        showLatLonGraticuleCheckMenuItem.setOnAction(event ->
                showHideLatLonGraticuleLayer(showLatLonGraticuleCheckMenuItem.isSelected()));

        CheckMenuItem showUtmGraticuleCheckMenuItem = new CheckMenuItem("Show UTM Graticule");
        showUtmGraticuleCheckMenuItem.setOnAction(event ->
                showHideUtmGraticuleLayer(showUtmGraticuleCheckMenuItem.isSelected()));

        MenuItem showLabelsMenuItem = new MenuItem("Show Labels");
        showLabelsMenuItem.setOnAction(event -> ShowLabelsController.show());

        MenuItem entityDisplayMenuItem = new MenuItem("Entity Display");
        entityDisplayMenuItem.setOnAction(event -> EntityDisplayController.show());

        getItems().add(showMissionMenuItem);
        getItems().add(showTirMenuItem);
        getItems().add(createFilterMenu());
        getItems().add(showMgrsGraticuleCheckMenuItem);
        getItems().add(showLatLonGraticuleCheckMenuItem);
        getItems().add(showUtmGraticuleCheckMenuItem);
//        getItems().add(showLabelsMenuItem);
        getItems().add(entityDisplayMenuItem);
    }

    private Menu createFilterMenu() {
        EntityFilter entityFilter = SettingsManager.getSettings().getEntityFilter();

        CheckMenuItem outOfCommsCheckMenuItem = new CheckMenuItem("Show Out-of-Comms");
        outOfCommsCheckMenuItem.setSelected(entityFilter.isFilterOutOfComms());
        outOfCommsCheckMenuItem.setOnAction(event -> {
            EntityManager.getEntityFilter().setFilterOutOfComms(outOfCommsCheckMenuItem.isSelected());
            SettingsManager.getSettings().getEntityFilter().setFilterOutOfComms(outOfCommsCheckMenuItem.isSelected());
        });

        CheckMenuItem outOfScopeCheckMenuItem = new CheckMenuItem("Show Out-of-Scope");
        outOfScopeCheckMenuItem.setSelected(entityFilter.isFilterOutOfScope());
        outOfScopeCheckMenuItem.setOnAction(event -> {
            EntityManager.getEntityFilter().setFilterOutOfScope(outOfScopeCheckMenuItem.isSelected());
            SettingsManager.getSettings().getEntityFilter().setFilterOutOfScope(outOfScopeCheckMenuItem.isSelected());
        });

        CheckMenuItem timedOutCheckMenuItem = new CheckMenuItem("Show Timed-Out");
        timedOutCheckMenuItem.setSelected(entityFilter.isFilterOutOfScope());
        timedOutCheckMenuItem.setOnAction(event -> {
            EntityManager.getEntityFilter().setFilterTimedOut(timedOutCheckMenuItem.isSelected());
            SettingsManager.getSettings().getEntityFilter().setFilterTimedOut(timedOutCheckMenuItem.isSelected());
        });

        Menu affiliationMenu = new Menu("Affiliation");
        for (Affiliation affiliation : Affiliation.values()) {
            CheckMenuItem menuItem = new CheckMenuItem(affiliation.getName());
            menuItem.setSelected(entityFilter.getAffiliationFilter(affiliation));
            menuItem.setOnAction(event -> {
                EntityManager.getEntityFilter().setAffiliationFilter(affiliation, menuItem.isSelected());
                SettingsManager.getSettings().getEntityFilter().setAffiliationFilter(affiliation, menuItem.isSelected());
            });
            affiliationMenu.getItems().add(menuItem);
        }

        Menu menu = new Menu("Filter");
        menu.getItems().add(outOfCommsCheckMenuItem);
        menu.getItems().add(outOfScopeCheckMenuItem);
        menu.getItems().add(timedOutCheckMenuItem);
        menu.getItems().add(affiliationMenu);

        return menu;
    }

    private void showHideMgrsGraticuleLayer(boolean show) {
        LayerList layerList = WWController.getWorldWindowPanel().getModel().getLayers();
        for (Layer layer : layerList) {
            if (layer instanceof MGRSGraticuleLayer) {
                layer.setEnabled(show);
            }
        }
    }

    private void showHideLatLonGraticuleLayer(boolean show) {
        LayerList layerList = WWController.getWorldWindowPanel().getModel().getLayers();
        for (Layer layer : layerList) {
            if (layer instanceof LatLonGraticuleLayer) {
                layer.setEnabled(show);
            }
        }
    }

    private void showHideUtmGraticuleLayer(boolean show) {
        LayerList layerList = WWController.getWorldWindowPanel().getModel().getLayers();
        for (Layer layer : layerList) {
            if (layer instanceof UTMGraticuleLayer) {
                layer.setEnabled(show);
            }
        }
    }

    private void showHideTirList(boolean show) {

    }
}
