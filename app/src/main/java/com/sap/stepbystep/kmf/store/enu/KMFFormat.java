package com.sap.stepbystep.kmf.store.enu;

import org.jetbrains.annotations.NotNull;

public enum KMFFormat {
    JSON("json"),
    XML("xml");

    private final String text;

    KMFFormat(final String text) {
        this.text = text;
    }

    @NotNull
    @Override
    public String toString() {
        return text;
    }
}
