package gov.mil.otc._3dvis.ui.main.menu;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.tools.GoToLocationController;
import gov.mil.otc._3dvis.ui.tools.TrackingSettingsController;
import gov.mil.otc._3dvis.ui.tools.entitytable.EntityTableController;
import gov.mil.otc._3dvis.ui.tools.eventtable.EventTableController;
import gov.mil.otc._3dvis.ui.tools.ils.InstrumentLandingSystemController;
import gov.mil.otc._3dvis.ui.tools.iterationtable.IterationTableController;
import gov.mil.otc._3dvis.ui.tools.media.RemoveAudioUtility;
import gov.mil.otc._3dvis.ui.tools.missiontable.MissionTableController;
import gov.mil.otc._3dvis.ui.tools.rangefinder.RangeFinderController2;
import gov.mil.otc._3dvis.ui.widgets.status.Result;
import gov.mil.otc._3dvis.ui.widgets.status.StatusDialog;
import gov.mil.otc._3dvis.ui.widgets.status.StatusLine;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class ToolsMenu extends Menu {

    public ToolsMenu() {
        setText("Tools");
        create();
    }

    private void create() {
        MenuItem entityTableMenuItem = new MenuItem("Entity Table");
        entityTableMenuItem.setOnAction(event -> EntityTableController.show());

        MenuItem eventTableMenuItem = new MenuItem("Event Table");
        eventTableMenuItem.setOnAction(event -> EventTableController.show());

        MenuItem missionTableMenuItem = new MenuItem("Mission Table");
        missionTableMenuItem.setOnAction(event -> MissionTableController.show());

        MenuItem iterationTableMenuItem = new MenuItem("Iteration Table");
        iterationTableMenuItem.setOnAction(event -> IterationTableController.show());

        MenuItem rangeFinder2MenuItem = new MenuItem("Range Finder");
        rangeFinder2MenuItem.setOnAction(event -> RangeFinderController2.show());

        MenuItem gotoMenuItem = new MenuItem("GoTo");
        gotoMenuItem.setOnAction(event -> GoToLocationController.show());

        MenuItem entityTrackingMenuItem = new MenuItem("Entity Tracking");
        entityTrackingMenuItem.setOnAction(event -> TrackingSettingsController.show());

        MenuItem ilsMenuItem = new MenuItem("ILS Manager");
        ilsMenuItem.setOnAction(event -> InstrumentLandingSystemController.show());

        MenuItem removeAudioMenuItem = new MenuItem("Remove Audio Utility");
        removeAudioMenuItem.setOnAction(event -> RemoveAudioUtility.show());
        Menu mediaMenu = new Menu("Media");
        mediaMenu.getItems().add(removeAudioMenuItem);

        MenuItem testMenuItem = new MenuItem("test");
        testMenuItem.setOnAction(event -> {
            final StatusDialog statusDialog = new StatusDialog(MainApplication.getInstance().getStage());
            statusDialog.createAndShow();
            new Thread(() -> {
                for (int i = 0; i < 2; i++) {
                    StatusLine statusLine1 = statusDialog.createStatusLine("status1 " + i);
                    StatusLine statusLine2 = statusDialog.createStatusLine("status2 " + i);
                    for (int j = 0; j < 5; j++) {
                        StatusLine childItem = statusLine1.createChildStatus("child " + i);
                        childItem.createChildStatus("child child item");
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                        if (j % 2 == 0) {
                            childItem.setResult(new Result("complete", true));
                        } else {
                            childItem.setResult(new Result("failed", false));
                        }
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                    for (int j = 0; j < 5; j++) {
                        StatusLine childItem = statusLine2.createChildStatus("child " + i);
                        childItem.createChildStatus("child child item");
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                        if (j % 2 == 0) {
                            childItem.setResult(new Result("complete", true));
                        } else {
                            childItem.setResult(new Result("failed", false));
                        }
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                    }
                    statusLine1.setResult(new Result("complete", i % 2 == 0));
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
            }).start();
//            MapAndTerrainDownloadController.show();

//            DirectoryChooser directoryChooser = new DirectoryChooser();
//            directoryChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("test"));
//            File directory = directoryChooser.showDialog(MainApplication.getInstance().getStage());
//            FileChooser fileChooser = new FileChooser();
//            fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("test"));
//            File file = fileChooser.showOpenDialog(MainApplication.getInstance().getStage());
//            if (file != null) {
//                SettingsManager.getPreferences().setLastDirectory("test", file.getParent());
//
//                LidarKmlFile lidarKmlFile = new LidarKmlFile(file);
//                lidarKmlFile.process();
//
//                System.out.println(lidarKmlFile.getName());
//                System.out.println(lidarKmlFile.getLidarPosition());
//
//            }

//            FileChooser fileChooser = new FileChooser();
//            fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("test"));
//            File file = fileChooser.showOpenDialog(MainApplication.getInstance().getStage());
//            if (file != null) {
//                try {
//                    ULogReader uLogReader = new ULogReader(file.getAbsolutePath());
//                    Object o = uLogReader.readMessage();
//                    while (o!=null) {
//                        System.out.println(o);
//                        o = uLogReader.readMessage();
//                    }
//                    for (MessageLog messageLog : uLogReader.loggedMessages) {
//                        System.out.println(messageLog.timestamp + ": " + messageLog.message);
//                    }
//                } catch (Exception e) {
//                    System.out.println(e);
//                }
//
//
//////                NbcrvDataImport.importNbcrvCsvFile(file);
////
////                RocketSledXmlReader rocketSledXmlReader = new RocketSledXmlReader(file);
////                rocketSledXmlReader.read();
//////                SensorXml sensorXml = new SensorXml(file);
//////                Sensor sensor = sensorXml.read();
//////                WeatherStation weatherStation = WeatherStation.createEntity(sensor);
//////                weatherStation.processSensorReadings();
//////                EntityManager.addEntity(weatherStation);
//////                System.out.print("done");
////                System.out.println(Utility.formatTime(TirReader.getTime(file)));
//            }
        });

        getItems().add(entityTableMenuItem);
        getItems().add(eventTableMenuItem);
        getItems().add(missionTableMenuItem);
        getItems().add(iterationTableMenuItem);
        getItems().add(rangeFinder2MenuItem);
        getItems().add(gotoMenuItem);
        getItems().add(entityTrackingMenuItem);
        getItems().add(ilsMenuItem);
        getItems().add(mediaMenu);
        getItems().add(testMenuItem);
    }
}
