package uk.co.obora.dcs.components.crud;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.obora.dcs.components.Notifier;
import uk.co.obora.dcs.components.form.AbstractFormLayout;
import uk.co.obora.dcs.components.grid.AbstractGrid;
import uk.co.obora.dcs.service.AbstractDbService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;

@Getter
@Slf4j
public abstract class AbstractCrudComponent<T,
    FORM extends AbstractFormLayout<T>,
    GRID extends AbstractGrid<T>,
    SERVICE extends AbstractDbService<T>> extends HorizontalLayout {

    private final Map<String, Button> buttons = new HashMap<>();

    private final FORM form;
    private final GRID grid;
    private final SERVICE service;

    private final HorizontalLayout buttonBar;
    private final VerticalLayout formWithButtons;

    public AbstractCrudComponent(SERVICE service) {
        setWidthFull();
        setHeightFull();

        this.service = service;

        form = createFormLayout();
        grid = createGrid(service, event -> {
            setEditingMode(event.getItem() != null);
            form.setItem(event.getItem());
        });

        formWithButtons = new VerticalLayout();
        buttonBar = createButtonBar();
        formWithButtons.add(form, buttonBar);

        setEditingMode(false);

        add(createSearchableGrid(grid), formWithButtons);
        setFlexGrow(1, grid);
    }

    private void setEditingMode(boolean enabled) {
        form.setEditingMode(enabled);
        setButtonVisibility(enabled);
    }

    private void setButtonVisibility(boolean enabled) {
        buttons.entrySet().stream()
            .filter(entry -> !entry.getKey().equalsIgnoreCase("create"))
            .map(Map.Entry::getValue)
            .forEach(button -> {
                button.setVisible(enabled);
                button.setEnabled(enabled);
            });
        formWithButtons.setVisible(enabled);
    }

    protected List<Button> getDefaultButtons() {
        return List.of(
            createButton("Save", false, event -> onSaveEvent(), LUMO_PRIMARY),
            createButton("Discard", false, event -> onDiscardEvent()),
            createButton("Delete", false, event -> onDeleteEvent(), LUMO_PRIMARY, LUMO_ERROR)
        );
    }

    protected void onSaveEvent() {
        saveItem();
        setEditingMode(false);
    }

    protected void onDiscardEvent() {
        form.clearAllFields();
        setEditingMode(false);
    }

    protected void onDeleteEvent() {
        deleteItem();
        setEditingMode(false);
    }

    protected Button createButton(String text, boolean enabled, Consumer<ClickEvent<Button>> consumer, ButtonVariant... themeVariants) {
        Button button = new Button(text, consumer::accept);
        button.addThemeVariants(themeVariants);
        button.setEnabled(enabled);
        return button;
    }

    protected Optional<Button> getButton(String text) {
        return Optional.ofNullable(buttons.get(text));
    }

    protected Consumer<Void> getDefaultEnterConsumer() {
        return event -> onSaveEvent();
    }

    protected Consumer<Void> getDefaultEscapeConsumer() {
        return event -> onDiscardEvent();
    }

    private VerticalLayout createSearchableGrid(GRID grid) {
        VerticalLayout gridWithSearch = new VerticalLayout();
        HorizontalLayout searchBar = new HorizontalLayout();

        TextField searchText = new TextField("Search");
        // TODO: Implement searching on keypress - probably needs DataProvider

        Button create = new Button("Create", event -> {
            form.clearAllFields();
            setEditingMode(true);
        });
        create.getStyle().set("margin-top", "37px");
        storeButton(create);

        searchBar.add(searchText, create);
        searchBar.setWidthFull();
        searchBar.setFlexGrow(1, searchText);
        searchBar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        gridWithSearch.add(searchBar, grid);
        return gridWithSearch;
    }

    private HorizontalLayout createButtonBar() {
        HorizontalLayout buttonBar = new HorizontalLayout();

        getFormButtons().forEach(button -> {
            storeButton(button);
            buttonBar.add(button);
        });

        return buttonBar;
    }

    private void storeButton(Button button) {
        buttons.put(button.getText(), button);
    }

    private void saveItem() {
        try {
            service.save(form.getItem());
            grid.getDataProvider().refreshAll();
            refreshItems();
            setEditingMode(false);
            Notifier.showSuccess("Saved.");
        } catch (Throwable t) {
            log.error("Failed to save item: {}", form.getItem(), t);
            Notifier.showError("Failed to save item.");
        }
    }

    private void deleteItem() {
        try {
            service.delete(form.getItem());
            grid.getDataProvider().refreshAll();
            refreshItems();
            setEditingMode(false);
            Notifier.showSuccess("Deleted.");
        } catch (Throwable t) {
            log.error("Failed to delete item: {}", form.getItem(), t);
            Notifier.showError("Failed to delete item.");
        }
    }

    protected abstract FORM createFormLayout();
    protected abstract GRID createGrid(SERVICE service, Consumer<ItemDoubleClickEvent<T>> consumer);
    protected abstract List<Button> getFormButtons();
    protected abstract void refreshItems();
}
