package com.serveur.moba.util;

import java.util.*;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;

public final class PassiveTaskBus {
    private final Map<UUID, List<BukkitTask>> tasks = new HashMap<>();

    public void track(UUID id, BukkitTask t) {
        tasks.computeIfAbsent(id, k -> new ArrayList<>()).add(t);
    }

    public void cancelAll(UUID id) {
        List<BukkitTask> list = tasks.remove(id);
        if (list != null)
            list.forEach(t -> {
                try {
                    t.cancel();
                } catch (Exception ignored) {
                }
            });
    }
}
