package gov.mil.otc._3dvis.ui.widgets;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A class that mimics the functionality of a combo box, and populates the popup with station names and IP addresses
 * in a tree-like view.
 */
public class AvailableIpComboBox extends GridPane {
    /**
     * The action button.
     */
    @FXML
    private MenuButton actionButton;

    /**
     * The content field for user input.
     */
    @FXML
    private TextField contentTextField;

    /**
     * The tree view contained in the menu of the menu button.
     */
    private TreeView<String> stationIpTreeView;

    /**
     * The root item of the tree view.
     */
    private TreeItem<String> rootItem;

    private final List<String> categories;

    /**
     * {@inheritDoc}
     */
    public AvailableIpComboBox() throws IOException {
        super();
        var loader = new FXMLLoader(getClass().getResource("/widgets/availableIpComboBox.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        categories = new ArrayList<>();

        initializeTreeView();
        initializeCustomMenuItem();
    }

    /**
     * Sets the value of the combo box.
     *
     * @param text The value to display in the text field.
     */
    public void setValue(String text) {
        contentTextField.setText(text == null ? "" : text);
    }

    /**
     * Gets the value of the combo box.
     *
     * @return The value of the combo box.
     */
    public String getValue() {
        return contentTextField.getText();
    }

    /**
     * Initializes the tree view and populates its content.
     */
    private void initializeTreeView() {
        rootItem = new TreeItem<>();
        stationIpTreeView = new TreeView<>(rootItem);
        stationIpTreeView.setShowRoot(false);
        stationIpTreeView.setEditable(false);

        stationIpTreeView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() >= 2) {
                var item = stationIpTreeView.getSelectionModel().getSelectedItem();
                if (!categories.contains(item.getValue())) {
                    contentTextField.setText(item.getValue());
                    stationIpTreeView.getSelectionModel().clearSelection();
                    actionButton.hide();
                }
            }
        });
    }

    /**
     * Populates the context menu tree view with set values.
     *
     * @param contentToPopulate The content to populate.
     */
    public void populateMenu(Map<String, Collection<String>> contentToPopulate) {
        if (contentToPopulate != null && !contentToPopulate.isEmpty()) {
            if (Platform.isFxApplicationThread()) {
                doPopulateTreeView(contentToPopulate);
            } else {
                Platform.runLater(() -> doPopulateTreeView(contentToPopulate));
            }
        }
    }

    /**
     * Populates the context menu tree view with set values.
     *
     * @param contentToPopulate The content to populate.
     */
    private void doPopulateTreeView(Map<String, Collection<String>> contentToPopulate) {
        categories.clear();
        for (var entry : contentToPopulate.entrySet()) {
            var stationName = entry.getKey();
            var stationIps = entry.getValue();
            if (!stationIps.isEmpty()) {
                var stationTreeItem = new TreeItem<>(stationName);
                stationTreeItem.setExpanded(true);
                for (var ip : stationIps) {
                    var ipTreeItem = new TreeItem<>(ip);
                    stationTreeItem.getChildren().add(ipTreeItem);
                }
                categories.add(stationName);
                rootItem.getChildren().add(stationTreeItem);
            }
        }
    }

    /**
     * Initializes the custom menu item and attaches it to the menu button.
     */
    private void initializeCustomMenuItem() {
        CustomMenuItem stationIpMenuItem = new CustomMenuItem(stationIpTreeView);
        stationIpMenuItem.setHideOnClick(false);
        actionButton.getItems().add(stationIpMenuItem);
    }
}
