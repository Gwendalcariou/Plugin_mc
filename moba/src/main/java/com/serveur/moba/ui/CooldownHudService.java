package com.serveur.moba.ui;

import com.serveur.moba.ability.AbilityKey;
import com.serveur.moba.ability.CooldownService;
import com.serveur.moba.state.PlayerStateService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Locale;

public final class CooldownHudService {

    private final Plugin plugin;
    private final PlayerStateService state;
    private final CooldownService cds;
    private final ActionBarBus bus;

    private final EnumMap<PlayerStateService.Role, EnumMap<AbilityKey, CooldownInfo>> table = new EnumMap<>(
            PlayerStateService.Role.class);

    private record CooldownInfo(String id, long ms) {
    }

    public CooldownHudService(Plugin plugin, PlayerStateService state, CooldownService cds, ActionBarBus bus) {
        this.plugin = plugin;
        this.state = state;
        this.cds = cds;
        this.bus = bus;
        for (var r : PlayerStateService.Role.values())
            table.put(r, new EnumMap<>(AbilityKey.class));
    }

    /** On map un sort à son id de cooldown et sa durée max */
    public void map(PlayerStateService.Role role, AbilityKey key, String id, long ms) {
        table.get(role).put(key, new CooldownInfo(id, ms));
    }

    /**
     * On lance la mise à jour automatique du HUD (toutes les 10 ticks) sans écraser
     */
    public void start() {
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (bus.isSuppressed(p))
                    continue; // ne pas écraser les messages des sorts
                var role = state.get(p.getUniqueId()).role;
                if (role == PlayerStateService.Role.NONE) {
                    p.sendActionBar(Component.empty());
                    continue;
                }
                String line = renderString(p, role);
                // IMPORTANT: convertir les codes § en Component
                var comp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .deserialize(line);
                p.sendActionBar(comp);
            }
        }, 20L, 10L);
    }

    private String renderString(org.bukkit.entity.Player p, PlayerStateService.Role role) {
        var map = table.get(role);
        if (map == null || map.isEmpty())
            return "";
        String q = format(p, map.get(AbilityKey.Q), "Q");
        String w = format(p, map.get(AbilityKey.W), "W");
        String e = format(p, map.get(AbilityKey.E), "E");
        String r = format(p, map.get(AbilityKey.R), "R");
        return q + " §7| " + w + " §7| " + e + " §7| " + r;
    }

    private String format(Player p, CooldownInfo info, String label) {
        if (info == null)
            return "§8" + label + " –";
        long remain = cds.remaining(p, info.id, info.ms);
        if (remain <= 0)
            return "§a" + label + " ✓";
        double s = remain / 1000.0;
        String num = (s >= 10) ? String.valueOf((int) Math.ceil(s))
                : String.format(Locale.US, "%.1f", s);
        String color = (s <= 5.0) ? "§e" : "§c";
        return color + label + " " + num + "s";
    }
}
