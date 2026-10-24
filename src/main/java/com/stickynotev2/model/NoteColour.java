package com.stickynotev2.model;

import java.awt.Color;

public enum NoteColour {
    YELLOW("#FFF59D", "#FDD835"),
    GREEN("#C5E1A5", "#9CCC65"),
    BLUE("#90CAF9", "#42A5F5"),
    PURPLE("#CE93D8", "#AB47BC"),
    PINK("#F8BBD0", "#F06292"),
    ORANGE("#FFCC80", "#FFA726"),
    GRAY("#E0E0E0", "#9E9E9E");

    private final String bodyHex;
    private final String headerHex;

    NoteColour(String bodyHex, String headerHex) {
        this.bodyHex = bodyHex;
        this.headerHex = headerHex;
    }

    public Color getBody() {
        return Color.decode(bodyHex);
    }

    public Color getHeader() {
        return Color.decode(headerHex);
    }
}
