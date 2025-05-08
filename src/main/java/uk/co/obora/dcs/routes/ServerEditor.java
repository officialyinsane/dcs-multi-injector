package uk.co.obora.dcs.routes;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.obora.dcs.components.DcsServerCrudComponent;
import uk.co.obora.dcs.service.DcsServerService;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

@Route("server-editor")
@Slf4j
public class ServerEditor extends VerticalLayout {

    @Autowired
    public ServerEditor(DcsServerService service) {
        setWidthFull();
        setHeightFull();
        setAlignItems(CENTER);

        DcsServerCrudComponent crud = new DcsServerCrudComponent(service);

        add(crud);
        setFlexGrow(1, crud);
    }
}
