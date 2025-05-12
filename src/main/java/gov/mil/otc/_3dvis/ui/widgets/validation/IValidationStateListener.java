package gov.mil.otc._3dvis.ui.widgets.validation;

import javafx.scene.control.Control;

/**
 * A listener interface that can be registered to any derivative of a {@link NumberValidationListener} to monitor input
 * feedback states.
 */
public interface IValidationStateListener {
    /**
     * The possible states a field under validation can take. Validation occurs when the user has cleared, edited,
     * selected, or deselected a {@link Control}.
     */
    enum ValidationState {
        /**
         * The conditional state of the input was invalid.
         */
        INVALID,
        /**
         * The conditional state of the input was valid.
         */
        VALID,
        /**
         * The conditional state of the input was empty.
         */
        EMPTY
    }

    /**
     * Invoked when a validation occurs on a {@link Control}.  This should occur when the user has
     * cleared, edited, selected, or deselected a user experience control.
     *
     * @param validationState The current validation state after the user modification.
     * @param control The instance of the user element for which the validation was performed on.
     */
    void onValidation(ValidationState validationState, Control control);
}
