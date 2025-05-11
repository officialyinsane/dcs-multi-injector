package uk.co.obora.dcs.components.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyUpEvent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class AbstractFormLayout<T> extends FormLayout {

    private final Consumer<Void> consumeEnterPressed;
    private final Consumer<Void> consumeEscapePressed;

    private Map<String, Component> fields = new LinkedHashMap<>();
    private T cachedItem;

    public AbstractFormLayout(Consumer<Void> consumeEnterPressed, Consumer<Void> consumeEscapePressed) {
        this.consumeEnterPressed = consumeEnterPressed;
        this.consumeEscapePressed = consumeEscapePressed;

        fields.putAll(createFields());
        fields.entrySet().forEach(this::createField);

        setResponsiveSteps(new ResponsiveStep("0", 1));
    }

    public void setEditingMode(boolean enabled) {
        fields.values().forEach(field -> this.setFieldEnabled(field, enabled));
        this.setVisible(enabled);
    }

    public void setEditable(T item) {
        setItem(item);
        setEditingMode(item != null);
    }

    public void clearAllFields() {
        setItem(null);
    }

    protected void setFieldValue(Component field, String value) {
        switch (field) {
            case TextField tf -> tf.setValue(value);
            case PasswordField pf -> pf.setValue(value);
            default -> throw new IllegalArgumentException("Unknown field type: " + field.getClass());
        }
    }

    protected void setFieldValue(Component field, int value) {
        if (field instanceof IntegerField) {
            ((IntegerField) field).setValue(value);
        } else {
            throw new IllegalArgumentException("Field is not an IntegerField: " + field.getClass());
        }
    }

    private void createField(Map.Entry<String, Component> entry) {
        Component component = entry.getValue();
        setFieldEnabled(component, false);

        if (component instanceof TextFieldBase<?, ?> base) {
            base.addKeyUpListener(event -> this.onKeyUp(event));
        }

        add(component);
    }

    private void onKeyUp(KeyUpEvent event) {
        if (event.getKey().equals(Key.ENTER)) {
            consumeEnterPressed.accept(null);
        } else { // this is needed because the key doesn't equal properly
            Key.ESCAPE.getKeys().stream()
                .filter(key -> key.equals(event.getKey().toString()))
                .findFirst()
                .ifPresent(k -> consumeEscapePressed.accept(null));
        }
    }

    private void setFieldEnabled(Component field, boolean enabled) {
        if (field instanceof HasEnabled) {
            ((HasEnabled) field).setEnabled(enabled);
        }
    }

    protected void cacheItem(T item) {
        this.cachedItem = item;
    }

    public abstract void setItem(T item);
    public abstract T getItem();

    protected abstract LinkedHashMap<String, Component> createFields();

}
