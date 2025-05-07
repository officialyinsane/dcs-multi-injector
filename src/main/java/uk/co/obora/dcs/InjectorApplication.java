package uk.co.obora.dcs;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.LoadDependenciesOnStartup;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@Theme(variant = Lumo.DARK)
@LoadDependenciesOnStartup
@SpringBootApplication
public class InjectorApplication extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(InjectorApplication.class, args);
    }
}
