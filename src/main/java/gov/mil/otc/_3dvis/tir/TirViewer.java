package gov.mil.otc._3dvis.tir;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TirViewer {

    private static TirViewer singleton = null;
    private final Stage stage = new Stage();
    private final TabPane tabPane = new TabPane();
    private double imageWidth = 0;
    private double imageHeight = 0;

    private TirViewer() {
        BorderPane borderPane = new BorderPane(tabPane);
        borderPane.setMinWidth(500);
        borderPane.setMinHeight(500);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle("TIR Viewer");
        stage.initOwner(MainApplication.getInstance().getStage());

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
    }

    public static void show(File file) {
        if (singleton == null) {
            singleton = new TirViewer();
        }
        singleton.doShow(file);
    }

    private void doShow(File file) {
        singleton.stage.show();
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getId().equals(file.getAbsolutePath())) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }
        Tab tab = createNewTab(file);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private Tab createNewTab(File file) {
        ImageView imageView = new ImageView();
        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage image = renderer.renderImage(0);
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "TirViewer::createNewTab", e);
        }
        imageView.setPreserveRatio(true);
        imageView.setOnScroll(event -> {
            if (event.isControlDown()) {
                double zoom = event.getDeltaY();
                imageWidth += zoom;
                imageHeight += zoom;
                imageView.setFitWidth(imageWidth);
                imageView.setFitHeight(imageHeight);
                event.consume();
            }
        });

        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setPannable(true);

        Tab tab = new Tab(file.getName());
        tab.setId(file.getAbsolutePath());
        tab.setContent(scrollPane);
        return tab;
    }
}
