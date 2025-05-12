package gov.mil.otc._3dvis.ui.widgets.validation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * A class that validates a text field for a Military Symbol Code.
 */
public class MilitarySymbolValidationListener implements ChangeListener<String> {

    private final TextField textField;

    /**
     * The constructor.
     *
     * @param textField The text field to validate content against.
     */
    public MilitarySymbolValidationListener(TextField textField) {
        this.textField = textField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue.isEmpty()) {
            return;
        }

        if (newValue.length() > 15) {
            textField.setText(oldValue);
        }

        if (!newValue.matches("^[a-zA-Z\\*\\-]*$")) {
            textField.setText(oldValue);
        }
    }
}
