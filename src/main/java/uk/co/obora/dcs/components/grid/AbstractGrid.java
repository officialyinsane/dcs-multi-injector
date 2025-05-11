package uk.co.obora.dcs.components.grid;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.obora.dcs.components.Notifier;
import uk.co.obora.dcs.entity.DcsServer;
import uk.co.obora.dcs.service.AbstractDbService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Getter
@Slf4j
public abstract class AbstractGrid<T> extends Grid<T> {

    private final Map<String, Map<String, Object>> columnAttributes = new HashMap<>();
    private final Map<String, ValueProvider<T, ?>> columnDefinitions = new LinkedHashMap<>();

    private final AbstractDbService<T> service;

    public AbstractGrid(Class<T> clz, Consumer<ItemDoubleClickEvent<T>> consumer, AbstractDbService<T> service) {
        super(clz, false);

        this.service = service;
        columnDefinitions.putAll(setupColumnDefinitions());
        columnAttributes.putAll(setupColumnAttributes());

        columnDefinitions.entrySet().forEach(this::createColumn);

        super.getColumns().getLast().setFlexGrow(0);
        addItemDoubleClickListener(consumer::accept);

        setWidthFull();
        setHeight("100%");

        setDataProvider(
            DataProvider.fromCallbacks(
                query -> findSafely(service, query),
                query -> getCountSafely(service, query)));

        // TODO: Filtering using this: https://vaadin.com/docs/latest/flow/binding-data/data-provider
    }

    // TODO: This method just renders the text of the object
    protected static ComponentRenderer<Icon, DcsServer> iconRendererWithClickListener(VaadinIcon icon, ComponentEventListener<ClickEvent<Icon>> consumer) {
        return new ComponentRenderer<>(server -> createIconWithClickListener(icon.create(), consumer));
    }

    protected static Icon createIconWithClickListener(Icon icon, ComponentEventListener<ClickEvent<Icon>> consumer) {
        icon.addClickListener(consumer);
        return icon;
    }

    private static <T> Stream<T> findSafely(AbstractDbService<T> service, Query<T,Void> query) {
        try {
            return service.find(query.getOffset(), query.getLimit());
        } catch (Throwable t) {
            log.error("Failed to fetch items from database", t);
            Notifier.showError("Failed to communicate with the database.");
            return Stream.empty();
        }
    }

    private static <T> int getCountSafely(AbstractDbService<T> service, Query<T,Void> query) {
        try {
            return service.getCount().intValue(); // note, Vaadin assumes int for Count. Things get interesting at Integer.MAX_VALUE
        } catch (Throwable t) {
            log.error("Failed to fetch count from database", t);
            Notifier.showError("Failed to communicate with the database.");
            return 0;
        }
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

    protected abstract LinkedHashMap<String, ValueProvider<T, ?>> setupColumnDefinitions();
    protected abstract Map<String, Map<String, Object>> setupColumnAttributes();
}
