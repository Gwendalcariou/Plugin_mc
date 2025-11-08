package com.serveur.moba;

import com.serveur.moba.ability.AbilityContext;
import com.serveur.moba.ability.AbilityKey;
import com.serveur.moba.ability.AbilityRegistry;
import com.serveur.moba.ability.CooldownIds;
import com.serveur.moba.ability.CooldownService;
import com.serveur.moba.classes.ClassService;
import com.serveur.moba.classes.adc.AdcPassiveListener;
import com.serveur.moba.classes.bruiser.BruiserPassiveListener;
import com.serveur.moba.classes.tank.TankPassiveListener;
import com.serveur.moba.combat.CombatTagService;
import com.serveur.moba.game.GameManager;
import com.serveur.moba.listeners.PvpGuardListener;
import com.serveur.moba.state.PlayerStateService;
import com.serveur.moba.state.PlayerStateService.Role;
import com.serveur.moba.util.Flags;
import com.serveur.moba.util.ProtectionListeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.serveur.moba.ui.ActionBarBus;
import com.serveur.moba.ui.CooldownHudService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Point d'entrée du plugin :
 * - charge la config
 * - instancie les services
 * - enregistre les listeners globaux
 * - expose les commandes /moba /forcepvp /class et les sorts (q/w/e/r)
 */
public final class MobaPlugin extends JavaPlugin implements Listener {

        // === Services & états centraux ===
        private PlayerStateService state; // état pur (classe/level/gold…)
        private GameManager gameManager;
        private AbilityRegistry abilities;
        private CooldownService cooldowns;
        private CombatTagService combat;
        private Flags globalFlags;
        private ClassService classService;
        private ActionBarBus actionBarBus;

        // Zones (wand)
        private final Map<UUID, Location> pos1 = new HashMap<>();
        private final Map<UUID, Location> pos2 = new HashMap<>();

        public void setPos1(UUID id, Location l) {
                pos1.put(id, l);
        }

        public void setPos2(UUID id, Location l) {
                pos2.put(id, l);
        }

        public Location getPos1(UUID id) {
                return pos1.get(id);
        }

        public Location getPos2(UUID id) {
                return pos2.get(id);
        }

        @Override
        public void onEnable() {
                saveDefaultConfig();

                // === Instanciation des services (UNE SEULE FOIS) ===
                this.state = new PlayerStateService(); // service "pur état"
                this.combat = new CombatTagService(3000L); // tagging combat
                this.cooldowns = new CooldownService();
                this.globalFlags = new Flags();
                this.abilities = new AbilityRegistry();
                this.gameManager = new GameManager(this);
                this.actionBarBus = new ActionBarBus();

                // === Listeners passifs globaux (role-guarded en interne) ===
                var adcListener = new AdcPassiveListener(state);
                var tankListener = new TankPassiveListener(6_000L, combat, this, state);
                var bruiserListener = new BruiserPassiveListener(combat, state, actionBarBus);

                var pm = getServer().getPluginManager();
                pm.registerEvents(adcListener, this);
                pm.registerEvents(tankListener, this);
                pm.registerEvents(bruiserListener, this);

                // === Service de changement de classe (disable old → enable new) ===
                this.classService = new ClassService(state, adcListener);

                // === Abilities (par rôle) ===
                // Tank
                var tankQ = new com.serveur.moba.classes.tank.TankQEmpowered(cooldowns, 10000L, 6000L);
                abilities.register(PlayerStateService.Role.TANK, AbilityKey.Q, tankQ);
                abilities.register(PlayerStateService.Role.TANK, AbilityKey.W,
                                new com.serveur.moba.classes.tank.TankWAbsorb(cooldowns, 8.0, 12000L));
                abilities.register(PlayerStateService.Role.TANK, AbilityKey.E,
                                new com.serveur.moba.classes.tank.TankEDash(cooldowns, globalFlags, 4.0, 500L, 8000L));
                abilities.register(PlayerStateService.Role.TANK, AbilityKey.R,
                                new com.serveur.moba.classes.tank.TankRSlowAoE(cooldowns, 2, 3, 6.0, 20000L));
                pm.registerEvents(new com.serveur.moba.classes.tank.TankQListener(tankQ), this);

                // Bruiser
                abilities.register(PlayerStateService.Role.BRUISER, AbilityKey.Q,
                                new com.serveur.moba.classes.bruiser.BruiserQTripleDash(cooldowns, 3.0, 6000L, 9000L));
                abilities.register(PlayerStateService.Role.BRUISER, AbilityKey.W,
                                new com.serveur.moba.classes.bruiser.BruiserWSlowAoE(cooldowns, 2, 3, 4.0, 10000L));
                abilities.register(PlayerStateService.Role.BRUISER, AbilityKey.E,
                                new com.serveur.moba.classes.bruiser.BruiserEDashAbsorb(cooldowns, 6.0, 8000L));
                abilities.register(PlayerStateService.Role.BRUISER, AbilityKey.R,
                                new com.serveur.moba.classes.bruiser.BruiserRToggleSmash(cooldowns, 8000L, 25000L, 6.0,
                                                70.0));

                // ADC
                abilities.register(PlayerStateService.Role.ADC, AbilityKey.Q,
                                new com.serveur.moba.classes.adc.AdcQAttackSpeed(cooldowns, 9000L, 6000L, 2.0));
                abilities.register(PlayerStateService.Role.ADC, AbilityKey.W,
                                new com.serveur.moba.classes.adc.AdcWShield(cooldowns, globalFlags, 12000L, 2000L));
                abilities.register(PlayerStateService.Role.ADC, AbilityKey.E,
                                new com.serveur.moba.classes.adc.AdcESlowZone(cooldowns, 2, 6.0, 6000L, 14000L));
                abilities.register(PlayerStateService.Role.ADC, AbilityKey.R,
                                new com.serveur.moba.classes.adc.AdcRAllSteroid(cooldowns, 25000L, 8000L, 6.0, 3.0));

                var hud = new CooldownHudService(this, state, cooldowns, actionBarBus);

                // === Tank ===
                hud.map(Role.TANK, AbilityKey.Q, CooldownIds.TANK_Q, 10_000L);
                hud.map(Role.TANK, AbilityKey.W, CooldownIds.TANK_W, 12_000L);
                hud.map(Role.TANK, AbilityKey.E, CooldownIds.TANK_E, 8_000L);
                hud.map(Role.TANK, AbilityKey.R, CooldownIds.TANK_R, 20_000L);

                hud.map(Role.BRUISER, AbilityKey.Q, CooldownIds.BRUISER_Q, 9_000L);
                hud.map(Role.BRUISER, AbilityKey.W, CooldownIds.BRUISER_W, 10_000L);
                hud.map(Role.BRUISER, AbilityKey.E, CooldownIds.BRUISER_E, 8_000L);
                hud.map(Role.BRUISER, AbilityKey.R, CooldownIds.BRUISER_R, 25_000L);

                hud.map(Role.ADC, AbilityKey.Q, CooldownIds.ADC_Q, 6_000L);
                hud.map(Role.ADC, AbilityKey.W, CooldownIds.ADC_W, 12_000L);
                hud.map(Role.ADC, AbilityKey.E, CooldownIds.ADC_E, 14_000L);
                hud.map(Role.ADC, AbilityKey.R, CooldownIds.ADC_R, 25_000L);

                // Démarrer l’affichage périodique
                hud.start();

                getLogger().info("Abilities init OK.");

                // === Listeners “système” ===
                pm.registerEvents(new PvpGuardListener(gameManager.lane()), this);
                pm.registerEvents(new ProtectionListeners(globalFlags), this);
                pm.registerEvents(new com.serveur.moba.listeners.WandListener(this), this);

                // Hooks de nettoyage (désactiver passifs + nettoyer état)
                pm.registerEvents(new Listener() {
                        @org.bukkit.event.EventHandler
                        public void onQuit(PlayerQuitEvent e) {
                                classService.disableCurrent(e.getPlayer());
                                state.clear(e.getPlayer());
                        }

                        @org.bukkit.event.EventHandler
                        public void onDeath(PlayerDeathEvent e) {
                                classService.disableCurrent(e.getEntity());
                        }
                }, this);

                // === Commandes ===
                var mobaCmd = new com.serveur.moba.commands.MobaCommand(
                                this, gameManager, state, abilities, classService);
                getCommand("moba").setExecutor(mobaCmd);
                getCommand("moba").setTabCompleter(mobaCmd);
                getCommand("forcepvp").setExecutor(mobaCmd);
                getCommand("forcepvp").setTabCompleter(mobaCmd);
                getCommand("class").setExecutor(mobaCmd);

                getCommand("q").setExecutor(this);
                getCommand("w").setExecutor(this);
                getCommand("e").setExecutor(this);
                getCommand("r").setExecutor(this);

                // === Lancement de la boucle de jeu ===
                gameManager.start();
                getLogger().info("Moba enabled!");
        }

        @Override
        public void onDisable() {
                // Désactiver proprement les passifs encore actifs
                for (Player p : getServer().getOnlinePlayers()) {
                        classService.disableCurrent(p);
                }
                getLogger().info("Moba disabled!");
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                        @NotNull String label, @NotNull String[] args) {

                if (command.getName().equalsIgnoreCase("class")) {
                        if (!(sender instanceof Player p)) {
                                sender.sendMessage("§cSeulement les joueurs in-game peuvent exécuter cette commande");
                                return true;
                        }
                        if (args.length != 1) {
                                p.sendMessage("§cUsage: /class <tank|bruiser|adc>");
                                return true;
                        }
                        var opt = classService.parseRole(args[0]);
                        if (opt.isEmpty()) {
                                p.sendMessage("§cClasse inconnue (choix: tank, bruiser, adc)");
                                return true;
                        }
                        classService.setClass(p, opt.get());
                        p.sendMessage("§aClasse définie à §b" + opt.get().name());
                        return true;
                }

                // Sorts (Q/W/E/R) déclenchés ici
                if (command.getName().equalsIgnoreCase("q")
                                || command.getName().equalsIgnoreCase("w")
                                || command.getName().equalsIgnoreCase("e")
                                || command.getName().equalsIgnoreCase("r")) {
                        if (!(sender instanceof Player p)) {
                                sender.sendMessage("Joueur requis.");
                                return true;
                        }
                        var s = state.get(p.getUniqueId());
                        AbilityKey key = switch (command.getName().toLowerCase()) {
                                case "q" -> AbilityKey.Q;
                                case "w" -> AbilityKey.W;
                                case "e" -> AbilityKey.E;
                                default -> AbilityKey.R;
                        };
                        var ab = abilities.get(s.role, key);
                        if (ab == null) {
                                p.sendMessage("§cAucune compétence assignée.");
                                return true;
                        }
                        boolean ok = ab.cast(new AbilityContext(this, p));
                        if (!ok)
                                p.sendMessage("§cImpossible de lancer le sort.");
                        return true;
                }

                return false;
        }
}
