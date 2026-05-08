package com.denticode.desktop.ui.patient;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Patient;
import com.denticode.desktop.ui.common.ProfileView;
import com.denticode.desktop.ui.shell.NavItem;
import com.denticode.desktop.ui.shell.PortalShell;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Optional;

public final class PatientPortal {

    private final PortalShell shell;

    public PatientPortal(AppContext app) {
        Optional<Patient> me = currentPatient(app);

        List<NavItem> items = List.of(
                new NavItem("home",   app.i18n().binding("nav.home"),
                        () -> me.map(p -> (Node) new PatientDashboardView(app, p).getRoot())
                                .orElseGet(() -> new Label(app.i18n().t("common.empty")))),
                new NavItem("visits", app.i18n().binding("nav.visits"),
                        () -> me.map(p -> (Node) new PatientAppointmentsView(app, p).getRoot())
                                .orElseGet(() -> new Label(app.i18n().t("common.empty")))),
                new NavItem("you",    app.i18n().binding("nav.yourDetails"),
                        () -> new ProfileView(app).getRoot())
        );

        this.shell = new PortalShell(app, app.i18n().binding("portal.patient"), items);
        shell.replaceContent(me.map(p -> (Node) new PatientDashboardView(app, p).getRoot())
                .orElseGet(() -> new Label(app.i18n().t("common.empty"))));
    }

    public Node getRoot() { return shell.getRoot(); }

    private static Optional<Patient> currentPatient(AppContext app) {
        var u = app.session().getUser();
        if (u == null) return Optional.empty();
        return app.patientService().findByUserId(u.getId());
    }
}
