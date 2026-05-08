package com.denticode.desktop.ui;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Role;
import com.denticode.desktop.ui.admin.AdminPortal;
import com.denticode.desktop.ui.doctor.DoctorPortal;
import com.denticode.desktop.ui.login.LoginView;
import com.denticode.desktop.ui.patient.PatientPortal;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Owns top-level navigation: shows the login screen, then the role-appropriate portal.
 *
 * Mirrors the Next.js role redirects:
 *   ADMIN  \u2192 /admin/dashboard
 *   DOCTOR \u2192 /doctor/dashboard
 *   PATIENT \u2192 /patient/dashboard
 */
public final class Router {

    private final Stage stage;
    private final AppContext app;
    private Scene scene;

    public Router(Stage stage, AppContext app) {
        this.stage = stage;
        this.app = app;
        app.session().userProperty().addListener((obs, was, now) -> {
            if (now == null) showLogin();
            else routeByRole();
        });
    }

    public void start() {
        showLogin();
        stage.setTitle(app.config().appName());
        stage.show();
    }

    private void showLogin() {
        replace(new LoginView(app, this::routeByRole).getRoot());
    }

    private void routeByRole() {
        Role role = app.session().primaryRole();
        if (role == null) {
            showLogin();
            return;
        }
        switch (role) {
            case ADMIN -> replace(new AdminPortal(app).getRoot());
            case DOCTOR -> replace(new DoctorPortal(app).getRoot());
            case PATIENT -> replace(new PatientPortal(app).getRoot());
        }
    }

    private void replace(Node root) {
        Parent parentRoot = asParent(root);
        if (scene == null) {
            scene = new Scene(parentRoot, 1200, 780);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(parentRoot);
        }
    }

    private Parent asParent(Node root) {
        if (root instanceof Parent parent) {
            return parent;
        }
        throw new IllegalArgumentException("Root node must be a Parent: " + root.getClass().getName());
    }
}
