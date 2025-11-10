package com.serveur.moba.classes.bruiser;

import com.serveur.moba.combat.CombatTagService;
import com.serveur.moba.state.PlayerStateService;
import com.serveur.moba.util.Buffs;
import com.serveur.moba.ui.ActionBarBus;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BruiserPassiveListener implements Listener {
    private final CombatTagService combat;
    private final PlayerStateService state;
    private final ActionBarBus actionBarBus;

    // true = le bonus de vitesse hors combat est actuellement actif pour ce joueur
    private final Map<UUID, Boolean> active = new HashMap<>();

    public BruiserPassiveListener(CombatTagService combat, PlayerStateService state, ActionBarBus actionBarBus) {
        this.combat = combat;
        this.state = state;
        this.actionBarBus = actionBarBus;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim))
            return;

        if (e.getDamager() instanceof Player damager) {
            combat.tag(victim);
            combat.tag(damager);
        } else if (CombatTagService.isMonster(e.getDamager())) {
            combat.tag(victim);
        }

        // entrée en combat -> le passif est considéré comme inactif
        active.put(victim.getUniqueId(), false);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (state.get(p.getUniqueId()).role != PlayerStateService.Role.BRUISER)
            return;

        if (e.getFrom().getX() == e.getTo().getX() &&
                e.getFrom().getY() == e.getTo().getY() &&
                e.getFrom().getZ() == e.getTo().getZ()) {
            return;
        }

        if (combat.inCombat(p))
            return;

        boolean hadSpeed = p.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED);

        Buffs.give(p, org.bukkit.potion.PotionEffectType.SPEED, 3, 4);

        if (!hadSpeed) {
            p.sendMessage("§a[Bruiser] Passif — Speed III (hors combat)");
        }
    }

}
