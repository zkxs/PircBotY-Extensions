package net.ae97.pokebot.extensions.mcping.pings;

import java.nio.channels.SocketChannel;

import net.ae97.pokebot.extensions.mcping.connection.Manager;
import net.ae97.pokebot.extensions.mcping.connection.PingResultCallback;

@FunctionalInterface
public interface PingImplementationFactory {
    
    /**
     * Used to generalize the creation of ping implementations because constructors are particularly
     * stupid to generalize in Java.
     */
    PingImplementation construct(Manager manager, SocketChannel socketChannel, PingResultCallback callback);
}
