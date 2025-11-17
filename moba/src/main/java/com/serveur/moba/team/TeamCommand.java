package com.serveur.moba.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class TeamCommand implements CommandExecutor, TabCompleter {

    private final TeamService teamService;

    public TeamCommand(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(Component.text("Usage: /team <red|blue|leave>", NamedTextColor.YELLOW));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "red" -> {
                teamService.join(p, TeamId.RED);
                p.sendMessage(Component.text("Tu as rejoint l'équipe Rouge.", NamedTextColor.RED));
            }
            case "blue" -> {
                teamService.join(p, TeamId.BLUE);
                p.sendMessage(Component.text("Tu as rejoint l'équipe Bleue.", NamedTextColor.BLUE));
            }
            case "leave" -> {
                teamService.leave(p);
                p.sendMessage(Component.text("Tu as quitté ton équipe.", NamedTextColor.GRAY));
            }
            default -> p.sendMessage(Component.text("Usage: /team <red|blue|leave>", NamedTextColor.YELLOW));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("red", "blue", "leave");
        }
        return List.of();
    }
}
