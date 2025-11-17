package com.serveur.moba.team;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatTeamListener implements Listener {

    private final TeamService teamService;

    public ChatTeamListener(TeamService teamService) {
        this.teamService = teamService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();

        String coloredName = switch (teamService.getTeam(p).orElse(null)) {
            case RED -> "§c" + p.getName() + "§r";
            case BLUE -> "§9" + p.getName() + "§r";
            case null -> p.getName();
        };

        event.setFormat(coloredName + " : %2$s");
    }
}
