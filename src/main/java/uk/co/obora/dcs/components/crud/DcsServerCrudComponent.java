package uk.co.obora.dcs.components.crud;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import uk.co.obora.dcs.components.form.DcsServerFormLayout;
import uk.co.obora.dcs.components.grid.DcsServerGrid;
import uk.co.obora.dcs.entity.DcsServer;
import uk.co.obora.dcs.service.DcsServerService;

import java.util.List;
import java.util.function.Consumer;

public class DcsServerCrudComponent extends AbstractCrudComponent<DcsServer, DcsServerFormLayout, DcsServerGrid, DcsServerService> {

    public DcsServerCrudComponent(DcsServerService service) {
        super(service);
        setWidthFull();
        setHeightFull();
    }

    @Override
    protected List<Button> getFormButtons() {
        return getDefaultButtons();
    }

    @Override
    protected DcsServerFormLayout createFormLayout() {
        return new DcsServerFormLayout(getDefaultEnterConsumer(), getDefaultEscapeConsumer());
    }

    @Override
    protected DcsServerGrid createGrid(DcsServerService service, Consumer<ItemDoubleClickEvent<DcsServer>> consumer) {
        return new DcsServerGrid(service, consumer);
    }

    @Override
    protected void refreshItems() {
        getGrid().getDataProvider().refreshAll();
    }

}
