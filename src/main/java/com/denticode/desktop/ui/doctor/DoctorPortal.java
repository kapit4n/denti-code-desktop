package com.denticode.desktop.ui.doctor;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.ui.common.AppointmentsView;
import com.denticode.desktop.ui.common.PatientsView;
import com.denticode.desktop.ui.common.ProfileView;
import com.denticode.desktop.ui.common.SettingsView;
import com.denticode.desktop.ui.shell.NavItem;
import com.denticode.desktop.ui.shell.PortalShell;
import javafx.scene.Node;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import java.util.List;
import java.util.Optional;

public final class DoctorPortal {

    private final PortalShell shell;

    public DoctorPortal(AppContext app) {
        Doctor self = currentDoctor(app).orElse(null);

        AppointmentsView appointments = new AppointmentsView(
                app, self == null ? null : a -> a.getPrimaryDoctor().getId().equals(self.getId()));
        final PortalShell[] shellRef = new PortalShell[1];
        appointments.setOnOpen(a -> shellRef[0].replaceContent(
                new AppointmentDetailView(app, a, self, () -> appointments.reload()).getRoot()));

        java.util.function.Consumer<com.denticode.desktop.domain.model.Appointment> openDetail =
                a -> shellRef[0].replaceContent(
                        new AppointmentDetailView(app, a, self, () -> appointments.reload()).getRoot());

        List<NavItem> items = List.of(
                new NavItem("home", app.i18n().binding("nav.home"),
                        () -> new DoctorDashboardView(app, self, shellRef[0], openDetail).getRoot(),
                        FontAwesomeSolid.HOME),
                new NavItem("appointments", app.i18n().binding("nav.visits"),
                        appointments::getRoot,
                        FontAwesomeSolid.CALENDAR_DAY),
                new NavItem("patients", app.i18n().binding("nav.patients"),
                        () -> new PatientsView(app).getRoot(),
                        FontAwesomeSolid.USERS),
                new NavItem("profile", app.i18n().binding("nav.profile"),
                        () -> new ProfileView(app).getRoot(),
                        FontAwesomeSolid.USER_CIRCLE),
                new NavItem("settings", app.i18n().binding("nav.settings"),
                        () -> new SettingsView(app).getRoot(),
                        FontAwesomeSolid.COG)
        );
        this.shell = new PortalShell(app, app.i18n().binding("portal.doctor"), items);
        shellRef[0] = this.shell;
        this.shell.showDefaultView();
    }

    public Node getRoot() {
        return shell.getRoot();
    }

    private static Optional<Doctor> currentDoctor(AppContext app) {
        var u = app.session().getUser();
        if (u == null) {
            return Optional.empty();
        }
        return app.doctorService().findByUserId(u.getId());
    }
}
