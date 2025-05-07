package uk.co.obora.dcs.routes;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.obora.dcs.entity.DcsServer;
import uk.co.obora.dcs.service.DcsServerService;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

@Route("server-editor")
public class ServerEditor extends VerticalLayout {

    private static final String ACTION_WIDTH = "120px";
    private static final String NON_ADD_WIDTH = "80px";

    private final DcsServerService service;
    private final Grid<DcsServer> grid = new Grid<>(DcsServer.class, false);

    @Autowired
    public ServerEditor(DcsServerService service) {
        this.service = service;

        setWidthFull();
        setAlignItems(CENTER);
        createGrid();

        add(grid);
    }

    private void createGrid() {
        Grid.Column<DcsServer> name = grid.addColumn(DcsServer::getName)
            .setSortable(true);
        Grid.Column<DcsServer> hostname = grid.addColumn(DcsServer::getHostname)
            .setSortable(true);
        Grid.Column<DcsServer> port = grid.addColumn(DcsServer::getPort)
            .setSortable(true);
        Grid.Column<DcsServer> password = grid.addColumn(server -> server.getPassword()
                .replaceAll(".", "*"))
            .setSortable(true);
        Grid.Column<DcsServer> actions = grid.addColumn(new ComponentRenderer<>(server -> {

            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(CENTER);

            Icon save = VaadinIcon.CHECK.create();
            save.addClickListener(e -> {
                service.save(server);
                grid.getDataProvider().refreshItem(server);
            });
            Icon delete = VaadinIcon.TRASH.create();
            delete.addClickListener(e -> {
                service.delete(server);
                grid.getDataProvider().refreshItem(server);
            });

            layout.add(save, delete);
            if (server.getName().isEmpty()) {
                Icon addIcon = VaadinIcon.PLUS.create();
                addIcon.addClickListener(e -> {
                    // TODO: Implement
                });

                layout.add(addIcon);
                layout.setMaxWidth(ACTION_WIDTH);
                // TODO: This row is wider, makes the icons smaller - change to 1 column per  icon
            } else {
                layout.setMaxWidth(NON_ADD_WIDTH);
            }

            return layout;
        }));
        actions.setWidth(ACTION_WIDTH);

        List<DcsServer> servers = service.fetchDcsServersSafely();
        servers.add(DcsServer.getBlankInstance());
        GridListDataView<DcsServer> dataView = grid.setItems(servers);
        ServerFilter filter = new ServerFilter(dataView);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(name).setComponent(
            createFilterHeader("Name", filter::setName, true));
        headerRow.getCell(hostname).setComponent(
            createFilterHeader("Hostname", filter::setHostname, true));
        headerRow.getCell(port).setComponent(
            createFilterHeader("Port", filter::setPort, true));
        headerRow.getCell(password).setComponent(
            createFilterHeader("Password", null, false));

        grid.getColumns().getLast().setFlexGrow(0);
        grid.setAllRowsVisible(true);
        grid.setItems(servers);
    }

    private static Component createFilterHeader(String labelText, Consumer<String> filterChangeConsumer,
                                                boolean canFilter) {
        NativeLabel label = new NativeLabel(labelText);
        label.getStyle()
            .set("padding-top", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-xs)");

        TextField field = new TextField();
        field.setValueChangeMode(EAGER);
        field.setClearButtonVisible(true);
        field.addThemeVariants(LUMO_SMALL);
        field.setWidthFull();
        field.setMaxWidth("100%");
        field.addValueChangeListener(e -> filterChangeConsumer.accept(e.getValue()));
        field.setEnabled(canFilter);

        VerticalLayout layout = new VerticalLayout(label, field);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");
        return layout;
    }

    private static class ServerFilter {
        private final GridListDataView<DcsServer> dataView;

        private String name;
        private String hostname;
        private Integer port;

        public ServerFilter(GridListDataView<DcsServer> dataView) {
            this.dataView = dataView;
        }

        public void setName(String name) {
            this.name = name;
            dataView.refreshAll();
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
            dataView.refreshAll();
        }

        public void setPort(String port) throws NumberFormatException {
            this.port = Integer.valueOf(port);
            dataView.refreshAll();
        }

        public boolean test(DcsServer server) {
            return matches(server.getName(), name)
                && matches(server.getHostname(), hostname)
                && matches(server.getPort(), port);
        }

        private boolean matches(Integer value, Integer searchTerm) {
            return searchTerm == null || matches(value.toString(), searchTerm.toString());
        }

        private boolean matches(String value, String searchTerm) {
            boolean simpleMatch = searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());

            if (simpleMatch) {
                return true;
            }

            try {
                Pattern pattern = Pattern.compile(searchTerm);
                return pattern.matcher(value).find();
            } catch (PatternSyntaxException e) {
                // swallow the exception, return false
                return false;
            }
        }
    }
}
