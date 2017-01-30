package net.ae97.pokebot.extensions.mcping.pings;

import net.ae97.pokebot.extensions.mcping.connection.PingReadCallback;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;

public interface PingImplementation extends PingReadCallback {
    
    /**
     * The actual implementation of a specific flavor of ping.
     * @throws PingException if something goes wrong
     */
    public void ping() throws PingException;
}
