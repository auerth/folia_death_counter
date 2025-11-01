package com.softwelop.deathcounter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Server;

public class DeathCounterMain extends JavaPlugin {

    // Thread-sichere Map für die Todeszähler (UUID des Spielers, Anzahl Tode)
    private ConcurrentHashMap<UUID, Integer> deathCounts;

    @Override
    public void onEnable() {
        // Map initialisieren
        this.deathCounts = new ConcurrentHashMap<>();

        // Config.yml (Standard-Speicherdatei) laden, falls nicht vorhanden
        saveDefaultConfig();

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        
        // ASYNCHRON (Folia-sicher): Lade die Zähler aus der Datei.
        // Wir nutzen den GlobalScheduler, da dies keine bestimmte Welt betrifft.
        getServer().getGlobalRegionScheduler().execute(this, this::loadCountsFromFile);

        getLogger().info("Folia DeathCounter erfolgreich geladen!");
    }

    @Override
    public void onDisable() {
        // SYNCHRON (Folia-sicher): Speichere die Zähler.
        // onDisable wird beim Herunterfahren aufgerufen, hier wollen wir sicherstellen,
        // dass es gespeichert wird, bevor der Server ausgeht.
        // Da dies beim Stoppen passiert, ist ein kurzer "blocking" Task in Ordnung.
        saveCountsToFile();
        
        getLogger().info("Folia DeathCounter gestoppt!");
    }

    // Getter für den Listener, um auf die Map zuzugreifen
    public ConcurrentHashMap<UUID, Integer> getDeathCounts() {
        return deathCounts;
    }


    private void loadCountsFromFile() {
        getLogger().info("Lade Todeszähler...");
        FileConfiguration config = getConfig();
        // Gehe durch alle Einträge im Abschnitt "counts"
        if (config.isConfigurationSection("counts")) {
            config.getConfigurationSection("counts").getKeys(false).forEach(key -> {
                UUID uuid = UUID.fromString(key);
                int deaths = config.getInt("counts." + key);
                deathCounts.put(uuid, deaths);
            });
        }
        getLogger().info(deathCounts.size() + " Einträge geladen.");
    }

    private void saveCountsToFile() {
        getLogger().info("Speichere Todeszähler...");
        FileConfiguration config = getConfig();
        
        // Sektion "counts" leeren, um alte Daten zu überschreiben
        config.set("counts", null); 
        
        // Alle aktuellen Zähler in die Config schreiben
        deathCounts.forEach((uuid, deaths) -> {
            config.set("counts." + uuid.toString(), deaths);
        });

        // Speichern
        saveConfig();
        getLogger().info("Speichern abgeschlossen.");
    }
}