package com.denticode.desktop.core;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Tiny i18n facade: holds the active locale, exposes a bindable property,
 * and resolves message keys from {@code i18n/messages_xx.properties}.
 *
 * Views can either call {@link #t(String, Object...)} directly or
 * use {@link #binding(String, Object...)} for labels that should refresh
 * when the locale changes.
 */
public final class I18n {

    private static final String BUNDLE = "i18n.messages";

    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(this, "locale", Locale.ENGLISH);
    private ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale.get());

    public I18n(String localeTag) {
        setLocale(parse(localeTag));
    }

    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public Locale getLocale() {
        return locale.get();
    }

    public void setLocale(Locale newLocale) {
        if (newLocale == null) newLocale = Locale.ENGLISH;
        bundle = ResourceBundle.getBundle(BUNDLE, newLocale);
        locale.set(newLocale);
    }

    public String t(String key, Object... args) {
        String pattern;
        try {
            pattern = bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
        if (args == null || args.length == 0) return pattern;
        return MessageFormat.format(pattern, args);
    }

    /**
     * Bindable variant of {@link #t}. Recomputes when the locale changes.
     */
    public StringBinding binding(String key, Object... args) {
        return Bindings.createStringBinding(() -> t(key, args), locale);
    }

    private static Locale parse(String tag) {
        if (tag == null || tag.isBlank()) return Locale.ENGLISH;
        return Locale.forLanguageTag(tag);
    }
}
