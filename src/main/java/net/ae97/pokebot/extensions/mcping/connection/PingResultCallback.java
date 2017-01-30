package net.ae97.pokebot.extensions.mcping.connection;

import net.ae97.pircboty.api.events.CommandEvent;

public class PingResultCallback {
    
    private CommandEvent commandEvent;
    
    public PingResultCallback(CommandEvent commandEvent) {
        this.commandEvent = commandEvent;
    }
    
    /**
     * Called when a ping has completed.
     * @param pingResult The result of a ping.
     */
    public void onComplete(PingResult pingResult) {
        for (String line: pingResult.getMessage()) {
            commandEvent.respond(line);
        }
    }
}
