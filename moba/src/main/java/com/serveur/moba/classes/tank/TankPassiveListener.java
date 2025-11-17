package com.serveur.moba.classes.tank;

import com.serveur.moba.combat.CombatTagService;
import com.serveur.moba.state.PlayerStateService;
import com.serveur.moba.team.TeamService;
import com.serveur.moba.util.Buffs;
import com.serveur.moba.util.Flags;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

public class TankPassiveListener implements Listener {

    private final long internalCdMs;
    private final Flags flags;
    private final CombatTagService combat;
    private final Plugin plugin;
    private static final String FLAG_PASSIVE_CD = "tank_passive_cd";
    private static final String FLAG_CC_IMMUNE = "CC_IMMUNE";
    private final PlayerStateService state;
    private final TeamService teams;

    public TankPassiveListener(long internalCdMs, Flags globalFlags, CombatTagService combat, Plugin plugin,
            PlayerStateService state,
            TeamService teams) {
        this.internalCdMs = internalCdMs;
        this.flags = globalFlags;
        this.combat = combat;
        this.plugin = plugin;
        this.state = state;
        this.teams = teams;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim))
            return;
        if (!(e.getDamager() instanceof Player attacker))
            return;
        if (teams.areAllies(attacker, victim))
            return;
        if (flags.has(attacker, FLAG_CC_IMMUNE))
            return;

        // Récupération des rôles
        var victimRole = state.get(victim.getUniqueId()).role;

        if (victimRole != PlayerStateService.Role.TANK)
            return;

        // Gestion combat tag
        combat.tag(victim);
        combat.tag(attacker);

        // Cooldown : un tank ne peut pas déclencher le passif en boucle
        if (flags.has(victim, FLAG_PASSIVE_CD))
            return;

        // On applique la cécité à l'attaquant
        Buffs.give(attacker, org.bukkit.potion.PotionEffectType.BLINDNESS, 3, 3);

        // Feedbacks visuels / textuels
        victim.sendMessage("§a[Tank] Passive — Blindness appliqué à " + attacker.getName());
        victim.sendMessage("§cBlind ! by §a[Tank] " + victim.getName());

        // Mise en cooldown du passif
        flags.set(victim, FLAG_PASSIVE_CD, true);
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> flags.set(victim, FLAG_PASSIVE_CD, false),
                internalCdMs / 50L);
    }

}
