package net.ae97.pokebot.extensions.mcping.pings;

import net.ae97.pokebot.extensions.mcping.connection.PingReadCallback;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;

/**
 * The actual implementation of a specific flavor of ping.
 */
public interface PingImplementation extends PingReadCallback {
    /**
     * 
     * @throws PingException
     */
    public void ping() throws PingException;
}
