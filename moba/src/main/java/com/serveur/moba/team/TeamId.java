package com.serveur.moba.team;

import net.kyori.adventure.text.format.NamedTextColor;

public enum TeamId {
    RED(NamedTextColor.RED, "Rouge"),
    BLUE(NamedTextColor.BLUE, "Bleue");

    private final NamedTextColor color;
    private final String displayNameFr;

    TeamId(NamedTextColor color, String displayNameFr) {
        this.color = color;
        this.displayNameFr = displayNameFr;
    }

    public NamedTextColor color() {
        return color;
    }

    public String displayNameFr() {
        return displayNameFr;
    }
}
