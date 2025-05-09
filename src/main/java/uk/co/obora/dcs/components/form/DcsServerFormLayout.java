package uk.co.obora.dcs.components.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import uk.co.obora.dcs.entity.DcsServer;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class DcsServerFormLayout extends AbstractFormLayout<DcsServer> {

    private static final int DEFAULT_PORT = 18080;

    public DcsServerFormLayout(Consumer<Void> consumeEnterPressed, Consumer<Void> consumeEscapePressed) {
        super(consumeEnterPressed, consumeEscapePressed);
    }

    @Override
    protected LinkedHashMap<String, Component> createFields() {
        LinkedHashMap<String, Component> fields = new LinkedHashMap<>();

        fields.put("Name", new TextField("Name"));
        fields.put("Hostname", new TextField("Hostname"));
        fields.put("Port", new IntegerField("Port"));
        fields.put("Password", new PasswordField("Password"));

        return fields;
    }

    @Override
    public void setItem(DcsServer server) {
        super.cacheItem(server);
        getFields().forEach((key, value) -> {
            switch (key) {
                case "Name":
                    setFieldValue(value, server == null
                         ? ""
                         : server.getName());
                    break;
                case "Hostname":
                    setFieldValue(value, server == null
                         ? ""
                         : server.getHostname());
                    break;
                case "Port":
                    setFieldValue(value, server == null
                         ? DEFAULT_PORT
                         : server.getPort());
                    break;
                case "Password":
                    setFieldValue(value, server == null
                         ? ""
                         : server.getPassword());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown field: " + key);
            }
        });
    }

    @Override
    public DcsServer getItem() {
        return DcsServer.builder()
            .id(getCachedItem() == null ? null : getCachedItem().getId())
            .name(getFieldValueAsString("Name"))
            .hostname(getFieldValueAsString("Hostname"))
            .port(Integer.valueOf(getFieldValueAsString("Port")))
            .password(getFieldValueAsString("Password"))
            .build();
    }

    protected String getFieldValueAsString(String key) throws IllegalArgumentException {
        Component component = getFields().get(key);
        return switch (component) {
            case TextField f -> f.getValue();
            case PasswordField f -> f.getValue();
            case IntegerField f -> Integer.toString(f.getValue());
            default -> throw new IllegalArgumentException("Unknown field type: " + component.getClass());
        };
    }
}
