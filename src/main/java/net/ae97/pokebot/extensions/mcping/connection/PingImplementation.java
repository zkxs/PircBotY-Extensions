package net.ae97.pokebot.extensions.mcping.connection;

import net.ae97.pokebot.extensions.mcping.PingException;
import net.ae97.pokebot.extensions.mcping.connection.Manager.ManagerCallback;

public interface PingImplementation extends ManagerCallback {
    public void ping() throws PingException;
}
