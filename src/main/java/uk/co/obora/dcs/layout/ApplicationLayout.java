package uk.co.obora.dcs.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import uk.co.obora.dcs.routes.InjectorRoute;
import uk.co.obora.dcs.routes.ServerEditor;

import static com.vaadin.flow.theme.lumo.LumoUtility.Padding.SMALL;

@Layout
public class ApplicationLayout extends AppLayout {

    public ApplicationLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("DCS Multi-Injector");
        title.getStyle()
            .set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0");

        addToDrawer(createSideNav());
        addToNavbar(toggle, title);
    }

    private Scroller createSideNav() {
        SideNav nav = new SideNav();

        SideNavItem injector = new SideNavItem("Code Injector", InjectorRoute.class, VaadinIcon.PLAY.create());
        injector.addItem(new SideNavItem("Lua Code", InjectorRoute.class, VaadinIcon.CODE.create()));
        injector.addItem(new SideNavItem("Server Editor", ServerEditor.class, VaadinIcon.SERVER.create()));

        nav.addItem(injector);

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(SMALL);

        return scroller;
    }
}
