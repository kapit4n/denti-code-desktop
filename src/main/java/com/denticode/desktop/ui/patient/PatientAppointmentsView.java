package com.denticode.desktop.ui.patient;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.AppointmentStatus;
import com.denticode.desktop.domain.model.Patient;
import com.denticode.desktop.ui.common.Formatters;
import com.denticode.desktop.ui.common.Notifier;
import com.denticode.desktop.ui.common.StatusTag;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Patient self-service visits list: confirm / reschedule / cancel.
 * Equivalent to {@code /patient/appointments}.
 */
public final class PatientAppointmentsView {

    private final AppContext app;
    private final Patient me;
    private final VBox root = new VBox();
    private final VBox upcomingList = new VBox(8);
    private final VBox pastList = new VBox(8);

    public PatientAppointmentsView(AppContext app, Patient me) {
        this.app = app;
        this.me = me;
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("appointments.title"));
        title.getStyleClass().add("page-title");

        Label upcomingTitle = new Label("\u2022 Upcoming");
        upcomingTitle.getStyleClass().add("section-title");
        Label pastTitle = new Label("\u2022 Past");
        pastTitle.getStyleClass().add("section-title");

        root.getChildren().addAll(title, upcomingTitle, upcomingList, pastTitle, pastList);
        reload();
        app.eventBus().<Object>subscribe(EventBus.APPOINTMENT_CHANGED,
                ignored -> Platform.runLater(this::reload));
    }

    public Node getRoot() { return root; }

    private void reload() {
        upcomingList.getChildren().clear();
        pastList.getChildren().clear();
        List<Appointment> all = app.appointmentService().byPatient(me.getId());
        LocalDateTime now = LocalDateTime.now();

        List<Appointment> upcoming = all.stream()
                .filter(a -> a.getScheduledAt().isAfter(now) && a.getStatus() != AppointmentStatus.CANCELLED)
                .sorted((x, y) -> x.getScheduledAt().compareTo(y.getScheduledAt()))
                .toList();
        List<Appointment> past = all.stream()
                .filter(a -> a.getScheduledAt().isBefore(now) || a.getStatus().isTerminal())
                .sorted((x, y) -> y.getScheduledAt().compareTo(x.getScheduledAt()))
                .toList();

        if (upcoming.isEmpty()) upcomingList.getChildren().add(new Label(app.i18n().t("common.empty")));
        else upcoming.forEach(a -> upcomingList.getChildren().add(renderRow(a, true)));
        if (past.isEmpty()) pastList.getChildren().add(new Label(app.i18n().t("common.empty")));
        else past.forEach(a -> pastList.getChildren().add(renderRow(a, false)));
    }

    private Node renderRow(Appointment a, boolean upcoming) {
        Label when = new Label(Formatters.dateTime(a.getScheduledAt(), app.i18n().getLocale()));
        when.setStyle("-fx-font-weight: 600;");
        Label doctor = new Label(a.getPrimaryDoctor().getFullName());
        Label purpose = new Label(a.getPurpose() == null ? "" : a.getPurpose());
        purpose.setStyle("-fx-text-fill: -color-fg-muted;");
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        HBox actions = new HBox(6);
        if (upcoming) {
            if (AppointmentStatus.patientReschedulable().contains(a.getStatus())) {
                if (a.getStatus() != AppointmentStatus.CONFIRMED) {
                    Button accept = new Button();
                    accept.textProperty().bind(app.i18n().binding("appointments.action.confirm"));
                    accept.setOnAction(e -> {
                        try { app.appointmentService().patientConfirm(a.getId()); }
                        catch (RuntimeException ex) { Notifier.error("", ex.getMessage()); }
                    });
                    actions.getChildren().add(accept);
                }
                Button reschedule = new Button();
                reschedule.textProperty().bind(app.i18n().binding("appointments.action.reschedule"));
                reschedule.setOnAction(e -> openReschedule(a));
                actions.getChildren().add(reschedule);
            }
            if (!a.getStatus().isTerminal()) {
                Button cancel = new Button();
                cancel.textProperty().bind(app.i18n().binding("appointments.action.cancel"));
                cancel.setOnAction(e -> {
                    if (Notifier.confirm("", app.i18n().t("appointments.action.cancel"))) {
                        try { app.appointmentService().patientCancel(a.getId()); }
                        catch (RuntimeException ex) { Notifier.error("", ex.getMessage()); }
                    }
                });
                actions.getChildren().add(cancel);
            }
        }

        HBox row = new HBox(12, when, doctor, purpose, grow, StatusTag.of(a.getStatus(), app.i18n()), actions);
        row.getStyleClass().add("kpi-card");
        return row;
    }

    private void openReschedule(Appointment a) {
        Dialog<LocalDateTime> d = new Dialog<>();
        d.setTitle(app.i18n().t("appointments.reschedule.title"));

        DatePicker date = new DatePicker(a.getScheduledAt().toLocalDate().isAfter(LocalDate.now())
                ? a.getScheduledAt().toLocalDate() : LocalDate.now().plusDays(1));
        TextField time = new TextField(a.getScheduledAt().toLocalTime().toString().substring(0, 5));

        VBox box = new VBox(8);
        Label l = new Label();
        l.textProperty().bind(app.i18n().binding("appointments.reschedule.newTime"));
        box.getChildren().addAll(l, date, time);
        d.getDialogPane().setContent(box);

        ButtonType save = new ButtonType(app.i18n().t("common.save"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(app.i18n().t("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        d.getDialogPane().getButtonTypes().setAll(save, cancel);

        d.setResultConverter(b -> {
            if (b != save) return null;
            try {
                LocalTime t = LocalTime.parse(time.getText().trim());
                LocalDateTime newWhen = LocalDateTime.of(date.getValue(), t);
                app.appointmentService().reschedule(a.getId(), newWhen);
                return newWhen;
            } catch (RuntimeException ex) {
                Notifier.error(app.i18n().t("appointments.reschedule.title"), ex.getMessage());
                return null;
            }
        });
        d.showAndWait();
    }
}
