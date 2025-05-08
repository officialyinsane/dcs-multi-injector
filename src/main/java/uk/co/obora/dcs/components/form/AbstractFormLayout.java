package uk.co.obora.dcs.components.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Getter
public abstract class AbstractFormLayout<T> extends FormLayout {

    private Map<String, Component> fields = new LinkedHashMap<>();
    private Set<String> fieldNamesInOrder = new LinkedHashSet<>();
    private T cachedItem;

    public AbstractFormLayout() {
        fieldNamesInOrder.addAll(setupFieldOrdering());
        fields.putAll(createFields());
        fieldNamesInOrder.forEach(field -> {
            this.createField(Map.entry(field, fields.get(field)));
        });
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
        add(component);
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

    protected abstract Set<String> setupFieldOrdering();
    protected abstract Map<String, Component> createFields();

}
