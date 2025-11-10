package com.serveur.moba.util;

import com.serveur.moba.ability.AbilityKey;
import com.serveur.moba.state.PlayerStateService.Role;

import java.util.EnumMap;
import java.util.Map;

public final class CooldownBase {
    private final Map<Role, Map<AbilityKey, Long>> base = new EnumMap<>(Role.class);

    public void set(Role r, AbilityKey k, long ms) {
        base.computeIfAbsent(r, x -> new EnumMap<>(AbilityKey.class)).put(k, ms);
    }

    public long get(Role r, AbilityKey k) {
        var m = base.get(r);
        return (m == null) ? 0L : m.getOrDefault(k, 0L);
    }
}
