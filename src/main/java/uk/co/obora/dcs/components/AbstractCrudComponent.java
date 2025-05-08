package uk.co.obora.dcs.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import uk.co.obora.dcs.components.form.AbstractFormLayout;
import uk.co.obora.dcs.components.grid.AbstractGrid;
import uk.co.obora.dcs.service.AbstractDbService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Getter
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
        add(createSearchAbleGrid(grid), formWithButtons);
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
            createButton("Save", false, event -> {
                saveItem();
                setEditingMode(false);
            }),
            createButton("Discard", false, event -> {
                form.clearAllFields();
                setEditingMode(false);
            }),
            createButton("Delete", false, event -> {
                deleteItem();
                setEditingMode(false);
            })
        );
    }

    protected Button createButton(String text, boolean enabled, Consumer<ClickEvent<Button>> consumer) {
        Button button = new Button(text, consumer::accept);
        button.setEnabled(enabled);
        return button;
    }

    protected Optional<Button> getButton(String text) {
        return Optional.ofNullable(buttons.get(text));
    }

    protected void setItems(List<T> items) {
        grid.setItems(items);
    }

    private VerticalLayout createSearchAbleGrid(GRID grid) {
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
        service.save(form.getItem()); // TODO: Notifications
        grid.getDataProvider().refreshAll();
        refreshItems();
        setEditingMode(false);
    }

    private void deleteItem() {
        service.delete(form.getItem()); // TODO: Notifications
        grid.getDataProvider().refreshAll();
        refreshItems();
        setEditingMode(false);
    }

    protected abstract FORM createFormLayout();
    protected abstract GRID createGrid(SERVICE service, Consumer<ItemDoubleClickEvent<T>> consumer);
    protected abstract List<Button> getFormButtons();
    protected abstract void refreshItems(); // TODO: This is a hack, should use the DataProvider
}
