package com.serveur.moba.classes.bruiser;

import com.serveur.moba.combat.CombatTagService;
import com.serveur.moba.state.PlayerStateService;
import com.serveur.moba.util.Buffs;
import com.serveur.moba.ui.ActionBarBus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent.Action;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BruiserPassiveListener implements Listener {
    private final CombatTagService combat;
    private final PlayerStateService state;

    private final ActionBarBus actionBarBus;

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
    }

    @EventHandler
    public void onMove(org.bukkit.event.player.PlayerMoveEvent e) {
        Player p = e.getPlayer();

        // ROLE GUARD: seulement pour Bruiser
        if (state.get(p.getUniqueId()).role != PlayerStateService.Role.BRUISER)
            return;

        if (combat.inCombat(p))
            return; // en combat => pas de speed
        // hors combat => Speed 3, ré-appliqué s’il bouge
        Buffs.give(p, org.bukkit.potion.PotionEffectType.SPEED, 3, 4);
        p.sendMessage("§a[Bruiser] Passive — Speed 3 Hors Combat");

    }
}
