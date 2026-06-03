package com.denticode.desktop.ui.shell;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.kordamp.ikonli.Ikon;

import java.util.function.Supplier;

/**
 * Sidebar entry with optional Ikonli icon.
 */
public record NavItem(String id, ObservableValue<String> label, Supplier<Node> viewFactory, Ikon icon) {

    public NavItem(String id, ObservableValue<String> label, Supplier<Node> viewFactory) {
        this(id, label, viewFactory, null);
    }
}
