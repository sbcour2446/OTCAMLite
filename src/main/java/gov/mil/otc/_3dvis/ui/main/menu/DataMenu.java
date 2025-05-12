package gov.mil.otc._3dvis.ui.main.menu;

import gov.mil.otc._3dvis.ui.data.dataimport.*;
import gov.mil.otc._3dvis.ui.data.dataimport.bft.BftImportController;
import gov.mil.otc._3dvis.ui.data.dataimport.datafile.CsvImportController;
import gov.mil.otc._3dvis.ui.data.dataimport.datafile.TirImportController;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaImportController;
import gov.mil.otc._3dvis.playback.PlaybackManagerController;
import gov.mil.otc._3dvis.playback.CreatePlaybackController;
import gov.mil.otc._3dvis.ui.data.dataimport.tapets.TapetsImportController;
import gov.mil.otc._3dvis.ui.data.iteration.IterationManagerController;
import gov.mil.otc._3dvis.ui.data.iteration.IterationUtility;
import gov.mil.otc._3dvis.ui.data.kml.OverlayManagerController;
import gov.mil.otc._3dvis.ui.data.manager.DataManagerController;
import gov.mil.otc._3dvis.ui.data.report.ReportGeneratorController;
import gov.mil.otc._3dvis.ui.projects.blackhawk.BlackHawkImportController;
import gov.mil.otc._3dvis.ui.projects.javelin.JavelinImportController;
import gov.mil.otc._3dvis.ui.projects.nbcrv.dataimport.ManualDataImportController;
import gov.mil.otc._3dvis.ui.projects.nbcrv.dataimport.NbcrvImportController;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class DataMenu extends Menu {

    public DataMenu() {
        setText("Data");
        create();
    }

    private void create() {
        MenuItem playbackMenuItem = new MenuItem("Playback Manager");
        playbackMenuItem.setOnAction(event -> PlaybackManagerController.show());

        MenuItem startIterationMenuItem = new MenuItem("Start New Iteration");
        startIterationMenuItem.setOnAction(event -> IterationUtility.startNewIteration());

        MenuItem stopIterationMenuItem = new MenuItem("Stop Current Iteration");
        stopIterationMenuItem.setOnAction(event -> IterationUtility.stopCurrentIteration());

        MenuItem iterationMenuItem = new MenuItem("Iteration Manager");
        iterationMenuItem.setOnAction(event -> IterationManagerController.show());

        Menu iterationMenu = new Menu("Iteration");
        iterationMenu.getItems().add(startIterationMenuItem);
        iterationMenu.getItems().add(stopIterationMenuItem);
        iterationMenu.getItems().add(iterationMenuItem);

        MenuItem dataManagerMenuItem = new MenuItem("Data Manager");
        dataManagerMenuItem.setOnAction(event -> DataManagerController.show());

        MenuItem overlayManagerMenuItem = new MenuItem("Overlay Manager");
        overlayManagerMenuItem.setOnAction(event -> OverlayManagerController.show());

        MenuItem reportGeneratorMenuItem = new MenuItem("Report Generator");
        reportGeneratorMenuItem.setOnAction(event -> ReportGeneratorController.show());

        getItems().add(playbackMenuItem);
        getItems().add(createDataImportMenu());
        getItems().add(createProjectMenu());
        getItems().add(iterationMenu);
        getItems().add(dataManagerMenuItem);
        getItems().add(overlayManagerMenuItem);
        getItems().add(reportGeneratorMenuItem);
    }

//    private Menu createPlaybackMenu() {
//        Menu menu = new Menu("Playback");
//
//        MenuItem menuItem = new MenuItem("Create Playback Set");
//        menuItem.setOnAction(event -> CreatePlaybackController.show());
//        menu.getItems().add(menuItem);
//
//        menuItem = new MenuItem("Load Playback Set");
//        menuItem.setOnAction(event -> LoadPlaybackController.show());
//        menu.getItems().add(menuItem);
//
//        return menu;
//    }

    private Menu createDataImportMenu() {
        MenuItem importTestMenuItem = new MenuItem("Test");
        importTestMenuItem.setOnAction(event -> CreatePlaybackController.show());

        MenuItem importOtcamMenuItem = new MenuItem("OTCAM");
        importOtcamMenuItem.setOnAction(event -> OtcamImportController.show());

        MenuItem importBftMenuItem = new MenuItem("BFT Data");
        importBftMenuItem.setOnAction(event -> BftImportController.show());

        //black hawk sub menu
        MenuItem blackHawkTspiMenuItem = new MenuItem("TSPI");
        blackHawkTspiMenuItem.setOnAction(event -> BlackHawkTspiImportController.show());

        MenuItem blackHawkThreatMenuItem = new MenuItem("Threat");
        blackHawkThreatMenuItem.setOnAction(event -> BlackHawkThreatImportController.show());

        Menu blackHawkMenu = new Menu("Black Hawk");
        blackHawkMenu.getItems().add(blackHawkTspiMenuItem);
        blackHawkMenu.getItems().add(blackHawkThreatMenuItem);

        MenuItem importShadowMenuItem = new MenuItem("Shadow");
        importShadowMenuItem.setOnAction(event -> ShadowImportController.show());

        MenuItem importTapetsMenuItem = new MenuItem("TAPETS");
        importTapetsMenuItem.setOnAction(event -> TapetsImportController.show());

        MenuItem mediaImportMenuItem = new MenuItem("Media");
        mediaImportMenuItem.setOnAction(event -> MediaImportController.show());

        Menu menu = new Menu("Import");
        menu.getItems().add(importTestMenuItem);
        menu.getItems().add(importOtcamMenuItem);
        menu.getItems().add(createDataFileImportMenu());
        menu.getItems().add(importBftMenuItem);
        menu.getItems().add(blackHawkMenu);
        menu.getItems().add(importShadowMenuItem);
        menu.getItems().add(importTapetsMenuItem);
        menu.getItems().add(mediaImportMenuItem);

        return menu;
    }

    private Menu createDataFileImportMenu() {
        MenuItem tspiFileMenuItem = new MenuItem("TSPI File");
        tspiFileMenuItem.setOnAction(event -> CsvImportController.show());

        MenuItem tirFileMenuItem = new MenuItem("TIR File");
        tirFileMenuItem.setOnAction(event -> TirImportController.show());

        Menu menu = new Menu("Data File");
        menu.getItems().add(tspiFileMenuItem);
        menu.getItems().add(tirFileMenuItem);

        return menu;
    }

    private Menu createProjectMenu() {
        MenuItem importBlackHawkMenuItem = new MenuItem("Black Hawk");
        importBlackHawkMenuItem.setOnAction(event -> BlackHawkImportController.show());

        MenuItem importNbcrvMenuItem = new MenuItem("Import Mission");
        importNbcrvMenuItem.setOnAction(event -> NbcrvImportController.show());
        MenuItem manualDataImportMenuItem = new MenuItem("Manual Data");
        manualDataImportMenuItem.setOnAction(event -> ManualDataImportController.show());
        Menu nbcrvMenu = new Menu("NBCRV");
        nbcrvMenu.getItems().add(importNbcrvMenuItem);
        nbcrvMenu.getItems().add(manualDataImportMenuItem);

        MenuItem importJavelinMenuItem = new MenuItem("Javelin");
        importJavelinMenuItem.setOnAction(event -> JavelinImportController.show());

        Menu projectMenu = new Menu("Projects");
        projectMenu.getItems().add(importBlackHawkMenuItem);
        projectMenu.getItems().add(nbcrvMenu);
        projectMenu.getItems().add(importJavelinMenuItem);

        return projectMenu;
    }
}
