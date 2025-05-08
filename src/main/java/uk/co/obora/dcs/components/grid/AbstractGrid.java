package uk.co.obora.dcs.components.grid;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.obora.dcs.entity.DcsServer;

import java.util.*;
import java.util.function.Consumer;

@Getter
@Slf4j
public abstract class AbstractGrid<T> extends Grid<T> {

    private Map<String, Map<String, Object>> columnAttributes = new HashMap<>();
    private Map<String, ValueProvider<T, ?>> columnDefinitions = new LinkedHashMap<>();
    private Set<String> columnNamesInOrder = new LinkedHashSet<>();

    public AbstractGrid(Class<T> clz, Consumer<ItemDoubleClickEvent<T>> consumer) {
        super(clz, false);

        columnNamesInOrder.addAll(setupColumnOrdering());
        columnDefinitions.putAll(setupColumnDefinitions());
        columnAttributes.putAll(setupColumnAttributes());


        columnNamesInOrder.forEach(column -> {
            // TODO: should use computeIfAbsent to provide a default implementation
            ValueProvider<T, ?> valueProvider = columnDefinitions.get(column);
            this.createColumn(Map.entry(column, valueProvider));
        });
        /*columnDefinitions.entrySet().forEach(this::createColumn);*/

        super.getColumns().getLast().setFlexGrow(0);
        addItemDoubleClickListener(consumer::accept);

        setWidthFull();
        setHeight("100%");
    }

    private void createColumn(Map.Entry<String, ValueProvider<T, ?>> entry) {
        Grid.Column<T> column = super.addColumn(entry.getValue());
        column.setHeader(entry.getKey());

        columnAttributes.computeIfAbsent(entry.getKey(), k -> new HashMap<>())
            .entrySet()
            .forEach(attribute -> {
                applyAttribute(column, attribute);
            });
    }

    private void applyAttribute(Grid.Column<T> column, Map.Entry<String, Object> attribute) {
        switch (attribute.getKey()) {
            case "sortable": column.setSortable(getAttributeType(attribute.getValue(), false)); break;
            default: throw new IllegalArgumentException("Unknown attribute: " + attribute.getKey());
        }
    }

    @SuppressWarnings("unchecked")
    private <A> A getAttributeType(Object o, A defaultValue) {
        return (A) switch (defaultValue) {
            case Boolean b -> {
                if (o == null) {
                    yield b;
                }
                yield Boolean.valueOf(o.toString());
            }
            case Integer i -> {
                if (o == null) {
                    yield i;
                }
                yield Integer.valueOf(o.toString());
            }
            case String s -> {
                if (o == null) {
                    yield s;
                }
                yield o.toString();
            }
            case null -> throw new IllegalArgumentException("Attribute default value cannot be null.");
            default -> throw new IllegalArgumentException("Unknown attribute type: " + defaultValue.getClass());
        };
    }

    protected static ComponentRenderer<Icon, DcsServer> iconRendererWithClickListener(VaadinIcon icon, ComponentEventListener<ClickEvent<Icon>> consumer) {
        return new ComponentRenderer<>(server -> createIconWithClickListener(icon.create(), consumer));
    }

    protected static Icon createIconWithClickListener(Icon icon, ComponentEventListener<ClickEvent<Icon>> consumer) {
        icon.addClickListener(consumer);
        return icon;
    }

    protected abstract Set<String> setupColumnOrdering();
    protected abstract Map<String, ValueProvider<T, ?>> setupColumnDefinitions();
    protected abstract Map<String, Map<String, Object>> setupColumnAttributes();
}
