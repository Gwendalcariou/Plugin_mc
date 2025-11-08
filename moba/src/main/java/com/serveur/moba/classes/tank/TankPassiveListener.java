package com.serveur.moba.classes.tank;

import com.serveur.moba.combat.CombatTagService;
import com.serveur.moba.state.PlayerStateService;
import com.serveur.moba.util.Buffs;
import com.serveur.moba.util.Flags;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

public class TankPassiveListener implements Listener {
    private final long internalCdMs;
    private final Flags flags = new Flags();
    private final CombatTagService combat;
    private final Plugin plugin;
    private static final String FLAG = "tank_passive_cd";
    private final PlayerStateService state;

    public TankPassiveListener(long internalCdMs, CombatTagService combat, Plugin plugin, PlayerStateService state) {
        this.internalCdMs = internalCdMs;
        this.combat = combat;
        this.plugin = plugin;
        this.state = state;

    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {

        if (!(e.getEntity() instanceof Player victim) || !(e.getDamager() instanceof Player attacker))
            return;

        var s = state.get(victim.getUniqueId());
        if (s.role != PlayerStateService.Role.TANK)
            return;

        combat.tag(victim);
        combat.tag(attacker);

        if (flags.has(victim, FLAG))
            return;

        Buffs.give(attacker, org.bukkit.potion.PotionEffectType.BLINDNESS, 3, 3);
        flags.set(victim, FLAG, true);
        victim.sendMessage("§a[Tank] Passive — Blindness appliqué à " + attacker);

        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> flags.set(victim, FLAG, false),
                internalCdMs / 50L);
    }
}
