package com.softwelop.deathcounter;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DeathListener implements Listener {

    private final DeathCounterMain plugin;

    // Konstruktor, um Zugriff auf die Hauptklasse (und die Map) zu bekommen
    public DeathListener(DeathCounterMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Füge den Spieler zur Map hinzu, falls er noch nicht existiert (mit 0 Toden)
        // putIfAbsent ist thread-sicher.
        plugin.getDeathCounts().putIfAbsent(uuid, 0);

        // Aktualisiere den Namen in der TAB-Liste sofort beim Join
        // Dies ist Folia-sicher, da der Event-Handler auf dem Thread des Spielers
        // läuft.
        updateTabName(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        // Erhöhe den Zähler für den Spieler
        // compute ist eine thread-sichere Methode, um einen Wert zu aktualisieren
        int newDeathCount = plugin.getDeathCounts().compute(uuid, (key, currentDeaths) -> {
            if (currentDeaths == null) {
                return 1; // Sollte nicht passieren (dank JoinEvent), aber sicher ist sicher
            }
            return currentDeaths + 1;
        });

        // Aktualisiere den Namen in der TAB-Liste
        // Dies ist Folia-sicher, da der Event-Handler auf dem Thread des Spielers
        // läuft.
        updateTabName(player);
    }

    /**
     * Eine Hilfsmethode, um den Namen des Spielers in der TAB-Liste zu
     * aktualisieren.
     */
    private void updateTabName(Player player) {
        int deaths = plugin.getDeathCounts().getOrDefault(player.getUniqueId(), 0);

        // Erstelle den neuen Tablist-Namen: "Borbelbee #Deaths: 4"
        Component tabName = Component.text()
                .append(Component.text(player.getName(), NamedTextColor.WHITE))
                .append(Component.text(" #Deaths: ", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(deaths), NamedTextColor.RED))
                .build();

        // Setze den Namen in der Spielerliste
        player.playerListName(tabName);
    }
}