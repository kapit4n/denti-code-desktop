package com.denticode.desktop.ui.login;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.User;
import com.denticode.desktop.ui.common.LanguageSwitcher;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Login form with role-based redirect, equivalent to {@code (auth)/login/page.tsx}.
 */
public final class LoginView {

    private final AppContext app;
    private final Runnable onSuccess;
    private final StackPane root;

    public LoginView(AppContext app, Runnable onSuccess) {
        this.app = app;
        this.onSuccess = onSuccess;
        this.root = build();
    }

    public Parent getRoot() {
        return root;
    }

    private StackPane build() {
        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("login.title"));
        title.getStyleClass().add("page-title");

        Label welcome = new Label();
        welcome.textProperty().bind(app.i18n().binding("app.welcome"));
        welcome.getStyleClass().add("brand-subtitle");

        Label emailLbl = new Label();
        emailLbl.textProperty().bind(app.i18n().binding("login.email"));
        TextField email = new TextField();
        email.setPromptText("you@example.com");
        email.setText("admin@denti-code.com");

        Label passwordLbl = new Label();
        passwordLbl.textProperty().bind(app.i18n().binding("login.password"));
        PasswordField password = new PasswordField();
        password.setText("Password123!");

        Label error = new Label();
        error.getStyleClass().add("field-error");

        Button submit = new Button();
        submit.textProperty().bind(app.i18n().binding("login.submit"));
        submit.setDefaultButton(true);
        submit.setMaxWidth(Double.MAX_VALUE);

        Runnable doLogin = () -> {
            error.setText("");
            Optional<User> u = app.authService().authenticate(email.getText(), password.getText());
            if (u.isEmpty()) {
                error.setText(app.i18n().t("errors.invalidCredentials"));
                return;
            }
            User user = u.get();
            if (user.getPreferredLocale() != null && !user.getPreferredLocale().isBlank()) {
                app.i18n().setLocale(java.util.Locale.forLanguageTag(user.getPreferredLocale()));
            }
            app.session().setUser(user);
            if (onSuccess != null) onSuccess.run();
        };
        submit.setOnAction(e -> doLogin.run());
        password.setOnAction(e -> doLogin.run());

        Label hint = new Label();
        hint.textProperty().bind(app.i18n().binding("login.demoHint"));
        hint.getStyleClass().add("brand-subtitle");

        VBox form = new VBox(10, title, welcome, emailLbl, email, passwordLbl, password, submit, error, hint);
        form.setPadding(new Insets(28));
        form.setMaxWidth(380);
        form.setMinWidth(360);
        form.getStyleClass().add("kpi-card");

        HBox topBar = new HBox(new LanguageSwitcher(app.i18n()).getNode());
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(12, 18, 0, 0));

        VBox col = new VBox(0, topBar, new StackPane(form));
        VBox.setVgrow(col.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        ((StackPane) col.getChildren().get(1)).setAlignment(Pos.CENTER);

        StackPane wrapper = new StackPane(col);
        wrapper.setStyle("-fx-background-color: -color-bg-subtle;");
        return wrapper;
    }
}
