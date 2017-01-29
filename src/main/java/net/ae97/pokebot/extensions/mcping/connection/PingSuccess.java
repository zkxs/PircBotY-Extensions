package net.ae97.pokebot.extensions.mcping.connection;

public class PingSuccess implements PingResult {
    private String protocolVersion;
    private String serverVersion;
    private String motd;
    private int currentPlayers;
    private int maxPlayers;
    
    public PingSuccess(String protocolVersion, String serverVersion, String motd, int currentPlayers, int maxPlayers) {
        this.protocolVersion = protocolVersion;
        this.serverVersion = serverVersion;
        this.motd = motd;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }
    
    public String getMessage() {
        return String.format("%s %s \"%s\" %d / %d", protocolVersion, serverVersion, motd, currentPlayers, maxPlayers);
    }
}
