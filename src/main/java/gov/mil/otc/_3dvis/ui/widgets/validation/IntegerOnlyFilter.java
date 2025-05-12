package gov.mil.otc._3dvis.ui.widgets.validation;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class IntegerOnlyFilter implements UnaryOperator<TextFormatter.Change> {

    private final int minValue;
    private final int maxValue;

    public IntegerOnlyFilter() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntegerOnlyFilter(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public TextFormatter.Change apply(TextFormatter.Change change) {
        String newText = change.getControlNewText();

        // Deletion should always be possible.
        if (change.isDeleted()) {
            if (validateValue(newText)) {
                return change;
            } else {
                return null;
            }
        }

        if (validateValue(newText)) {
            return change;
        } else {
            return null;
        }
    }

    private boolean validateValue(String newText) {
        // Try parsing and check if the result is in [minValue, maxValue].
        try {
            int n = Integer.parseInt(newText);
            return minValue <= n && n <= maxValue;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
