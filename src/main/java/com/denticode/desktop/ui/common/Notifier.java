package com.denticode.desktop.ui.common;

import javafx.scene.control.Alert;

public final class Notifier {

    private Notifier() {}

    public static void info(String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    public static void error(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    public static boolean confirm(String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(header);
        a.setContentText(content);
        return a.showAndWait().filter(b -> b.getButtonData().isDefaultButton()).isPresent();
    }
}
