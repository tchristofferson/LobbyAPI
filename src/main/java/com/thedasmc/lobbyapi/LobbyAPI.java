package com.thedasmc.lobbyapi;

import com.sun.istack.internal.NotNull;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tchristofferson
 * @version v0.1
 *
 * Created to use for mini-game lobbies such as a pre-game lobby.
 */
public class LobbyAPI extends Plugin implements Listener {

    private static Map<String, Lobby> lobbies;
    private static Configuration config;

    @Override
    public void onEnable() {

        lobbies = new HashMap<>();
        loadConfig();
        loadLobbies();

    }

    @Override
    public void onDisable() {

        try {

            saveLobbies();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    /**
     * Create and register a new lobby
     *
     * @param serverInfo The server where the lobby is located
     * @param name The name of the lobby
     * @param size How many players can join the lobby
     * @return The newly created lobby
     */
    @NotNull
    public static Lobby createLobby(ServerInfo serverInfo, String name, int size) {

        if (lobbies.containsKey(name.trim().toUpperCase())) {

            throw new IllegalArgumentException("A lobby with that name already exists!");

        }

        Lobby lobby = new Lobby(serverInfo, name, size);
        lobbies.put(lobby.getName(), lobby);
        return lobby;

    }

    /**
     * Delete and unregister a lobby
     * This will also kick all players that are in the lobby
     *
     * @param name The name of the lobby
     * @return {@code true} if the lobby exists and was removed, {@code false} otherwise
     */
    public static boolean deleteLobby(String name) {

        name = name.trim().toUpperCase();

        if (!lobbies.containsKey(name)) {

            return false;

        }

        Lobby lobby = lobbies.remove(name);
        lobby.kickall(ServerConnectEvent.Reason.KICK_REDIRECT);

        return true;

    }

    /**
     * Rename a lobby
     *
     * @param oldName The current name of the lobby
     * @param newName The name to change it to
     * @return {@code true} if a lobby with the old name was found and there isn't a lobby that exists with the new name, {@code false} otherwise
     */
    public static boolean renameLobby(String oldName, String newName) {

        oldName = oldName.trim().toUpperCase();
        newName = newName.trim().toUpperCase();

        if (!lobbies.containsKey(oldName) || lobbies.containsKey(newName)) {

            return false;

        }

        Lobby lobby = lobbies.remove(oldName);
        lobby.setName(newName);
        lobbies.put(newName, lobby);
        return true;

    }

    /**
     * Get a lobby
     *
     * @param lobby The name of the lobby
     * @return The lobby with the specified name or null if one wasn't found
     */
    public static Lobby getLobby(String lobby) {

        return lobbies.get(lobby.trim().toUpperCase());

    }

    private void loadConfig() {

        if (!getDataFolder().exists()) {

            getDataFolder().mkdir();

        }

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {

            try {

                configFile.createNewFile();
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

            } catch (IOException e) {

                e.printStackTrace();

            }

        }

    }

    private void loadLobbies() {

        if (config.getKeys().isEmpty()) {

            return;

        }

        for (String lobbyName : config.getKeys()) {

            String serverName = config.getString(lobbyName + ".server");
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverName);
            int size = config.getInt(lobbyName + ".size");
            boolean enabled = config.getBoolean(lobbyName + ".enabled");

            createLobby(serverInfo, lobbyName, size);

        }

    }

    private void saveLobbies() throws IOException {

        for (String lobbyName : config.getKeys()) {

            config.set(lobbyName, null);

        }

        lobbies.forEach((name, lobby) -> {

            config.set(name + ".server", lobby.getServer().getName());
            config.set(name + ".size", lobby.getSize());
            config.set(name + ".enabled", lobby.isEnabled());

        });

        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));

    }

    @EventHandler
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {

        ProxiedPlayer player = event.getPlayer();

        new HashMap<>(lobbies).forEach((name, lobby) -> {

            if (lobby.contains(player)) {

                lobby.removePlayer(player);
                return;

            }

        });

    }

}
