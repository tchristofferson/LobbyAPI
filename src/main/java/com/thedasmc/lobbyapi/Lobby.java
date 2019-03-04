package com.thedasmc.lobbyapi;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectEvent;

import java.util.*;

public class Lobby implements Iterable<ProxiedPlayer> {

    private final ServerInfo server;
    private String name;
    private int size;
    private boolean enabled;
    private boolean inGame;
    private Map<ProxiedPlayer, Server> players;

    Lobby(ServerInfo server, String name, int size) {

        this.server = server;
        this.name = name.trim().toUpperCase();
        this.size = size;
        enabled = true;
        inGame = false;
        players = new HashMap<>(size);

    }

    public ServerInfo getServer() {

        return server;

    }

    public String getName() {

        return name;

    }

    void setName(String name) {

        this.name = name.trim().toUpperCase();

    }

    public int getSize() {

        return players.size();

    }

    public void setSize(int size) {

        this.size = size;

    }

    public boolean isEnabled() {

        return enabled;

    }

    public void setEnabled(boolean b) {

        enabled = b;

    }

    public boolean isInGame() {

        return inGame;

    }

    public void setIsInGame(boolean b) {

        inGame = b;

    }

    public boolean addPlayer(ProxiedPlayer player) {

        if (players.size() == size || !enabled) {

            return false;

        }

        players.put(player, player.getServer());
        player.connect(server);
        return true;

    }

    public boolean kickPlayer(UUID uuid, ServerConnectEvent.Reason reason) {

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        if (player == null || !players.containsKey(player)) {

            return false;

        }

        Server fallback = players.remove(player);
        player.connect(fallback.getInfo(), reason);
        players.remove(player);
        return true;

    }

    public void kickall(ServerConnectEvent.Reason reason) {

        getPlayers().keySet().forEach(player -> kickPlayer(player.getUniqueId(), reason));
        players.clear();

    }

    public Map<ProxiedPlayer, Server> getPlayers() {

        return new HashMap<>(players);

    }

    @Override
    public Iterator<ProxiedPlayer> iterator() {

        return players.keySet().iterator();

    }

    @Override
    public boolean equals(Object object) {

        if (object == this) return true;
        if (!object.getClass().equals(Lobby.class)) return false;
        Lobby lobby = (Lobby) object;

        return lobby.server.equals(server) && lobby.name.equals(name) &&
                lobby.size == size && lobby.enabled == enabled && lobby.players.equals(players);

    }

    @Override
    public int hashCode() {

        return Objects.hash(server, name, size, enabled, players);

    }

}
