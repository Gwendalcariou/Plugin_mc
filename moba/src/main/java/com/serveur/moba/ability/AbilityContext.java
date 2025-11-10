package com.serveur.moba.ability;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

// Contexte simple : on passe le plugin + le joueur
public record AbilityContext(Plugin plugin, Player player) {
}
