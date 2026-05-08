package com.denticode.desktop.ui.shell;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

public record NavItem(String id, ObservableValue<String> label, java.util.function.Supplier<Node> viewFactory) {
}
