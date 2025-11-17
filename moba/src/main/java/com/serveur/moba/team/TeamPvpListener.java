package com.serveur.moba.team;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class TeamPvpListener implements Listener {

    private final TeamService teamService;

    public TeamPvpListener(TeamService teamService) {
        this.teamService = teamService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player damager = getPlayerDamager(event.getDamager());
        Player victim = (event.getEntity() instanceof Player p) ? p : null;

        if (damager == null || victim == null) {
            return;
        }

        if (teamService.areAllies(damager, victim)) {
            event.setCancelled(true);
        }
    }

    private Player getPlayerDamager(Entity e) {
        if (e instanceof Player p)
            return p;
        if (e instanceof Projectile proj && proj.getShooter() instanceof Player p)
            return p;
        return null;
    }
}
