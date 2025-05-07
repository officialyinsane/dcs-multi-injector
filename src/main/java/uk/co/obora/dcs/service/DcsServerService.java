package uk.co.obora.dcs.service;

import com.vaadin.flow.spring.annotation.UIScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.obora.dcs.entity.DcsServer;
import uk.co.obora.dcs.repository.DcsServerRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@UIScope
public class DcsServerService {

    private final DcsServerRepository repository;

    public List<DcsServer> list() {
        return repository.findAll();
    }

    public void save(DcsServer server) {
        repository.save(server);
    }

    public List<DcsServer> fetchDcsServersSafely() {
        Map<String, DcsServer> servers = new HashMap<>();

        try {
            servers = list().stream()
                .collect(Collectors.toMap(DcsServer::getName, server -> server));
        } catch (Exception e) {
            log.warn("Failed to fetch DCS servers", e);
        }

        DcsServer defaultServerInstall = DcsServer.getDefaultInstallationDetails();
        servers.computeIfAbsent(defaultServerInstall.getName(), name -> defaultServerInstall);

        return new ArrayList<>(servers.values());

    }

    public void delete(DcsServer server) {
        repository.delete(server);
    }
}
