package gov.mil.otc._3dvis.ui.widgets.utilities;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Contains reusable code to perform operations such as populating, theming, and other preferences in standing up
 * new user experiences.
 */
public class WidgetUtilities {
    /**
     * Adds validators and ranges on ID spinner values provided by the user, and corrects bad input.
     *
     * @param spinner The spinner to apply the value factories and converters to.
     */
    public static void addIdSpinnerValidators(Spinner<Integer> spinner) {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65533);

        StringConverter<Integer> stringConverter = new StringConverter<>() {
            @Override
            public String toString(Integer integer) {
                return integer.toString();
            }

            @Override
            public Integer fromString(String string) {
                if (string.matches("-?\\d+")) {
                    int integer;
                    try {
                        integer = Integer.parseInt(string);
                    } catch (NumberFormatException e) {
                        integer = valueFactory.getMax();
                    }
                    return integer;
                }
                return 0;
            }
        };
        valueFactory.setConverter(stringConverter);

        spinner.setValueFactory(valueFactory);

        spinner.getEditor().textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("\\d*")) {
                spinner.getEditor().setText(oldValue);
            }
        });
    }

    private WidgetUtilities() {
    }
}
