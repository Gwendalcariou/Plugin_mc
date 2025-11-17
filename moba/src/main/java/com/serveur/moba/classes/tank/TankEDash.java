package com.serveur.moba.classes.tank;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Dash;
import com.serveur.moba.util.Flags;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

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

        // Dash
        Dash.smallForward(p, distance);

        // Feedback
        p.sendMessage("§a[Tank] E — dash + immunité CC");
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.2f);

        // CD du sort
        cds.putOnCooldown(p, CooldownIds.TANK_E, cdMs);

        // Fin de l’immunité après ccImmuneMs
        long ticks = ccImmuneMs / 50L;
        ctx.plugin().getServer().getScheduler().runTaskLater(ctx.plugin(),
                () -> flags.set(p, FLAG_CC_IMMUNE, false),
                ticks);

        return true;
    }
}
