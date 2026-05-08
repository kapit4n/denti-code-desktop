package com.denticode.desktop.ui.admin;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.ui.shell.NavItem;
import com.denticode.desktop.ui.shell.PortalShell;
import javafx.scene.Node;

import java.util.List;

public final class AdminPortal {

    private final PortalShell shell;

    public AdminPortal(AppContext app) {
        List<NavItem> items = List.of(
                new NavItem("dashboard",    app.i18n().binding("nav.dashboard"),    () -> new DashboardView(app).getRoot()),
                new NavItem("patients",     app.i18n().binding("nav.patients"),     () -> new com.denticode.desktop.ui.common.PatientsView(app).getRoot()),
                new NavItem("doctors",      app.i18n().binding("nav.doctors"),      () -> new com.denticode.desktop.ui.common.DoctorsView(app).getRoot()),
                new NavItem("appointments", app.i18n().binding("nav.appointments"), () -> new com.denticode.desktop.ui.common.AppointmentsView(app, null).getRoot()),
                new NavItem("inventory",    app.i18n().binding("nav.inventory"),    () -> new com.denticode.desktop.ui.common.InventoryView(app).getRoot()),
                new NavItem("settings",     app.i18n().binding("nav.settings"),     () -> new com.denticode.desktop.ui.common.SettingsView(app).getRoot()),
                new NavItem("profile",      app.i18n().binding("nav.profile"),      () -> new com.denticode.desktop.ui.common.ProfileView(app).getRoot())
        );

        this.shell = new PortalShell(app, app.i18n().binding("portal.admin"), items);
        shell.replaceContent(new DashboardView(app).getRoot());
    }

    public Node getRoot() {
        return shell.getRoot();
    }
}
