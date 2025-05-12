package gov.mil.otc._3dvis.ui.widgets.validation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * A class that validates a text field for an EntityType.
 */
public class EntityTypeValidationListener implements ChangeListener<String> {

    private final TextField textField;

    /**
     * The constructor.
     *
     * @param textField The text field to validate content against.
     */
    public EntityTypeValidationListener(TextField textField) {
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

        if (countDots(newValue) > 6) {
            textField.setText(oldValue);
        }

        if (countDots(newValue) > 6) {
            textField.setText(oldValue);
        }

        validateEntityType(oldValue, newValue);

        if (!textField.getText().isEmpty() &&
                !textField.getText().matches("^(?:[0-9]{1,3}\\.){6}[0-9]{1,3}$")) {
            textField.setStyle("-fx-text-fill:red;");
        } else {
            textField.setStyle(null);
        }
    }

    /**
     * Count the number of periods entered.
     *
     * @param value The string to search.
     * @return The number of periods.
     */
    private int countDots(String value) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }

    /**
     * Validate the entity type string.
     *
     * @param oldValue The original value.
     * @param newValue The new value.
     */
    private void validateEntityType(String oldValue, String newValue) {
        String[] fields = newValue.split("\\.", -1);
        if (fields.length > 7) {
            textField.setText(oldValue);
        } else {
            validateFields(fields, oldValue, newValue);
        }
    }

    /**
     * Validate each field of the entity type for numeric values in correct range.
     *
     * @param fields   The entity type fields.
     * @param oldValue The original value.
     * @param newValue The new value.
     */
    private void validateFields(String[] fields, String oldValue, String newValue) {
        StringBuilder validatedValue = new StringBuilder();
        String prefix = "";
        for (int i = 0; i < fields.length; i++) {
            try {
                int value = Integer.parseInt(fields[i]);
                if (value < 0 || (i != 2 && value > 255) || value > 65535) {
                    textField.setText(oldValue);
                    return;
                }
                validatedValue.append(prefix);
                validatedValue.append(value);
                prefix = ".";
            } catch (Exception e) {
                validatedValue.append(prefix);
                prefix = ".";
            }
        }

        if (!newValue.equals(validatedValue.toString())) {
            textField.setText(validatedValue.toString());
        }
    }
}
