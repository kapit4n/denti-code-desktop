package com.denticode.desktop.domain.model;

import java.util.EnumSet;
import java.util.Set;

public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED;

    /** Statuses a patient is allowed to set when rescheduling. */
    public static Set<AppointmentStatus> patientReschedulable() {
        return EnumSet.of(SCHEDULED, CONFIRMED, RESCHEDULED);
    }

    /** Statuses that are terminal for billing / patient self-service. */
    public static Set<AppointmentStatus> terminal() {
        return EnumSet.of(COMPLETED, CANCELLED, NO_SHOW);
    }

    public boolean isTerminal() {
        return terminal().contains(this);
    }
}
