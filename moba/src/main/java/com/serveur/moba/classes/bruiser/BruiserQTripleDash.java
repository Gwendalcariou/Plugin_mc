package com.serveur.moba.classes.bruiser;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Dash;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.*;

public class BruiserQTripleDash implements Ability {
    private final CooldownService cds;
    private final double dashBlocks;
    private final long chargesWindowMs; // fenêtre pour faire les 3 dashs
    private final long cdMs; // CD total après la 3e (ou fin fenêtre)

    private final Map<UUID, Integer> used = new HashMap<>();
    private final Map<UUID, Long> until = new HashMap<>();

    public BruiserQTripleDash(CooldownService cds, double dashBlocks, long chargesWindowMs, long cdMs) {
        this.cds = cds;
        this.dashBlocks = dashBlocks;
        this.chargesWindowMs = chargesWindowMs;
        this.cdMs = cdMs;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        UUID id = p.getUniqueId();

        // Première activation — check CD
        if (!until.containsKey(id)) {
            if (!cds.ready(p, CooldownIds.BRUISER_Q, cdMs)) {
                p.sendMessage("§cQ en CD.");
                return false;
            }
            used.put(id, 0);
            until.put(id, System.currentTimeMillis() + chargesWindowMs);
            p.sendMessage("§e[Bruiser] Q — Dashs prêts (x3)");
        }

        // Fenêtre expirée ?
        if (System.currentTimeMillis() > until.get(id)) {
            until.remove(id);
            used.remove(id);
            p.sendMessage("§cFenêtre de dash expirée.");
            return false;
        }

        // --- Dash + traînée de poussière ---

        Location start = p.getLocation().clone();

        // Exécuter un dash
        Dash.smallForward(p, dashBlocks);

        Location end = p.getLocation().clone();
        spawnDashTrail(start, end);

        int u = used.merge(id, 1, Integer::sum);

        if (u >= 3) { // fin des charges
            until.remove(id);
            used.remove(id);
            p.sendMessage("§a[Bruiser] Q — Charges consommées");
        }
        return true;
    }

    /**
     * Dessine une traînée de particules DUST_PLUME entre deux points.
     * Plus dense pour donner l'impression qu'elle reste un peu plus longtemps.
     */
    private void spawnDashTrail(Location from, Location to) {
        if (from == null || to == null)
            return;
        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld()))
            return;

        Vector delta = to.toVector().subtract(from.toVector());
        double length = delta.length();
        if (length == 0)
            return;

        int points = (int) (length * 10); // au lieu de 6 -> plus dense
        if (points < 1)
            points = 1;

        Vector step = delta.multiply(1.0 / points);
        Location current = from.clone();

        for (int i = 0; i <= points; i++) {
            world.spawnParticle(
                    Particle.DUST_PLUME,
                    current,
                    3, // plus de particules par point
                    0.05, 0.05, 0.05, // spread légèrement plus grand
                    0.0);
            current.add(step);
        }
    }

}
