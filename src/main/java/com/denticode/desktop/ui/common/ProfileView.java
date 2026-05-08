package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.User;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

/**
 * Read-only profile card for the current session. Avatar upload and
 * patient/doctor self-service edit fields can layer on top of this later.
 */
public final class ProfileView {

    private final VBox root = new VBox();

    public ProfileView(AppContext app) {
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("profile.title"));
        title.getStyleClass().add("page-title");

        User u = app.session().getUser();
        if (u == null) {
            root.getChildren().setAll(title, new Label(app.i18n().t("common.empty")));
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        addRow(grid, 0, app.i18n().t("profile.id"), String.valueOf(u.getId()));
        addRow(grid, 1, app.i18n().t("profile.email"), u.getEmail());
        addRow(grid, 2, app.i18n().t("profile.displayName"),
                u.getDisplayName() == null ? "" : u.getDisplayName());
        addRow(grid, 3, app.i18n().t("profile.roles"),
                u.getRoles().stream().map(Enum::name).collect(Collectors.joining(", ")));

        root.getChildren().addAll(title, grid);
    }

    private static void addRow(GridPane g, int row, String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-font-weight: 600; -fx-text-fill: -color-fg-muted;");
        Label v = new Label(value);
        g.add(l, 0, row);
        g.add(v, 1, row);
    }

    public Node getRoot() { return root; }
}
