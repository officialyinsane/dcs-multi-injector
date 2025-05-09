package uk.co.obora.dcs.components.grid;

import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.function.ValueProvider;
import org.apache.commons.lang3.StringUtils;
import uk.co.obora.dcs.entity.DcsServer;
import uk.co.obora.dcs.service.DcsServerService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DcsServerGrid extends AbstractGrid<DcsServer> { // TODO: Add table filtering and think about pagination

    public DcsServerGrid(DcsServerService service, Consumer<ItemDoubleClickEvent<DcsServer>> consumer) {
        super(DcsServer.class, consumer, service);
    }

    @Override
    protected LinkedHashMap<String, ValueProvider<DcsServer, ?>> setupColumnDefinitions() {
        LinkedHashMap<String, ValueProvider<DcsServer, ?>> definitions = new LinkedHashMap<>();

        definitions.put("Name", DcsServer::getName);
        definitions.put("Hostname", DcsServer::getHostname);
        definitions.put("Port", DcsServer::getPort);
        definitions.put("Password", server -> StringUtils.repeat("*", server.getPassword().length()));

        return definitions;
    }

    @Override
    protected Map<String, Map<String, Object>> setupColumnAttributes() {
        return Map.of(
            "Name", Map.of("sortable", true),
            "Hostname", Map.of("sortable", true),
            "Port", Map.of("sortable", true)
        );
    }
}
