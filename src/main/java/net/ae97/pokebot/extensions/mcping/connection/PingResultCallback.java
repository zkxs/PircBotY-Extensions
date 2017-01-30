package net.ae97.pokebot.extensions.mcping.connection;

@FunctionalInterface
public interface PingResultCallback {
    
    /**
     * Called when a ping has completed.
     * @param pingResult The result of a ping.
     */
    void onComplete(PingResult pingResult);
}
