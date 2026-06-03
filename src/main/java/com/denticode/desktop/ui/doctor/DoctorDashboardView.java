package com.denticode.desktop.ui.doctor;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.ui.shell.PortalShell;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * Doctor home dashboard (FXML + {@link DoctorDashboardController}).
 */
public final class DoctorDashboardView {

    private final Parent root;

    public DoctorDashboardView(AppContext app, Doctor self, PortalShell portal,
                                 Consumer<Appointment> openAppointmentDetail) {
        try {
            FXMLLoader loader = new FXMLLoader(DoctorDashboardView.class.getResource("/fxml/views/DashboardView.fxml"));
            loader.setController(new DoctorDashboardController(app, self, portal, openAppointmentDetail));
            this.root = loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Node getRoot() {
        return root;
    }
}
