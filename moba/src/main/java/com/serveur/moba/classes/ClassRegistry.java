package com.serveur.moba.classes;

import com.serveur.moba.state.PlayerStateService.Role;
import java.util.*;

public final class ClassRegistry {
    private final EnumMap<Role, ClassPassive> map = new EnumMap<>(Role.class);

    public void register(Role role, ClassPassive impl) {
        map.put(role, impl);
    }

    public ClassPassive of(Role role) {
        return map.get(role);
    }
}
