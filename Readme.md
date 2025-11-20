# Projet Minecraft MOBA – Plugin Serveur

## 🎮 Description générale

Ce dépôt contient le code source d’un plugin Minecraft orienté **MOBA** (inspiré de jeux comme _League of Legends_).  
L’objectif à terme est de faire tourner ce plugin sur un **serveur public** jouable par tout le monde.

Le plugin ajoute notamment :

- des **rôles / classes** (Tank, Bruiser, ADC, etc.) ;
- des **kits** et équipements associés ;
- des **sorts** (Q / W / E / R, passifs) avec gestion de cooldowns ;
- un **système de boutique in‑game** (items achetables, effets passifs, etc.) ;
- des mécaniques de combat spécifiques (tag de combat, protections, teamfight…).

Le projet est développé pour **Minecraft 1.21.4**, avec des tests effectués principalement sur **Paper**.  
Pour les tests à plus grande échelle, un **serveur loué** est utilisé.

---

## 🧑‍💻 Équipe & organisation

- **Développement du plugin** : une seule personne (le développeur principal de ce dépôt).
- **Équipe projet** : 4 personnes fixes (et parfois des renforts pour des tests à grande échelle), chacune avec un rôle bien défini: game design, organisation, build, etc.
- **Organisation du travail** :
  - Réunions régulières pour faire le point sur :
    - ce qui a été fait par chacun ;
    - ce qui reste à implémenter ou tester ;
    - les priorités pour les prochaines itérations.
  - **Sessions de tests approfondies**, en particulier pour :
    - les mécaniques de combat multijoueur ;
    - l’équilibrage des sorts et items ;
    - les interactions entre rôles/classes.
  - Maintien d’un **cahier des charges** détaillé (fonctionnalités prévues, règles de gameplay, contraintes techniques).
  - Rédaction d’un **document guide**, décrivant :
    - les nouvelles fonctionnalités ajoutées via le plugin ;
    - les mécaniques de bases ;
    - les différentes règles du jeu.

---

## ⚙️ Stack technique

- **Version Minecraft** : 1.21.4
- **Serveur** : [Paper](https://papermc.io/) pour les tests rapides locaux
- **Tests à grande échelle** : serveur dédié loué pour tester en conditions proches de la prod
- **Langage** : Java
- **API** : Bukkit / Spigot / Paper API
- **IDE conseillé** : Visual Studio Code (ou un IDE Java classique : IntelliJ IDEA / Eclipse)

---

## 🕹️ Fonctionnalités principales (actuelles)

### Rôles / Classes

- Gestion d’un **PlayerStateService** qui associe chaque joueur à un **rôle** :
  - ex. `TANK`, `BRUISER`, `ADC`, etc.
- Chaque rôle :
  - possède ses propres **sorts** (Q / W / E / R + passif),
  - dispose de **kits d’équipement** dédiés,
  - est ciblé par des **items de boutique** spécifiques (effets passifs adaptés au rôle).

### Sorts & cooldowns

- Système de sorts basé sur :
  - un **AbilityRegistry** (enregistrement des compétences),
  - des **AbilityKey** (Q / W / E / R),
  - un **CooldownService** centralisé :
    - pose de cooldowns,
    - gestion du “ready / not ready”,
    - réduction dynamique des temps de recharge (ex. item Navori).
- **HUD de cooldown** :
  - affichage des CD dans la barre d’action (`ActionBar`),
  - mise à jour régulière via `CooldownHudService`,
  - respect des priorités d’affichage (`ActionBarBus`).

### Kits & inventaire

- `KitService` :
  - applique les **kits d’armure et d’armes** en fonction du rôle (Tank, Bruiser, ADC, etc.) qui sont voués à changer plus tard;
  - remplit une **hotbar de sorts** (objets représentant Q / W / E / R / passif) ;
  - verrouille certains items pour empêcher leur drop ou modification involontaire.
- `SpellHotbar` :
  - attribue un **tag** (Q, W, E, R, PASSIVE) aux items de la hotbar via `NamespacedKey`,
  - permet de déclencher les sorts en fonction de la sélection dans la barre rapide.

### Boutique & items passifs

- `ShopService` + `ShopListeners` :
  - gestion d’items de boutique achetables via une **émeraude** dans la hotbar ;
  - application d’**effets passifs** tant que l’item est “actif” chez le joueur ;
  - suppression des effets lors de la vente/retrait de l’item.
- Exemples d’items (inspirés de LoL) :
  - **Bruiser**
    - Divine Sunderer : soin périodique sur auto‑attaque.
    - Sterak’s Gage : bouclier + force quand la vie passe sous un certain seuil.
  - **Tank**
    - Rookern : cœurs d’absorption après un certain temps sans subir de dégâts.
    - Heartsteel : gain permanent de points de vie max après un nombre d’attaques.
    - Thornmail : renvoi de dégâts après plusieurs attaques reçues.
  - **ADC**
    - Soif de sang : vol de vie simplifié (régénération fixe par auto‑attaque).
    - Navori : réduction des cooldowns des sorts sur chaque attaque automatique.

---

## 🚀 Objectif final

À terme, ce plugin doit permettre de proposer un **mode de jeu MOBA complet dans Minecraft**, avec :

- des **parties structurées** (équipes, objectifs, timers, etc.) ;
- des **rôles bien différenciés** et équilibrés ;
- une **progression en partie** (or, achats à la boutique, montée en puissance) ;
- une **expérience proche d’un MOBA**, mais adaptée au gameplay Minecraft.

L’objectif ultime est d’ouvrir un **serveur public** pour permettre à d’autres joueurs de découvrir le mode de jeu et d’obtenir des retours sur :

- l’équilibrage des rôles / sorts / items,
- la lisibilité des mécaniques,
- les performances serveur et la stabilité.

---

## 🔧 Lancer le plugin en local (Paper)

1. Télécharger **Paper 1.21.4** et créer un serveur local.
2. Compiler le plugin (via Maven/Gradle ou depuis l’IDE) pour obtenir le `.jar`.
3. Placer le `.jar` dans le dossier `plugins/` du serveur Paper.
4. Démarrer le serveur.
5. Rejoindre le serveur en 1.21.4 et utiliser les commandes exposées par le plugin (sélection de classe, lancement de partie, etc., selon ce qui est déjà implémenté).

---

_Note : ce README décrit l’état actuel et la vision du projet. Il est amené à évoluer au fur et à mesure que de nouvelles fonctionnalités sont ajoutées (nouveaux rôles, nouveaux sorts, refontes de systèmes, etc.)._
