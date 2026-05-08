package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.AppointmentStatus;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.domain.model.Patient;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class AppointmentCreateDialog extends Dialog<Appointment> {

    public AppointmentCreateDialog(AppContext app, Patient defaultPatient, Doctor defaultDoctor) {
        setTitle(app.i18n().t("appointments.new"));
        getDialogPane().setPrefWidth(560);

        ChoiceBox<Patient> patientPicker = new ChoiceBox<>();
        patientPicker.getItems().setAll(app.patientService().search(null));
        patientPicker.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Patient p) { return p == null ? "" : p.getFullName(); }
            @Override public Patient fromString(String s) { return null; }
        });
        if (defaultPatient != null) patientPicker.setValue(defaultPatient);

        ChoiceBox<Doctor> doctorPicker = new ChoiceBox<>();
        doctorPicker.getItems().setAll(app.doctorService().active());
        doctorPicker.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Doctor d) { return d == null ? "" : d.getFullName(); }
            @Override public Doctor fromString(String s) { return null; }
        });
        if (defaultDoctor != null) doctorPicker.setValue(defaultDoctor);
        else if (!doctorPicker.getItems().isEmpty()) doctorPicker.setValue(doctorPicker.getItems().get(0));

        DatePicker date = new DatePicker(LocalDate.now().plusDays(1));
        TextField time = new TextField("09:00");
        Spinner<Integer> duration = new Spinner<>(5, 240, 30, 5);
        duration.setEditable(true);

        TextField purpose = new TextField();
        TextArea notes = new TextArea();
        notes.setPrefRowCount(3);

        ChoiceBox<AppointmentStatus> initial = new ChoiceBox<>();
        initial.getItems().setAll(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        initial.setValue(AppointmentStatus.SCHEDULED);
        initial.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(AppointmentStatus s) { return s == null ? "" : app.i18n().t("status." + s.name()); }
            @Override public AppointmentStatus fromString(String s) { return null; }
        });

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        addRow(g, 0, app, "appointments.field.patient", patientPicker);
        addRow(g, 1, app, "appointments.field.doctor", doctorPicker);
        addRow(g, 2, app, "appointments.field.scheduledAt", date);
        addRow(g, 3, app, "common.actions", time);
        addRow(g, 4, app, "appointments.field.duration", duration);
        addRow(g, 5, app, "appointments.field.purpose", purpose);
        addRow(g, 6, app, "appointments.field.notes", notes);
        addRow(g, 7, app, "appointments.field.initialStatus", initial);
        getDialogPane().setContent(g);

        ButtonType save = new ButtonType(app.i18n().t("common.save"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(app.i18n().t("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().setAll(save, cancel);

        setResultConverter(button -> {
            if (button != save) return null;
            try {
                Patient p = patientPicker.getValue();
                Doctor d = doctorPicker.getValue();
                if (p == null || d == null) {
                    Notifier.error(app.i18n().t("appointments.new"), "Patient and doctor are required");
                    return null;
                }
                LocalTime t = LocalTime.parse(time.getText().trim());
                LocalDateTime when = LocalDateTime.of(date.getValue(), t);
                return app.appointmentService().create(
                        p.getId(), d.getId(), when, duration.getValue(),
                        emptyToNull(purpose.getText()), emptyToNull(notes.getText()),
                        initial.getValue());
            } catch (RuntimeException e) {
                Notifier.error(app.i18n().t("appointments.new"), e.getMessage());
                return null;
            }
        });
    }

    private static void addRow(GridPane g, int row, AppContext app, String key, javafx.scene.Node ctl) {
        Label l = new Label();
        l.textProperty().bind(app.i18n().binding(key));
        g.add(l, 0, row);
        g.add(ctl, 1, row);
        GridPane.setHgrow(ctl, javafx.scene.layout.Priority.ALWAYS);
    }

    private static String emptyToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
