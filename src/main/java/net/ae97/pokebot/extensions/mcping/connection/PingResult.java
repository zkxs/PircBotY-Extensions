package net.ae97.pokebot.extensions.mcping.connection;

import java.util.List;

/**
 * Contains the result of a ping, be it a success or failure
 */
public interface PingResult {
    
    /**
     * @return A message containing all relevant information about a ping
     */
    public List<String> getMessage();
}
