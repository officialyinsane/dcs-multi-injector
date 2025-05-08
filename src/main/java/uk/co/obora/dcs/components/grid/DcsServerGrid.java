package uk.co.obora.dcs.components.grid;

import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.function.ValueProvider;
import org.apache.commons.lang3.StringUtils;
import uk.co.obora.dcs.entity.DcsServer;
import uk.co.obora.dcs.service.DcsServerService;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DcsServerGrid extends AbstractGrid<DcsServer> { // TODO: Add table filtering and think about pagination

    public DcsServerGrid(DcsServerService service, Consumer<ItemDoubleClickEvent<DcsServer>> consumer) {
        super(DcsServer.class, consumer);

        super.setItems(service.fetchDcsServersSafely());
    }

    @Override
    protected Map<String, ValueProvider<DcsServer, ?>> setupColumnDefinitions() {
        return Map.of(
            "Name", DcsServer::getName,
            "Hostname", DcsServer::getHostname,
            "Port", DcsServer::getPort,
            "Password", server -> StringUtils.repeat("*", server.getPassword().length())
        );
    }

    @Override
    protected Map<String, Map<String, Object>> setupColumnAttributes() {
        return Map.of(
            "Name", Map.of("sortable", true),
            "Hostname", Map.of("sortable", true),
            "Port", Map.of("sortable", true)
        );
    }

    @Override
    protected Set<String> setupColumnOrdering() {
        LinkedHashSet<String> ordering = new LinkedHashSet<>();
        ordering.add("Name");
        ordering.add("Hostname");
        ordering.add("Port");
        ordering.add("Password");
        return ordering;
    }
}
