package net.ae97.pokebot.extensions.mcping.connection;

import java.util.LinkedList;
import java.util.List;

public class PingSuccess implements PingResult {
    private String protocolVersion;
    private String serverVersion;
    private String motd;
    private int currentPlayers;
    private int maxPlayers;
    
    /**
     * Construct a successful ping
     * @param protocolVersion Protocol version (e.g. 127)
     * @param serverVersion Server version string (e.g. 1.7.10)
     * @param motd Server MOTD
     * @param currentPlayers Current number of players on the server
     * @param maxPlayers Maximum number of players that can be on the server
     */
    public PingSuccess(String protocolVersion, String serverVersion, String motd, int currentPlayers, int maxPlayers) {
        this.protocolVersion = protocolVersion;
        this.serverVersion = serverVersion;
        this.motd = motd;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }
    
    @Override
    public List<String> getMessage() {
        final List<String> toReturn = new LinkedList<>();
        toReturn.add(String.format("\"%s\" v%s %d/%d players", motd, serverVersion, currentPlayers, maxPlayers));
        return toReturn;
    }
}
