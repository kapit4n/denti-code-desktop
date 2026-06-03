package com.denticode.desktop.ui.components;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.AppointmentStatus;
import com.denticode.desktop.ui.common.Formatters;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.geometry.Side;
import javafx.scene.control.MenuItem;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import java.util.function.Consumer;

public final class VisitCardController {

    @FXML
    private Label avatar;
    @FXML
    private Label patientName;
    @FXML
    private Label visitType;
    @FXML
    private Label visitTime;
    @FXML
    private Label statusBadge;
    @FXML
    private Button menuBtn;

    private AppContext app;
    private Appointment appointment;
    private Consumer<Appointment> onOpen;

    public void init(AppContext app, Appointment appointment, Consumer<Appointment> onOpen) {
        this.app = app;
        this.appointment = appointment;
        this.onOpen = onOpen;
        if (menuBtn != null) {
            FontIcon ic = new FontIcon(FontAwesomeSolid.ELLIPSIS_V);
            ic.setIconSize(14);
            menuBtn.setGraphic(ic);
            menuBtn.setText("");
        }
        refresh();
    }

    private void refresh() {
        if (appointment == null || app == null) {
            return;
        }
        patientName.setText(appointment.getPatient().getFullName());
        visitType.setText(appointment.getPurpose() == null || appointment.getPurpose().isBlank()
                ? app.i18n().t("dashboard.visit.typeDefault")
                : appointment.getPurpose());
        visitTime.setText(Formatters.dateTime(appointment.getScheduledAt(), app.i18n().getLocale()));
        avatar.setText(initials(appointment.getPatient().getFullName()));
        styleStatus(appointment.getStatus());

        ContextMenu menu = new ContextMenu();
        MenuItem open = new MenuItem(app.i18n().t("dashboard.visit.openDetail"));
        open.setOnAction(e -> {
            if (onOpen != null) {
                onOpen.accept(appointment);
            }
        });
        menu.getItems().add(open);
        menuBtn.setOnAction(e -> menu.show(menuBtn, Side.BOTTOM, 0, 0));
    }

    private void styleStatus(AppointmentStatus s) {
        statusBadge.getStyleClass().removeIf(c -> c.startsWith("visit-badge-"));
        String key = switch (s) {
            case CONFIRMED, IN_PROGRESS -> "confirmed";
            case COMPLETED -> "completed";
            case SCHEDULED, RESCHEDULED -> "pending";
            default -> "neutral";
        };
        statusBadge.getStyleClass().add("visit-badge-" + key);
        statusBadge.setText(app.i18n().t("status." + s.name()));
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
