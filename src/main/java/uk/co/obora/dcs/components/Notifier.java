package uk.co.obora.dcs.components;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS;

public class Notifier {

    public static void showSuccess(String message) {
        showNotification(message, LUMO_SUCCESS);
    }

    public static void showError(String message) {
        showNotification(message, LUMO_ERROR);
    }
    public static void showNotification(String message, NotificationVariant... variants) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variants);
    }
}
