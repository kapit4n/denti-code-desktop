package com.denticode.desktop.ui.admin;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.AppointmentStatus;
import com.denticode.desktop.ui.common.StatusTag;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Admin dashboard \u2014 KPI cards + status breakdown.
 * Equivalent to {@code /admin/dashboard}.
 */
public final class DashboardView {

    private final AppContext app;
    private final VBox root = new VBox();
    private final Label patientsValue = new Label("0");
    private final Label doctorsValue = new Label("0");
    private final Label appointmentsValue = new Label("0");
    private final Label todayValue = new Label("0");
    private final VBox statusList = new VBox(6);

    public DashboardView(AppContext app) {
        this.app = app;
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("dashboard.kpi.appointments"));
        Label header = new Label();
        header.textProperty().bind(app.i18n().binding("portal.admin"));
        header.getStyleClass().add("page-title");

        FlowPane kpis = new FlowPane(16, 16);
        kpis.getChildren().addAll(
                kpi("dashboard.kpi.patients", patientsValue),
                kpi("dashboard.kpi.doctors", doctorsValue),
                kpi("dashboard.kpi.appointments", appointmentsValue),
                kpi("dashboard.kpi.today", todayValue)
        );

        Label breakdown = new Label();
        breakdown.textProperty().bind(app.i18n().binding("dashboard.statusBreakdown"));
        breakdown.getStyleClass().add("section-title");

        root.getChildren().addAll(header, kpis, breakdown, statusList);

        refresh();
        app.eventBus().subscribe(EventBus.PATIENT_CHANGED, x -> Platform.runLater(this::refresh));
        app.eventBus().subscribe(EventBus.APPOINTMENT_CHANGED, x -> Platform.runLater(this::refresh));
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
        patientsValue.setText(Long.toString(app.patientService().count()));
        doctorsValue.setText(Long.toString(app.doctorService().count()));
        appointmentsValue.setText(Long.toString(app.appointmentService().count()));
        todayValue.setText(Long.toString(app.appointmentService().today().size()));

        statusList.getChildren().clear();
        Map<AppointmentStatus, Long> counts = app.appointmentService().countByStatus();
        for (AppointmentStatus s : AppointmentStatus.values()) {
            HBox row = new HBox(10);
            row.getChildren().add(StatusTag.of(s, app.i18n()));
            Region grow = new Region();
            HBox.setHgrow(grow, Priority.ALWAYS);
            Label count = new Label(Long.toString(counts.getOrDefault(s, 0L)));
            count.setStyle("-fx-font-weight: 600;");
            row.getChildren().addAll(grow, count);
            statusList.getChildren().add(row);
        }
    }
}
