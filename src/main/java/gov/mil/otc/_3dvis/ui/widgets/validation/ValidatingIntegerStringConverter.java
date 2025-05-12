package gov.mil.otc._3dvis.ui.widgets.validation;

import javafx.util.converter.IntegerStringConverter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ValidatingIntegerStringConverter extends IntegerStringConverter {

    @Override
    public String toString(Integer object) {
        try {
            return super.toString(object);
        } catch (NumberFormatException e) {
            Logger.getGlobal().log(Level.FINER, null, e);
        }
        return "";
    }

    @Override
    public Integer fromString(String string) {
        try {
            return super.fromString(string);
        } catch (NumberFormatException e) {
            Logger.getGlobal().log(Level.FINER, null, e);
        }
        return 0;
    }
}
