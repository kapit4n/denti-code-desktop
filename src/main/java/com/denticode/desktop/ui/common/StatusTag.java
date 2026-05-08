package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.I18n;
import com.denticode.desktop.domain.model.AppointmentStatus;
import javafx.scene.control.Label;

public final class StatusTag {

    private StatusTag() {}

    public static Label of(AppointmentStatus status, I18n i18n) {
        Label tag = new Label();
        tag.textProperty().bind(i18n.binding("status." + status.name()));
        tag.getStyleClass().addAll("tag", "tag-status-" + status.name());
        return tag;
    }
}
