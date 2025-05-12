package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NbcrvStatusView {

    public static void show(IEntity entity) {
        NbcrvStatusView nbcrvStatusView = nbcrvStatusViewMap.get(entity.getEntityId());
        if (nbcrvStatusView == null) {
            nbcrvStatusView = new NbcrvStatusView(entity);
            nbcrvStatusViewMap.put(entity.getEntityId(), nbcrvStatusView);
        }
        nbcrvStatusView.stage.requestFocus();
    }

    private static final Map<EntityId, NbcrvStatusView> nbcrvStatusViewMap = new ConcurrentHashMap<>();
    private final Stage stage = new Stage();
    private final TextField readingTimeTextField = new TextField();
    private final TextField operationalStatusTextField = new TextField();
    private final IEntity entity;

    private NbcrvStatusView(IEntity entity) {
        this.entity = entity;
        initialize();
    }

    private void initialize() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        readingTimeTextField.setMaxWidth(Double.MAX_VALUE);

        gridPane.add(new TextWithStyleClass("Reading Time:"), 0, rowIndex);
        gridPane.add(readingTimeTextField, 1, rowIndex);

        rowIndex++;

        operationalStatusTextField.setMaxWidth(Double.MAX_VALUE);

        gridPane.add(new TextWithStyleClass("Operational Status:"), 0, rowIndex);
        gridPane.add(operationalStatusTextField, 1, rowIndex);

        VBox mainVBox = new VBox(gridPane);
        mainVBox.setPrefWidth(800);

        Scene scene = new Scene(mainVBox);
        ThemeHelper.applyTheme(scene);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.setTitle("NBCRV Status View : " + entity.getEntityId());

        StageSizer stageSizer = new StageSizer("NBCRV Status View");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
    }
}
