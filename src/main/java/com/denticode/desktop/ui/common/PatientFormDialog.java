package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Patient;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;

/**
 * Create or edit a patient. Mirrors {@code RegisterPatientForm} / {@code EditPatientForm}.
 */
public final class PatientFormDialog extends Dialog<Patient> {

    public PatientFormDialog(AppContext app, Patient existing) {
        boolean editing = existing != null;
        setTitle(app.i18n().t(editing ? "patients.edit" : "patients.new"));
        getDialogPane().setPrefWidth(560);

        TextField first = new TextField();
        TextField last = new TextField();
        DatePicker dob = new DatePicker();
        ChoiceBox<String> gender = new ChoiceBox<>();
        gender.getItems().setAll("", "Female", "Male", "Other");
        TextField address = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        TextArea history = new TextArea();
        history.setPrefRowCount(3);

        if (editing) {
            first.setText(existing.getFirstName());
            last.setText(existing.getLastName());
            dob.setValue(existing.getDateOfBirth());
            gender.setValue(existing.getGender() == null ? "" : existing.getGender());
            address.setText(existing.getAddress());
            phone.setText(existing.getContactPhone());
            email.setText(existing.getEmail());
            history.setText(existing.getMedicalHistorySummary());
        } else {
            dob.setValue(LocalDate.now().minusYears(25));
            gender.setValue("");
        }

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        int row = 0;
        for (var triple : List.<Object[]>of(
                new Object[]{"patients.firstName", first},
                new Object[]{"patients.lastName", last},
                new Object[]{"patients.dateOfBirth", dob},
                new Object[]{"patients.gender", gender},
                new Object[]{"patients.contactPhone", phone},
                new Object[]{"patients.email", email},
                new Object[]{"patients.address", address},
                new Object[]{"patients.medicalHistory", history}
        )) {
            Label l = new Label();
            l.textProperty().bind(app.i18n().binding((String) triple[0]));
            g.add(l, 0, row);
            javafx.scene.Node ctl = (javafx.scene.Node) triple[1];
            g.add(ctl, 1, row);
            javafx.scene.layout.GridPane.setHgrow(ctl, javafx.scene.layout.Priority.ALWAYS);
            row++;
        }
        getDialogPane().setContent(g);

        ButtonType save = new ButtonType(app.i18n().t("common.save"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(app.i18n().t("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().setAll(save, cancel);

        setResultConverter(button -> {
            if (button != save) return null;
            try {
                Patient draft = editing ? existing : new Patient();
                draft.setFirstName(trim(first.getText()));
                draft.setLastName(trim(last.getText()));
                draft.setDateOfBirth(dob.getValue());
                draft.setGender(blankToNull(gender.getValue()));
                draft.setAddress(blankToNull(address.getText()));
                draft.setContactPhone(trim(phone.getText()));
                draft.setEmail(blankToNull(email.getText()));
                draft.setMedicalHistorySummary(blankToNull(history.getText()));
                return editing ? app.patientService().update(draft) : app.patientService().create(draft);
            } catch (RuntimeException e) {
                Notifier.error(app.i18n().t(editing ? "patients.edit" : "patients.new"), e.getMessage());
                return null;
            }
        });
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
