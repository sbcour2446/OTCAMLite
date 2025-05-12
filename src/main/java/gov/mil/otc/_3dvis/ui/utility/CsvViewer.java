package gov.mil.otc._3dvis.ui.utility;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.widgets.tableview.CsvTableView;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;

public class CsvViewer {

    private final Stage stage = new Stage();

    public static synchronized void show(File file) {
        CsvViewer csvViewer = new CsvViewer(file);
        csvViewer.doShow();
    }

    private CsvViewer(File file) {
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle(file.getName());
        stage.initOwner(MainApplication.getInstance().getStage());
        Scene scene = new Scene(new BorderPane(CsvTableView.create(file)));
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Entity Table");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
    }

    private void doShow() {
        stage.show();
    }
}
