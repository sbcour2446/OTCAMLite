package gov.mil.otc._3dvis.ui.widgets.validation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class whose children validate a {@link Control} containing some representation of a {@link Number} in a range.
 * @param <X> The type of number being validated.
 * @param <T> The secondary representation of the input control. For example, a string for
 *           a {@link javafx.scene.control.TextField}.
 */
public abstract class NumberValidationListener<X extends Number, T> implements ChangeListener<T> {
    /**
     * The minimum value in the range.
     */
    protected final X minimum;
    /**
     * The maximum value in the range.
     */
    protected final X maximum;
    /**
     * The UI control element validation is being performed against.
     */
    protected final Control control;

    /**
     * List of listeners asking about validation information.
     */
    private final List<IValidationStateListener> listeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructor.
     *
     * @param control The control object that validation is being performed against.
     * @param minValue The minimum value (inclusive) in the range of supported values.
     * @param maxValue The maximum value (inclusive) in the range of supported values.
     */
    protected NumberValidationListener(Control control, X minValue, X maxValue) {
        this.control = control;
        this.minimum = minValue;
        this.maximum = maxValue;

        if (control == null) {
            throw new IllegalArgumentException("The UX control object cannot be null");
        }
    }

    /**
     * Performed when the content of an {@link javafx.beans.Observable} has changed.
     * This must be
     *
     * @param observable The instance of the {@link javafx.beans.Observable} that has changed.
     * @param oldValue The old value contained in the observable.
     * @param newValue The new value contained in the observable.
     */
    public abstract void onChanged(ObservableValue<? extends T> observable, T oldValue, T newValue);

    /**
     * {@inheritDoc}
     */
    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        onChanged(observable, oldValue, newValue);
    }

    /**
     * Adds a listener to this object, if it has not already been added.
     *
     * @param listener The listener to add.
     */
    public void addListener(IValidationStateListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    /**
     * Adds a listener to this object, if it has not already been added.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(IValidationStateListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Stimulates listeners with values they should be made aware of.
     *
     * @param state The new {@link IValidationStateListener.ValidationState}.
     */
    protected void stimulateListeners(IValidationStateListener.ValidationState state) {
        synchronized (listeners) {
            for (IValidationStateListener listener : listeners) {
                listener.onValidation(state, control);
            }
        }
    }

}
