package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.I18n;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class LanguageSwitcher {

    private final ChoiceBox<String> choice;

    public LanguageSwitcher(I18n i18n) {
        this.choice = new ChoiceBox<>();
        Map<String, Locale> options = new LinkedHashMap<>();
        options.put(i18n.t("language.en"), Locale.ENGLISH);
        options.put(i18n.t("language.es"), Locale.forLanguageTag("es"));
        choice.getItems().addAll(options.keySet());

        choice.setValue(matchingLabel(options, i18n.getLocale()));
        choice.valueProperty().addListener((obs, was, now) -> {
            if (now == null) return;
            Locale picked = options.get(now);
            if (picked != null) {
                i18n.setLocale(picked);
            }
        });
    }

    private static String matchingLabel(Map<String, Locale> options, Locale current) {
        for (Map.Entry<String, Locale> e : options.entrySet()) {
            if (e.getValue().getLanguage().equals(current.getLanguage())) return e.getKey();
        }
        return options.keySet().iterator().next();
    }

    public Node getNode() {
        return choice;
    }
}
