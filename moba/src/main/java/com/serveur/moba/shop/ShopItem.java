package com.serveur.moba.shop;

import org.bukkit.Material;

public enum ShopItem {

        BRUISER_DIVINE_SUNDERER(
                        "bruiser_divine_sunderer",
                        "Divine Sunderer (Bruiser)",
                        Material.IRON_INGOT,
                        "Toutes les 4 s, votre prochaine attaque rend 3 coeurs.",
                        Role.BRUISER),

        BRUISER_STERAKS_GAGE(
                        "bruiser_steraks_gage",
                        "Sterak's Gage (Bruiser)",
                        Material.IRON_INGOT,
                        "Sous 50% PV : 4 coeurs d'absorption + Force I (30 s de CD).",
                        Role.BRUISER),

        TANK_ROOKERN(
                        "tank_rookern",
                        "Rookern (Tank)",
                        Material.IRON_INGOT,
                        "Après 10 s sans dégâts, recharge jusqu'à 8 coeurs d'absorption.",
                        Role.TANK),

        TANK_HEARTSTEEL(
                        "tank_heartsteel",
                        "Heartsteel (Tank)",
                        Material.IRON_INGOT,
                        "Toutes les 20 attaques sur un adversaire : +1/2 coeur permanent.",
                        Role.TANK),

        TANK_THORNMAIL(
                        "tank_thornmail",
                        "Thornmail (Tank)",
                        Material.IRON_INGOT,
                        "Toutes les 3 attaques subies : l'attaquant perd 1/2 coeur.",
                        Role.TANK),

        ADC_BLOODTHIRST(
                        "adc_bloodthirst",
                        "Soif de sang (ADC)",
                        Material.IRON_INGOT,
                        "Chaque attaque rend 1/2 coeur, indépendamment des dégâts.",
                        Role.ADC),

        ADC_NAVORI(
                        "adc_navori",
                        "Navori (ADC)",
                        Material.IRON_INGOT,
                        "Chaque attaque réduit de 1% les CD des sorts en recharge.",
                        Role.ADC);

        public enum Role {
                BRUISER, TANK, ADC
        }

        public final String id;
        public final String display;
        public final Material icon;
        public final String description;
        public final Role role;

        ShopItem(String id, String display, Material icon, String description, Role role) {
                this.id = id;
                this.display = display;
                this.icon = icon;
                this.description = description;
                this.role = role;
        }

        @Override
        public String toString() {
                return id;
        }
}
