package com.serveur.moba.state;

import java.util.*;
import org.bukkit.entity.Player;

public final class PlayerStateService {

    public enum Role {
        TANK, BRUISER, ADC
    }

    public static class State {
        public Role role = Role.BRUISER;
        public int level = 1;
        public int xp = 0;
        public int gold = 0;
        public int aaCount = 0;
    }

    private final Map<UUID, State> states = new HashMap<>();

    public State get(UUID id) {
        return states.computeIfAbsent(id, k -> new State());
    }

    public Role getRole(Player p) {
        return get(p.getUniqueId()).role;
    }

    public void setRole(Player p, Role role) {
        get(p.getUniqueId()).role = role;
    }

    public void clear(Player p) {
        states.remove(p.getUniqueId());
    }
}
