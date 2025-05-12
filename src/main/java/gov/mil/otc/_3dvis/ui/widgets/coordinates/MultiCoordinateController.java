package gov.mil.otc._3dvis.ui.widgets.coordinates;

import gov.nasa.worldwind.geom.Position;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

public class MultiCoordinateController extends Pane implements ChangeListener<Tab> {

    private final Tab ddTab = new Tab("Decimal Degrees");
    private final Tab mgrsTab = new Tab("MGRS");
    private final Tab utmTab = new Tab("UTM");
    private final TabPane tabPane = new TabPane();

    public MultiCoordinateController() {
        this(null);
    }

    public MultiCoordinateController(Position position) {
        initialize(position);
    }

    public Position getPosition() {
        return ((ICoordinatesController) tabPane.getSelectionModel().getSelectedItem().getContent()).getPosition();
    }

    private void initialize(Position position) {
        ddTab.setContent(new DecimalDegreesCoordinatesController(position));
        mgrsTab.setContent(new MgrsCoordinatesController(position));
        utmTab.setContent(new UtmCoordinatesController(position));

        tabPane.getTabs().add(ddTab);
        tabPane.getTabs().add(mgrsTab);
        tabPane.getTabs().add(utmTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener(this);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        getChildren().add(tabPane);
    }

    @Override
    public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        if (!oldValue.equals(newValue)) {
            ICoordinatesController oldCoordinatesController = (ICoordinatesController) oldValue.getContent();
            ICoordinatesController newCoordinatesController = (ICoordinatesController) newValue.getContent();
            newCoordinatesController.setPosition(oldCoordinatesController.getPosition());
        }
    }
}
