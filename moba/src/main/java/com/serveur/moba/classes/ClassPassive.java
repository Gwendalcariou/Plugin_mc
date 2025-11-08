package com.serveur.moba.classes;

import org.bukkit.entity.Player;

public interface ClassPassive {
    void enable(Player p);

    void disable(Player p);
}