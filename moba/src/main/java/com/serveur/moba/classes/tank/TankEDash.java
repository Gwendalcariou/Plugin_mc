package com.serveur.moba.classes.tank;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Dash;
import com.serveur.moba.util.Flags;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TankEDash implements Ability {
    private final CooldownService cds;
    private final Flags flags;
    private final long ccImmuneMs;
    private final double distance;
    private final long cdMs;
    private static final String FLAG_CC_IMMUNE = "CC_IMMUNE";

    public TankEDash(CooldownService cds, Flags globalFlags, double distance, long ccImmuneMs, long cdMs) {
        this.cds = cds;
        this.flags = globalFlags;
        this.distance = distance;
        this.ccImmuneMs = ccImmuneMs;
        this.cdMs = cdMs;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        if (!cds.ready(p, CooldownIds.TANK_E, cdMs)) {
            p.sendMessage("§cE en CD.");
            return false;
        }

        // Immunité CC
        flags.set(p, FLAG_CC_IMMUNE, true);

        // Position de départ (pour le premier segment)
        Location start = p.getLocation().clone();

        // Dash
        Dash.smallForward(p, distance);

        Location end = p.getLocation().clone();
        spawnDashTrailSmoke(start, end);

        // Feedback
        p.sendMessage("§a[Tank] E — dash + immunité CC");
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.2f);

        // CD du sort
        cds.putOnCooldown(p, CooldownIds.TANK_E, cdMs);

        // Fin de l’immunité après ccImmuneMs
        long ticks = ccImmuneMs / 50L;
        ctx.plugin().getServer().getScheduler().runTaskLater(
                ctx.plugin(),
                () -> flags.set(p, FLAG_CC_IMMUNE, false),
                ticks);

        return true;
    }

    /**
     * Dessine une traînée de fumée sombre pendant le dash du tank.
     * Identique au Bruiser (qui utilise DUST_PLUME) mais ici avec SMOKE.
     */
    private void spawnDashTrailSmoke(Location from, Location to) {
        if (from == null || to == null)
            return;
        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld()))
            return;

        Vector delta = to.toVector().subtract(from.toVector());
        double length = delta.length();
        if (length == 0)
            return;

        int points = (int) (length * 10);
        if (points < 1)
            points = 1;

        Vector step = delta.multiply(1.0 / points);
        Location current = from.clone();

        for (int i = 0; i <= points; i++) {

            // fumée lourde et dense
            world.spawnParticle(
                    Particle.SMOKE,
                    current,
                    8, // beaucoup de fumée
                    0.2, 0.2, 0.2, // spread large → impression de masse
                    0.01 // speed faible
            );

            current.add(step);
        }
    }

}
