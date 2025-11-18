package com.serveur.moba.ability;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownService {
    private final Map<String, Long> cds = new HashMap<>();

    private static String k(UUID id, String name) {
        return id + ":" + name;
    }

    public boolean ready(Player p, String name, long ms) {
        long now = System.currentTimeMillis();
        String key = k(p.getUniqueId(), name);
        Long last = cds.get(key);
        if (last == null || (now - last) >= ms) {
            cds.put(key, now);
            return true;
        }
        return false;
    }

    public long remaining(Player p, String name, long ms) {
        Long last = cds.get(k(p.getUniqueId(), name));
        if (last == null)
            return 0;
        long d = ms - (System.currentTimeMillis() - last);
        return Math.max(0, d);
    }

    public boolean isReady(Player p, String name, long ms) {
        return remaining(p, name, ms) <= 0;
    }

    public void putOnCooldown(Player p, String name, long ms) {
        cds.put(k(p.getUniqueId(), name), System.currentTimeMillis());
    }

    /**
     * Reduce the remaining cooldown for (player,name) by percent of the full
     * cooldown (ms).
     * percent = 1.0 means 1% -> reduces by ms * 0.01 milliseconds (at least 1ms).
     * No-op if there is no active cooldown or remaining <= 0.
     */
    public void reduceRemainingPercent(Player p, String name, long ms, double percent) {
        String key = k(p.getUniqueId(), name);
        Long last = cds.get(key);
        if (last == null)
            return;
        long now = System.currentTimeMillis();
        long elapsed = now - last;
        long remaining = ms - elapsed;
        if (remaining <= 0)
            return;
        long reduce = Math.max(1L, (long) Math.floor(ms * (percent / 100.0)));
        long newLast = last + reduce;
        cds.put(key, newLast);
    }
}
