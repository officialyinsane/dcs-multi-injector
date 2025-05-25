package uk.co.obora.dcs.routes;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.obora.dcs.Injector;
import uk.co.obora.dcs.dto.InboundPacket;
import uk.co.obora.dcs.entity.DcsServer;
import uk.co.obora.dcs.service.DcsServerService;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

@Route("dcs-injector")
@RolesAllowed("REGULAR_USER")
@Slf4j
public class InjectorRoute extends VerticalLayout {

    private static final Integer DEFAULT_TIMEOUT_SECS = 3;

    private final DcsServerService service;
    private final ComboBox<DcsServer> serverSelect = new ComboBox<>();
    private final TextArea code = new TextArea();

    private static final int MAX_CODE_LENGTH = 100000;

    @Autowired
    public InjectorRoute(DcsServerService service) {
        this.service = service;

        setWidthFull();
        setHeightFull();
        setAlignItems(CENTER);

        add(createSelectBar(), createCodeArea());
    }

    private HorizontalLayout createSelectBar() {
        HorizontalLayout selectBar = new HorizontalLayout();
        selectBar.setWidthFull();

        List<DcsServer> servers = service.fetchDcsServersSafely().stream()
            .sorted(Comparator.comparing(DcsServer::getName))
            .toList();

        selectBar.add(createServerSelect(servers), createIcons());

        return selectBar;
    }

    private ComboBox<DcsServer> createServerSelect(List<DcsServer> servers) {
        serverSelect.setItems(servers);
        serverSelect.setValue(servers.getFirst());
        serverSelect.setItemLabelGenerator(DcsServer::getName);
        serverSelect.setWidthFull();

        return serverSelect;
    }

    private TextArea createCodeArea() {
        code.setMaxLength(MAX_CODE_LENGTH);
        code.setWidthFull();
        code.setLabel("Lua Code");
        code.setMinHeight("500px");
        code.setHeightFull();

        return code;
    }

    private HorizontalLayout createIcons() {
        HorizontalLayout icons = new HorizontalLayout();

        Icon play = VaadinIcon.PLAY.create();
        play.addClickListener(e -> {
            executeCode();
        });
        icons.add(play);
        icons.getStyle().set("margin-top", "10px");
        return icons;
    }

    private void executeCode() {
        try {
            AtomicReference<Throwable> ex = new AtomicReference<>();

            InboundPacket reply = Injector.doInjection(serverSelect.getValue(), code.getValue())
                .block(Duration.ofSeconds(DEFAULT_TIMEOUT_SECS)); // TODO: Make this configurable per server

            if (ex.get() != null) {
                displayException(ex.get());
            } if (reply == null) {
                showNotification("Injection failed, see logs. ", LUMO_ERROR);
                log.error("Failed to get a reply from the server.");
            } else {
                showNotification("Injection completed: " + reply.getStatus(), LUMO_SUCCESS);
            }
        } catch (Exception e) {
            displayException(e);
        }
    }

    private static void displayException(Throwable t) {
        String message = "Injection failed: " + t.getMessage();

        if (t.getCause() instanceof TimeoutException) {
            message = "Injection timed out. Likely incorrect Lua.";
        }
        showNotification(message, LUMO_ERROR);
    }

    private static void showNotification(String message, NotificationVariant... variants) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variants);
    }

}
