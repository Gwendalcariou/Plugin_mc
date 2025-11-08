package com.serveur.moba.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import java.util.*;

public final class ActionBarBus {
    private static final class Slot {
        long until;
        Component msg;
    }

    private final Map<UUID, Slot> slots = new HashMap<>();

    /** Message prioritaire qui masque le HUD pendant durationMs */
    public void push(Player p, Component message, long durationMs) {
        long until = System.currentTimeMillis() + Math.max(0, durationMs);
        Slot s = slots.computeIfAbsent(p.getUniqueId(), k -> new Slot());
        s.until = until;
        s.msg = message;
        p.sendActionBar(message);
    }

    /** true = HUD doit se taire ; on purge quand c’est expiré */
    public boolean isSuppressed(Player p) {
        Slot s = slots.get(p.getUniqueId());
        if (s == null)
            return false;
        if (System.currentTimeMillis() >= s.until) {
            slots.remove(p.getUniqueId());
            return false;
        }
        return true;
    }
}
