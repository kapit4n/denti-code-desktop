package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.AppContext;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Lets the user pick the display language. Equivalent to the Next.js
 * {@code /admin/settings} (org default locale) and the in-place
 * language switcher used everywhere else.
 */
public final class SettingsView {

    private final VBox root = new VBox();

    public SettingsView(AppContext app) {
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("settings.title"));
        title.getStyleClass().add("page-title");

        Label localeLbl = new Label();
        localeLbl.textProperty().bind(app.i18n().binding("settings.locale"));

        HBox row = new HBox(10, localeLbl, new LanguageSwitcher(app.i18n()).getNode());
        row.setStyle("-fx-padding: 12 0 0 0;");

        root.getChildren().addAll(title, row);
    }

    public Node getRoot() { return root; }
}
