package com.serveur.moba.team;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class TeamQuitListener implements Listener {

    private final TeamService teamService;

    public TeamQuitListener(TeamService teamService) {
        this.teamService = teamService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        teamService.handleQuit(event.getPlayer());
    }
}
