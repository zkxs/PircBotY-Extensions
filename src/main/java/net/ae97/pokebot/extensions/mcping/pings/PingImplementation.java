package net.ae97.pokebot.extensions.mcping.pings;

import java.nio.channels.SelectionKey;

import net.ae97.pokebot.extensions.mcping.connection.PingResultCallback;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;

public interface PingImplementation {
    
    /**
     * The actual implementation of a specific flavor of ping.
     * @throws PingException if something goes wrong
     */
    public void ping() throws PingException;
    
    /**
     * Used to callback to the {@link PingImplementation} when the ManagerThread is reading the
     * response of a specific ping
     */
    public void onReadable(SelectionKey key);
    
    /**
     * @return the last time something happened to this ping
     */
    public long getLastActivityTime();
    
    /**
     * @return the callback to be called when this ping is done
     */
    public PingResultCallback getCallback();
}
