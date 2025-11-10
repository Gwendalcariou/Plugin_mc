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
        if (!(e.getEntity() instanceof Player victim))
            return;
        if (!(e.getDamager() instanceof Player attacker))
            return;

        // Récupération des rôles
        var victimRole = state.get(victim.getUniqueId()).role;

        if (victimRole != PlayerStateService.Role.TANK)
            return;

        // Gestion combat tag
        combat.tag(victim);
        combat.tag(attacker);

        // Cooldown : un tank ne peut pas déclencher le passif en boucle
        if (flags.has(victim, FLAG))
            return;

        // On applique la cécité à l'attaquant
        Buffs.give(attacker, org.bukkit.potion.PotionEffectType.BLINDNESS, 3, 3);

        // Feedbacks visuels / textuels
        victim.sendMessage("§a[Tank] Passive — Blindness appliqué à " + attacker.getName());
        victim.sendMessage("§cBlind ! by §a[Tank] " + victim.getName());

        // Mise en cooldown du passif
        flags.set(victim, FLAG, true);
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> flags.set(victim, FLAG, false),
                internalCdMs / 50L);
    }

}
