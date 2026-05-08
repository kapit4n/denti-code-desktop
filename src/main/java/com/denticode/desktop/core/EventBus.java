package com.denticode.desktop.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A trivial publish/subscribe bus used for cross-view notifications
 * (e.g. "patient created" so the doctor's patients list refreshes).
 */
public final class EventBus {

    public static final String PATIENT_CHANGED = "patient.changed";
    public static final String APPOINTMENT_CHANGED = "appointment.changed";
    public static final String PERFORMED_ACTION_CHANGED = "performedAction.changed";
    public static final String INVENTORY_CHANGED = "inventory.changed";
    public static final String PAYMENT_CHANGED = "payment.changed";
    public static final String LOCALE_CHANGED = "locale.changed";

    private final Map<String, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Runnable subscribe(String topic, Consumer<T> handler) {
        Consumer<Object> wrapped = (Consumer<Object>) handler;
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(wrapped);
        return () -> {
            List<Consumer<Object>> list = subscribers.get(topic);
            if (list != null) list.remove(wrapped);
        };
    }

    public void publish(String topic, Object payload) {
        List<Consumer<Object>> list = subscribers.get(topic);
        if (list == null) return;
        for (Consumer<Object> c : list) {
            try {
                c.accept(payload);
            } catch (RuntimeException ignored) {
                // Subscribers must not break the bus.
            }
        }
    }
}
