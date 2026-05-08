package com.denticode.desktop.ui.doctor;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.ui.common.Formatters;
import com.denticode.desktop.ui.common.StatusTag;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Doctor home overview. Shows upcoming visits + KPI cards similar to
 * {@code DoctorHomeOverview} + {@code DoctorDashboardStats}.
 */
public final class DoctorDashboardView {

    private final AppContext app;
    private final Doctor self;
    private final VBox root = new VBox();
    private final Label upcomingValue = new Label("0");
    private final Label completedValue = new Label("0");
    private final Label patientsValue = new Label("0");
    private final VBox upcomingList = new VBox(8);

    public DoctorDashboardView(AppContext app, Doctor self) {
        this.app = app;
        this.self = self;
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("portal.doctor"));
        title.getStyleClass().add("page-title");

        FlowPane kpis = new FlowPane(16, 16);
        kpis.getChildren().addAll(
                kpi("dashboard.kpi.appointments", upcomingValue),
                kpi("status.COMPLETED", completedValue),
                kpi("dashboard.kpi.patients", patientsValue)
        );

        Label upcoming = new Label();
        upcoming.textProperty().bind(app.i18n().binding("nav.visits"));
        upcoming.getStyleClass().add("section-title");

        root.getChildren().addAll(title, kpis, upcoming, upcomingList);
        refresh();
        app.eventBus().<Object>subscribe(EventBus.APPOINTMENT_CHANGED,
                ignored -> Platform.runLater(this::refresh));
    }

    public Node getRoot() {
        return root;
    }

    private VBox kpi(String key, Label value) {
        VBox card = new VBox();
        card.getStyleClass().add("kpi-card");
        Label label = new Label();
        label.textProperty().bind(app.i18n().binding(key));
        label.getStyleClass().add("kpi-label");
        value.getStyleClass().add("kpi-value");
        card.getChildren().addAll(label, value);
        return card;
    }

    private void refresh() {
        upcomingList.getChildren().clear();
        if (self == null) {
            upcomingList.getChildren().add(new Label(app.i18n().t("common.empty")));
            return;
        }
        List<Appointment> mine = app.appointmentService().byDoctor(self.getId());
        long upcoming = mine.stream().filter(a -> a.getScheduledAt().isAfter(LocalDateTime.now())).count();
        long completed = mine.stream().filter(a -> a.getStatus() == com.denticode.desktop.domain.model.AppointmentStatus.COMPLETED).count();
        long uniquePatients = mine.stream().map(a -> a.getPatient().getId()).distinct().count();
        upcomingValue.setText(Long.toString(upcoming));
        completedValue.setText(Long.toString(completed));
        patientsValue.setText(Long.toString(uniquePatients));

        mine.stream()
                .filter(a -> a.getScheduledAt().isAfter(LocalDateTime.now()))
                .sorted((x, y) -> x.getScheduledAt().compareTo(y.getScheduledAt()))
                .limit(8)
                .forEach(a -> upcomingList.getChildren().add(renderRow(a)));

        if (upcomingList.getChildren().isEmpty()) {
            upcomingList.getChildren().add(new Label(app.i18n().t("common.empty")));
        }
    }

    private Node renderRow(Appointment a) {
        Label when = new Label(Formatters.dateTime(a.getScheduledAt(), app.i18n().getLocale()));
        when.setStyle("-fx-font-weight: 600;");
        Label patient = new Label(a.getPatient().getFullName());
        Label purpose = new Label(a.getPurpose() == null ? "" : a.getPurpose());
        purpose.setStyle("-fx-text-fill: -color-fg-muted;");
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox row = new HBox(12, when, patient, purpose, grow, StatusTag.of(a.getStatus(), app.i18n()));
        row.getStyleClass().add("kpi-card");
        return row;
    }
}
