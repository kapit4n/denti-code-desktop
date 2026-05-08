package com.denticode.desktop.ui.patient;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.Patient;
import com.denticode.desktop.ui.common.Formatters;
import com.denticode.desktop.ui.common.StatusTag;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Patient home: greeting, demographics summary, and upcoming visits.
 * Equivalent to {@code /patient/dashboard}.
 */
public final class PatientDashboardView {

    private final AppContext app;
    private final Patient me;
    private final VBox root = new VBox();

    public PatientDashboardView(AppContext app, Patient me) {
        this.app = app;
        this.me = me;
        root.getStyleClass().add("page");

        Label hello = new Label(app.i18n().t("app.welcome") + ", " + me.getFirstName());
        hello.getStyleClass().add("page-title");

        GridPane info = new GridPane();
        info.setHgap(20);
        info.setVgap(6);
        addRow(info, 0, app.i18n().t("patients.col.name"), me.getFullName());
        addRow(info, 1, app.i18n().t("patients.col.email"),
                me.getEmail() == null ? app.i18n().t("patients.noEmail") : me.getEmail());
        addRow(info, 2, app.i18n().t("patients.col.phone"), me.getContactPhone());
        addRow(info, 3, app.i18n().t("patients.col.dob"), Formatters.date(me.getDateOfBirth()));
        if (me.getAddress() != null) addRow(info, 4, app.i18n().t("patients.address"), me.getAddress());

        Label upcomingTitle = new Label();
        upcomingTitle.textProperty().bind(app.i18n().binding("nav.visits"));
        upcomingTitle.getStyleClass().add("section-title");

        VBox upcoming = new VBox(8);
        List<Appointment> visits = app.appointmentService().byPatient(me.getId()).stream()
                .filter(a -> a.getScheduledAt().isAfter(LocalDateTime.now()))
                .sorted((x, y) -> x.getScheduledAt().compareTo(y.getScheduledAt()))
                .limit(4)
                .toList();
        if (visits.isEmpty()) {
            upcoming.getChildren().add(new Label(app.i18n().t("common.empty")));
        } else {
            visits.forEach(v -> upcoming.getChildren().add(renderRow(v)));
        }

        root.getChildren().addAll(hello, info, upcomingTitle, upcoming);
    }

    public Node getRoot() { return root; }

    private Node renderRow(Appointment a) {
        Label when = new Label(Formatters.dateTime(a.getScheduledAt(), app.i18n().getLocale()));
        when.setStyle("-fx-font-weight: 600;");
        Label doctor = new Label(a.getPrimaryDoctor().getFullName());
        Label purpose = new Label(a.getPurpose() == null ? "" : a.getPurpose());
        purpose.setStyle("-fx-text-fill: -color-fg-muted;");
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox row = new HBox(12, when, doctor, purpose, grow, StatusTag.of(a.getStatus(), app.i18n()));
        row.getStyleClass().add("kpi-card");
        return row;
    }

    private static void addRow(GridPane g, int row, String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-font-weight: 600; -fx-text-fill: -color-fg-muted;");
        Label v = new Label(value);
        g.add(l, 0, row);
        g.add(v, 1, row);
    }
}
