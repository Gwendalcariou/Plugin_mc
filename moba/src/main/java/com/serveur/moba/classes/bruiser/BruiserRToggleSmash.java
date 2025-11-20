package com.serveur.moba.classes.bruiser;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Buffs;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.*;

public class BruiserRToggleSmash implements Ability {
    private final CooldownService cds;
    private final long durationMs;
    private final long cdMs;
    private final double radius;
    private final double coneDegrees;

    private final Set<UUID> active = new HashSet<>();

    // Rouge "rage" pour l'aura et le shockwave
    private static final Particle.DustOptions RAGE_RED = new Particle.DustOptions(Color.fromRGB(255, 40, 40), 1.6f);

    public BruiserRToggleSmash(CooldownService cds, long durationMs, long cdMs, double radius, double coneDegrees) {
        this.cds = cds;
        this.durationMs = durationMs;
        this.cdMs = cdMs;
        this.radius = radius;
        this.coneDegrees = coneDegrees;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        UUID id = p.getUniqueId();

        // Si actif -> relance pour SMASH
        if (active.contains(id)) {
            active.remove(id);
            p.removePotionEffect(PotionEffectType.STRENGTH);

            // Shockwave visuel + dégâts
            smash(ctx);

            p.sendMessage("§6[Bruiser] R — Smash déclenché !");
            return true;
        }

        // Activation du buff
        if (!cds.ready(p, CooldownIds.BRUISER_R, cdMs)) {
            p.sendMessage("§cR en CD.");
            return false;
        }
        active.add(id);
        Buffs.give(p, PotionEffectType.STRENGTH, 2, (int) (durationMs / 1000));

        // Aura rouge pendant toute la durée de l'activation
        startRageAura(ctx, p, id);

        // Désactivation auto
        ctx.plugin().getServer().getScheduler().runTaskLater(ctx.plugin(), () -> {
            if (active.remove(id)) {
                p.removePotionEffect(PotionEffectType.STRENGTH);
                p.sendMessage("§c[Bruiser] R — expiré.");
            }
        }, durationMs / 50L);

        p.sendMessage("§a[Bruiser] R — Strength II actif (appuie encore pour SMASH)");
        return true;
    }

    private void smash(AbilityContext ctx) {
        Player p = ctx.player();
        Vector fwd = p.getLocation().getDirection().normalize();
        double cos = Math.cos(Math.toRadians(coneDegrees / 2.0));

        // Shockwave visuel vers l'avant
        spawnShockwave(p, fwd);

        p.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && !e.equals(p))
                .map(e -> (LivingEntity) e)
                .filter(le -> {
                    Vector to = le.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                    return fwd.dot(to) >= cos; // dans le cône devant
                })
                .forEach(le -> {
                    le.damage(8.0, p); // “gros dégâts” (à ajuster)
                    le.setVelocity(fwd.clone().multiply(0.7)); // petit knock
                });
    }

    /**
     * Aura rouge tournante autour du Bruiser tant que le R est actif.
     */
    private void startRageAura(AbilityContext ctx, Player p, UUID id) {
        final long period = 2L;

        new BukkitRunnable() {
            long ticks = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead() || !active.contains(id)) {
                    cancel();
                    return;
                }

                Location base = p.getLocation().add(0, 0.8, 0);
                double radius = 0.9;
                int points = 18;
                double angleOffset = ticks * 0.25; // petite rotation

                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i / points) + angleOffset;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location loc = base.clone().add(x, 0, z);

                    p.getWorld().spawnParticle(
                            Particle.DUST,
                            loc,
                            1,
                            0.02, 0.02, 0.02,
                            0,
                            RAGE_RED);
                }

                ticks += period;
            }
        }.runTaskTimer(ctx.plugin(), 0L, period);
    }

    /**
     * Effet visuel du Smash : une vague de coups (SWEEP_ATTACK + CRIT)
     * qui part vers l'avant, bien distincte de l'aura.
     */
    private void spawnShockwave(Player p, Vector dir) {
        World world = p.getWorld();
        Location start = p.getLocation().add(0, 1.0, 0); // hauteur buste

        double maxDist = radius; // cohérent avec la portée de l'attaque
        double stepDist = 0.6; // distance entre chaque "frappe" visuelle
        int steps = (int) (maxDist / stepDist);

        for (int s = 1; s <= steps; s++) {
            double dist = stepDist * s;
            Location center = start.clone().add(dir.clone().multiply(dist));

            // Légère variation latérale pour que ça fasse "coup large"
            for (int i = 0; i < 3; i++) {
                double side = (i - 1) * 0.4; // -0.4, 0, +0.4
                Vector sideOffset = new Vector(-dir.getZ(), 0, dir.getX()).normalize().multiply(side);
                Location loc = center.clone().add(sideOffset);

                // Coup d'arme visuel
                world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);

                // Critiques rouges
                world.spawnParticle(Particle.CRIT, loc, 3, 0.1, 0.1, 0.1, 0.0);

                // Un peu de poussière rouge rageuse pour la cohérence
                world.spawnParticle(
                        Particle.DUST,
                        loc,
                        2,
                        0.02, 0.02, 0.02,
                        0,
                        RAGE_RED);
            }
        }

        // Petit impact au corps du bruiser pour marquer le déclenchement
        world.spawnParticle(
                Particle.DUST_PLUME,
                start,
                5,
                0.1, 0.1, 0.1,
                0.0);
    }

}
