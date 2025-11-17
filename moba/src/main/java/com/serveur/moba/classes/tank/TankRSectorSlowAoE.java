package com.serveur.moba.classes.tank;

import com.serveur.moba.ability.*;
import com.serveur.moba.team.TeamService;
import com.serveur.moba.util.Flags;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

public class TankRSectorSlowAoE implements Ability {
    private final CooldownService cds;
    private final int amp; // Slowness level (amplifier)
    private final int seconds; // duration in seconds
    private final double radius; // sector radius
    private final double angleDeg; // sector total angle (e.g., 90 for a quarter pie)
    private final long cdMs;
    private final int windupTicks; // telegraph duration
    private final TeamService teams;
    private static final String FLAG_CC_IMMUNE = "CC_IMMUNE";
    private final Flags flags;

    public TankRSectorSlowAoE(CooldownService cds, TeamService teams, Flags globalFlags, int amp, int seconds,
            double radius,
            double angleDeg, long cdMs,
            int windupTicks) {
        this.cds = cds;
        this.teams = teams;
        this.flags = globalFlags;
        this.amp = amp;
        this.seconds = seconds;
        this.radius = radius;
        this.angleDeg = angleDeg;
        this.cdMs = cdMs;
        this.windupTicks = windupTicks;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        if (!cds.ready(p, CooldownIds.TANK_R, cdMs)) {
            p.sendMessage("§cR en CD.");
            return false;
        }

        final Location origin = p.getLocation().clone().add(0, 0.1, 0);
        final double half = angleDeg / 2.0;
        final Vector facing = getForward(p).clone();

        // Petit root durant la télégraphie
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, windupTicks,
                9, false, false, false));
        p.getWorld().playSound(origin, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);

        // Anim de télégraphie : on “remplit” le secteur progressivement
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                    return;
                }

                // dessine le secteur en particles
                double progress = Math.min(1.0, (double) tick / (double) windupTicks);
                drawSector(origin, facing, radius, half, progress);

                tick += 2;
                if (tick >= windupTicks) {
                    // impact
                    impactSector(p, origin, facing, radius, half);
                    p.getWorld().playSound(origin, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 0.7f);
                    p.getWorld().spawnParticle(
                            Particle.CLOUD,
                            origin.clone().add(facing.clone().multiply(radius * 0.6)),
                            12, 0.4, 0.2, 0.4, 0.02);
                    cds.putOnCooldown(p, CooldownIds.TANK_R, cdMs);
                    cancel();
                }
            }
        }.runTaskTimer(ctx.plugin(), 0L, 2L);

        p.sendMessage("§a[Tank] R — Télégraphie en cours…");
        return true;
    }

    /**
     * Dessine le secteur : arc extérieur + 2 rayons latéraux + léger remplissage
     */
    private void drawSector(Location origin, Vector forward, double radius, double halfAngleDeg, double progress) {
        World w = origin.getWorld();
        if (w == null)
            return;

        Vector f = forward.clone().normalize();
        Vector right = new Vector(-f.getZ(), 0, f.getX()).normalize(); // 90° à droite

        double maxR = Math.max(0.001, radius * progress);
        double yOffset = 1.2;
        double arcPointSpacingDeg = 4;

        // 1) arc extérieur
        for (double a = -halfAngleDeg; a <= halfAngleDeg; a += arcPointSpacingDeg) {
            double phi = Math.toRadians(a);
            double cos = Math.cos(phi), sin = Math.sin(phi);
            Vector offset = f.clone().multiply(maxR * cos).add(right.clone().multiply(maxR * sin));
            Location point = origin.clone().add(offset).add(0, yOffset, 0);

            w.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
            w.spawnParticle(Particle.CHERRY_LEAVES, point, 1, 0, 0, 0, 0);
        }

        // 2) anneau intérieur léger
        double innerR = Math.max(0.3, maxR * 0.6);
        for (double a = -halfAngleDeg; a <= halfAngleDeg; a += arcPointSpacingDeg * 3) {
            double phi = Math.toRadians(a);
            double cos = Math.cos(phi), sin = Math.sin(phi);
            Vector offset = f.clone().multiply(innerR * cos).add(right.clone().multiply(innerR * sin));
            Location point = origin.clone().add(offset).add(0, yOffset, 0);
            w.spawnParticle(Particle.ASH, point, 1, 0, 0, 0, 0);
        }

        // 3) rayons latéraux
        double[] sideAngles = { -halfAngleDeg, halfAngleDeg };
        for (double sa : sideAngles) {
            double phi = Math.toRadians(sa);
            double cos = Math.cos(phi), sin = Math.sin(phi);
            for (double r = 0.2; r <= maxR; r += 0.35) {
                Vector offset = f.clone().multiply(r * cos).add(right.clone().multiply(r * sin));
                Location point = origin.clone().add(offset).add(0, yOffset, 0);
                w.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
            }
        }
    }

    /** Applique l’effet aux joueurs dans le secteur (distance + angle) */
    private void impactSector(Player caster, Location origin, Vector forward, double radius, double halfAngleDeg) {
        origin.getWorld().getNearbyEntities(origin, radius, 2.0, radius).stream()
                .filter(e -> e instanceof Player target && !target.equals(caster))
                .map(e -> (Player) e)
                .filter(target -> !teams.areAllies(caster, target))
                .filter(target -> !flags.has(target, FLAG_CC_IMMUNE))
                .filter(target -> isInSector(origin, forward, target, radius, halfAngleDeg))
                .forEach(target -> target.addPotionEffect(
                        new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.SLOWNESS,
                                seconds * 20,
                                amp,
                                false,
                                true,
                                true)));

        caster.sendMessage("§b[Tank] R — Contrôle de foule appliqué !");
    }

    /** Test sectoriel : within radius & angle */
    private boolean isInSector(Location origin, Vector forward, Entity e, double radius, double halfAngleDeg) {
        Vector to = e.getLocation().toVector().setY(0).subtract(origin.toVector().setY(0));
        double dist = to.length();
        if (dist > radius || dist < 0.01)
            return false;

        double angle = Math.toDegrees(Math.acos(forward.clone().normalize().dot(to.clone().normalize())));
        return angle <= halfAngleDeg;
    }

    /** Vecteur "regard" horizontal du joueur */
    private Vector getForward(Player p) {
        Vector dir = p.getLocation().getDirection();
        dir.setY(0); // horizontal
        if (dir.lengthSquared() == 0)
            return new Vector(0, 0, 1);
        return dir.normalize();
    }
}
