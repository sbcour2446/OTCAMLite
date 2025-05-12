package gov.mil.otc._3dvis.ui.main;

import gov.mil.otc._3dvis.ui.utility.StageUtility;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AboutController {

    private final Stage stage = new Stage();

    public static synchronized void show() {
        new AboutController().doShow();
    }

    private AboutController() {
        String text = "3DVis is property of the United States Government, developed by the US Army's Operational Test Command (OTC)." +
                System.lineSeparator() +
                System.lineSeparator() +
                "DISTRIBUTION STATEMENT D" +
                System.lineSeparator() +
                " Distribution authorized to the Department of Defense and U.S. DoD contractors only. Other requests shall be referred to:" +
                System.lineSeparator() +
                "     Test Technology Directorate (TTD)" +
                System.lineSeparator() +
                "     U.S. Army Operational Test Command" +
                System.lineSeparator() +
                "     91012 Station Avenue, ATTN:  TEOT-TT" +
                System.lineSeparator() +
                "     Fort Hood, Texas  76544-5068." +
                System.lineSeparator() +
                System.lineSeparator() +
                "REASONING:" +
                System.lineSeparator() +
                System.lineSeparator() +
                "ADMINISTRATIVE OR OPERATIONAL USE: To protect technical or operational data or information from automatic dissemination under the International Exchange Program or by other means. This protection covers publications required solely for official use or strictly for administrative or operational purposes. This statement may apply to manuals, pamphlets, technical orders, technical reports, and other publications containing valuable technical or operational data." +
                System.lineSeparator() +
                System.lineSeparator() +
                "CRITICAL TECHNOLOGY: To protect information and technical data that advance current technology or describe new technology in an area of significant or potentially significant military application or that relate to a specific military deficiency of a potential adversary. Information of this type may be classified or unclassified." +
                System.lineSeparator() +
                System.lineSeparator() +
                "EXPORT CONTROLLED: To protect information subject to the provisions of Reference (d)." +
                System.lineSeparator() +
                System.lineSeparator() +
                "SOFTWARE DOCUMENTATION: To protect technical data relating to computer software that is releasable only in accordance with the software license in subpart 227.72 of Reference (so). It includes documentation such as user or owner manuals, installation instructions, operating instructions, and other information that explains the capabilities of or provides instructions for using or maintaining computer software." +
                System.lineSeparator() +
                System.lineSeparator() +
                "SPECIFIC AUTHORITY: To protect information not specifically included in the above reasons, but which requires protection in accordance with valid documented authority (e.g., Executive orders, statutes such as Atomic Energy Federal regulation). When filling in the reason, cite \"Specific Authority (identification of valid documented authority).\"" +
                System.lineSeparator() +
                System.lineSeparator() +
                "VULNERABILITY INFORMATION: To protect information and technical data that provides insight into vulnerabilities of U.S. critical infrastructure, including DoD warfighting infrastructure, vital to National Security that are otherwise not publicly available." +
                System.lineSeparator();


        Label label = new Label("3DVis");
        label.setFont(Font.font(31));

        Label versionLabel = new Label();
        versionLabel.setText("Version: " + gov.mil.otc._3dvis.generated.BuildInformation.VERSION);

        ImageView imageView = new ImageView();
        imageView.setImage(ImageLoader.getLogo());
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);

        TextArea textArea = new TextArea(text);
        textArea.setWrapText(true);
        textArea.setEditable(false);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(label);
        vBox.getChildren().add(versionLabel);

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(vBox);
        borderPane.setRight(imageView);

        BorderPane mainBorderPane = new BorderPane();
        mainBorderPane.setPadding(new Insets(10, 10, 10, 10));
        mainBorderPane.setTop(borderPane);
        mainBorderPane.setCenter(textArea);

        BorderPane.setAlignment(borderPane, Pos.CENTER);
        BorderPane.setAlignment(vBox, Pos.CENTER);
        BorderPane.setAlignment(imageView, Pos.CENTER);
        BorderPane.setAlignment(textArea, Pos.CENTER);

        Scene scene = new Scene(mainBorderPane);
        ThemeHelper.applyTheme(scene);
        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.setResizable(true);
        stage.setTitle("About 3DVis");
        stage.setScene(scene);
        StageUtility.centerStage(stage, MainApplication.getInstance().getStage());
    }

    private void doShow() {
        stage.show();
    }
}