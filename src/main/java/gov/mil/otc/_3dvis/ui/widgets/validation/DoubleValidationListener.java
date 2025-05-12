package gov.mil.otc._3dvis.ui.widgets.validation;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import java.util.Objects;

/**
 * A class that validates a text field for double content that represents a range.
 */
public class DoubleValidationListener extends NumberValidationListener<Double, String> {

    private final TextField textField;

    /**
     * Constructor.
     *
     * @param textField The text field to validate content against.
     * @param minimumValue The minimum value (inclusive) in the range of supported values.
     * @param maximumValue The maximum value (inclusive) in the range of supported values.
     */
    public DoubleValidationListener(TextField textField, double minimumValue, double maximumValue) {
        super(textField, minimumValue, maximumValue);

        if (minimum > maximum) {
            throw new IllegalArgumentException("The minimum value is larger than the maximum value");
        }

        if (Objects.equals(minimum, maximum)) {
            throw new IllegalArgumentException("The supported range is a single number");
        }

        this.textField = (TextField) control;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        // Support empty fields
        if (newValue.isEmpty()) {
            textField.setText(newValue);
            stimulateListeners(IValidationStateListener.ValidationState.EMPTY);
        }
        // If the minimum is a negative number and an negative symbol is provided by the user, allow it
        else if (minimum < 0 && newValue.equals("-")) {
            textField.setText(newValue);
            stimulateListeners(IValidationStateListener.ValidationState.VALID);
        }
        // Try and create a number and check the ranges
        else {
            try {
                var valueOf = Double.parseDouble(newValue);
                // If the values are out of bounds, don't add them
                if (valueOf >= minimum && valueOf <= maximum) {
                    textField.setText(newValue);
                    stimulateListeners(IValidationStateListener.ValidationState.VALID);
                }
                else {
                    textField.setText(oldValue);
                    stimulateListeners(IValidationStateListener.ValidationState.INVALID);
                }
            }
            catch (NumberFormatException e) {
                textField.setText(oldValue);
                stimulateListeners(IValidationStateListener.ValidationState.INVALID);
            }
        }
    }
}
