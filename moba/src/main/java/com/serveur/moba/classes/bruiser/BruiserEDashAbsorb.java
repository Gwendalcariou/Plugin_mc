package com.serveur.moba.classes.bruiser;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Buffs;
import com.serveur.moba.util.Dash;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BruiserEDashAbsorb implements Ability {
    private final CooldownService cds;
    private final double distance;
    private final long cdMs;

    private final Particle.DustOptions YELLOW_TRAIL = new Particle.DustOptions(Color.fromRGB(255, 210, 30), 1.7f);

    public BruiserEDashAbsorb(CooldownService cds, double distance, long cdMs) {
        this.cds = cds;
        this.distance = distance;
        this.cdMs = cdMs;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        if (!cds.ready(p, CooldownIds.BRUISER_E, cdMs)) {
            p.sendMessage("§cE en CD.");
            return false;
        }

        // Position de départ
        Location start = p.getLocation().clone();

        // Dash
        Dash.smallForward(p, distance);

        // Position d’arrivée
        Location end = p.getLocation().clone();

        // Traînée jaune (absorption)
        spawnYellowTrail(start, end);

        // Buff absorption
        Buffs.absorption(p, 4.0, 10);

        p.sendMessage("§a[Bruiser] E — Dash + Absorption");
        return true;
    }

    private void spawnYellowTrail(Location from, Location to) {
        if (from == null || to == null)
            return;
        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld()))
            return;

        Vector delta = to.toVector().subtract(from.toVector());
        double length = delta.length();
        if (length == 0)
            return;

        int points = (int) (length * 7); // particules par bloc
        if (points < 1)
            points = 1;

        Vector step = delta.multiply(1.0 / points);
        Location current = from.clone();

        for (int i = 0; i <= points; i++) {
            world.spawnParticle(
                    Particle.DUST,
                    current,
                    1,
                    0.04, 0.04, 0.04,
                    0,
                    YELLOW_TRAIL);
            current.add(step);
        }
    }
}
