package net.ae97.pokebot.extensions.mcping.connection;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.extensions.mcping.Server;

public class PingResultCallback {
    
    private CommandEvent commandEvent;
    private Server server;
    
    public PingResultCallback(CommandEvent commandEvent, Server server) {
        this.commandEvent = commandEvent;
        this.server = server;
    }
    
    /**
     * Called when a ping has completed.
     * @param pingResult The result of a ping.
     */
    public void onComplete(PingResult pingResult) {
        StringBuilder serverLine = new StringBuilder();
        serverLine.append(String.format("Pinging %s", server.toString()));
        
        if (server.isSrvRecord()) {
            serverLine.append(" using SRV record");
        }
        commandEvent.respond(serverLine.toString());
        
        for (String line: pingResult.getMessage()) {
            commandEvent.respond(line);
        }
    }
}
