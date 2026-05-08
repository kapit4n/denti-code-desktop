package com.denticode.desktop.ui.shell;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.ui.common.LanguageSwitcher;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sidebar + header + content area used by all three role portals.
 *
 * Maps roughly to {@code AdminPortalShell.tsx}, {@code DoctorPortalShell.tsx}
 * and {@code PatientPortalShell.tsx} in the Next.js project.
 */
public final class PortalShell {

    private final AppContext app;
    private final BorderPane root;
    private final StackPane content;
    private final VBox sidebar;
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private String activeId;

    public PortalShell(AppContext app, ObservableValue<String> portalTitle, List<NavItem> items) {
        this.app = app;
        this.root = new BorderPane();
        this.content = new StackPane();
        this.sidebar = new VBox();
        this.sidebar.getStyleClass().add("sidebar");

        buildSidebar(portalTitle, items);
        root.setLeft(sidebar);
        root.setTop(buildHeader());
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: -color-bg-default; -fx-background-color: -color-bg-default;");
        root.setCenter(scroll);

        if (!items.isEmpty()) navigate(items.get(0).id());
    }

    public Node getRoot() {
        return root;
    }

    public void navigate(String id) {
        for (Map.Entry<String, Button> e : navButtons.entrySet()) {
            if (e.getKey().equals(id)) {
                e.getValue().getStyleClass().add("active");
            } else {
                e.getValue().getStyleClass().remove("active");
            }
        }
        activeId = id;
    }

    private void buildSidebar(ObservableValue<String> portalTitle, List<NavItem> items) {
        Label brand = new Label("Denti-Code");
        brand.getStyleClass().add("brand-title");
        Label sub = new Label();
        sub.textProperty().bind(portalTitle);
        sub.getStyleClass().add("brand-subtitle");

        VBox header = new VBox(2, brand, sub);
        header.setStyle("-fx-padding: 8 12 16 12;");
        sidebar.getChildren().add(header);

        for (NavItem item : items) {
            Button b = new Button();
            b.textProperty().bind(item.label());
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER_LEFT);
            b.getStyleClass().add("nav-item");
            b.setOnAction(e -> {
                Node view = item.viewFactory().get();
                content.getChildren().setAll(view);
                navigate(item.id());
            });
            navButtons.put(item.id(), b);
            sidebar.getChildren().add(b);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logout = new Button();
        logout.textProperty().bind(app.i18n().binding("logout.label"));
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.getStyleClass().add("nav-item");
        logout.setOnAction(e -> app.session().clear());
        sidebar.getChildren().add(logout);
    }

    private HBox buildHeader() {
        Label userLabel = new Label();
        userLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
            var u = app.session().getUser();
            return u == null ? "" : (u.getDisplayName() != null ? u.getDisplayName() : u.getEmail());
        }, app.session().userProperty()));

        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        HBox header = new HBox(12);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(grow, new LanguageSwitcher(app.i18n()).getNode(), userLabel);
        return header;
    }

    /**
     * Helper for views that want to drive the central content area without
     * touching the public navigate path (e.g. drilling into appointment details).
     */
    public void replaceContent(Node node) {
        content.getChildren().setAll(node);
    }
}
