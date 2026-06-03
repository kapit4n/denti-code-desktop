package com.denticode.desktop.ui.shell;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.ui.common.LanguageSwitcher;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sidebar + top navbar + content. Loads the first nav item on open; supports
 * {@link #navigateToView(String)} for in-app navigation (e.g. dashboard quick actions).
 */
public final class PortalShell {

    private static final int ICON_NAV = 18;
    private static final int ICON_HEADER = 16;

    private final AppContext app;
    private final BorderPane root;
    private final StackPane content;
    private final VBox sidebar;
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, NavItem> itemsById = new LinkedHashMap<>();
    private final TextField globalSearch = new TextField();
    private final FontIcon themeIcon = icon(FontAwesomeSolid.MOON, ICON_HEADER);
    private String activeId;
    private boolean darkTheme;
    private final String firstNavId;

    public PortalShell(AppContext app, ObservableValue<String> portalTitle, List<NavItem> items) {
        this.app = app;
        this.firstNavId = items.isEmpty() ? null : items.get(0).id();
        for (NavItem item : items) {
            itemsById.put(item.id(), item);
        }
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
        scroll.getStyleClass().add("portal-scroll");
        root.setCenter(scroll);

        root.sceneProperty().addListener((obs, oldSc, scene) -> {
            if (scene != null) {
                scene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN),
                        () -> globalSearch.requestFocus());
            }
        });
    }

    private static FontIcon icon(Ikon ikon, int size) {
        FontIcon fi = new FontIcon(ikon);
        fi.setIconSize(size);
        return fi;
    }

    /** Call once after the shell reference is visible to nav factories (e.g. doctor dashboard). */
    public void showDefaultView() {
        if (firstNavId != null) {
            selectNav(firstNavId, false);
        }
    }

    public Node getRoot() {
        return root;
    }

    /** Updates sidebar active styling only. */
    public void navigate(String id) {
        for (Map.Entry<String, Button> e : navButtons.entrySet()) {
            if (e.getKey().equals(id)) {
                if (!e.getValue().getStyleClass().contains("active")) {
                    e.getValue().getStyleClass().add("active");
                }
            } else {
                e.getValue().getStyleClass().remove("active");
            }
        }
        activeId = id;
    }

    /** Load a main nav screen by id (sidebar + content). */
    public void navigateToView(String id) {
        if (!itemsById.containsKey(id)) {
            return;
        }
        selectNav(id, true);
    }

    private void selectNav(String id, boolean animate) {
        NavItem item = itemsById.get(id);
        if (item == null) {
            return;
        }
        Node view = item.viewFactory().get();
        if (animate) {
            FadeTransition out = new FadeTransition(Duration.millis(100), content);
            out.setFromValue(1);
            out.setToValue(0);
            out.setOnFinished(ev -> {
                content.getChildren().setAll(view);
                navigate(id);
                FadeTransition in = new FadeTransition(Duration.millis(140), content);
                in.setFromValue(0);
                in.setToValue(1);
                in.play();
            });
            out.play();
        } else {
            content.getChildren().setAll(view);
            navigate(id);
        }
    }

    /**
     * Replace central content without changing sidebar selection (e.g. appointment detail).
     */
    public void replaceContent(Node node) {
        content.getChildren().setAll(node);
    }

    private void buildSidebar(ObservableValue<String> portalTitle, List<NavItem> items) {
        Label brand = new Label("Denti-Code");
        brand.getStyleClass().add("brand-title");
        Label sub = new Label();
        sub.textProperty().bind(portalTitle);
        sub.getStyleClass().add("brand-subtitle");

        VBox header = new VBox(4, brand, sub);
        header.getStyleClass().add("sidebar-brand-block");
        sidebar.getChildren().add(header);

        for (NavItem item : items) {
            Button b = new Button();
            b.textProperty().bind(item.label());
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER_LEFT);
            b.getStyleClass().add("nav-item");
            if (item.icon() != null) {
                FontIcon g = icon(item.icon(), ICON_NAV);
                g.getStyleClass().add("nav-item-icon");
                b.setGraphic(g);
            }
            b.setOnAction(e -> selectNav(item.id(), true));
            navButtons.put(item.id(), b);
            sidebar.getChildren().add(b);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logout = new Button();
        logout.textProperty().bind(app.i18n().binding("logout.label"));
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.getStyleClass().addAll("nav-item", "nav-item-logout");
        FontIcon outIcon = icon(FontAwesomeSolid.SIGN_OUT_ALT, ICON_NAV);
        outIcon.getStyleClass().add("nav-item-icon");
        logout.setGraphic(outIcon);
        logout.setOnAction(e -> app.session().clear());
        sidebar.getChildren().add(logout);
    }

    private HBox buildHeader() {
        globalSearch.promptTextProperty().bind(app.i18n().binding("dashboard.search.placeholder"));
        globalSearch.getStyleClass().add("navbar-search");
        globalSearch.setMaxWidth(440);
        globalSearch.setMinWidth(220);
        HBox.setHgrow(globalSearch, Priority.SOMETIMES);

        Label searchHint = new Label();
        searchHint.textProperty().bind(app.i18n().binding("dashboard.search.hint"));
        searchHint.getStyleClass().add("navbar-search-hint");

        VBox searchCol = new VBox(4, globalSearch, searchHint);

        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        Button notifications = iconButton(FontAwesomeSolid.BELL, "nav.notifications.tooltip");
        Button themeToggle = new Button(null, themeIcon);
        themeToggle.getStyleClass().add("icon-button");
        themeToggle.setOnAction(e -> toggleTheme());
        Tooltip themeTip = new Tooltip();
        themeTip.textProperty().bind(app.i18n().binding("theme.toggle"));
        Tooltip.install(themeToggle, themeTip);

        Node lang = new LanguageSwitcher(app.i18n()).getNode();
        lang.getStyleClass().add("navbar-lang");

        MenuButton profile = buildProfileMenu();

        HBox right = new HBox(10, notifications, themeToggle, lang, profile);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.getStyleClass().add("navbar-right");

        HBox header = new HBox(20, searchCol, grow, right);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMinHeight(80);
        header.setPrefHeight(80);
        return header;
    }

    private Button iconButton(Ikon ikon, String tooltipKey) {
        FontIcon ic = icon(ikon, ICON_HEADER);
        Button b = new Button(null, ic);
        b.getStyleClass().add("icon-button");
        Tooltip t = new Tooltip();
        t.textProperty().bind(app.i18n().binding(tooltipKey));
        Tooltip.install(b, t);
        b.setOnAction(e -> { /* reserved for notifications center */ });
        return b;
    }

    private MenuButton buildProfileMenu() {
        MenuButton mb = new MenuButton();
        mb.getStyleClass().add("profile-menu");
        Label name = new Label();
        name.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
            var u = app.session().getUser();
            return u == null ? "" : (u.getDisplayName() != null && !u.getDisplayName().isBlank()
                    ? u.getDisplayName() : u.getEmail());
        }, app.session().userProperty()));
        name.getStyleClass().add("profile-menu-name");
        FontIcon chev = icon(FontAwesomeSolid.CHEVRON_DOWN, 12);
        HBox graphic = new HBox(8, name, chev);
        graphic.setAlignment(Pos.CENTER_LEFT);
        mb.setGraphic(graphic);

        MenuItem openProfile = new MenuItem();
        openProfile.textProperty().bind(app.i18n().binding("nav.profile"));
        openProfile.setGraphic(icon(FontAwesomeSolid.USER_CIRCLE, 14));
        openProfile.setOnAction(e -> navigateToView(profileNavId()));

        MenuItem logout = new MenuItem();
        logout.textProperty().bind(app.i18n().binding("logout.label"));
        logout.setGraphic(icon(FontAwesomeSolid.SIGN_OUT_ALT, 14));
        logout.setOnAction(e -> app.session().clear());

        mb.getItems().addAll(openProfile, new SeparatorMenuItem(), logout);
        return mb;
    }

    private String profileNavId() {
        if (itemsById.containsKey("profile")) {
            return "profile";
        }
        if (itemsById.containsKey("you")) {
            return "you";
        }
        return "profile";
    }

    private void toggleTheme() {
        darkTheme = !darkTheme;
        Application.setUserAgentStylesheet(
                darkTheme ? new PrimerDark().getUserAgentStylesheet() : new PrimerLight().getUserAgentStylesheet());
        themeIcon.setIconCode(darkTheme ? FontAwesomeSolid.SUN : FontAwesomeSolid.MOON);
    }
}
