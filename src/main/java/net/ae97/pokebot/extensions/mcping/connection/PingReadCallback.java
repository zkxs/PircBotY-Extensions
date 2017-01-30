package net.ae97.pokebot.extensions.mcping.connection;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import net.ae97.pokebot.extensions.mcping.pings.PingImplementation;

@FunctionalInterface
public interface PingReadCallback {
    
    /**
     * Used to callback to the {@link PingImplementation} when the ManagerThread is reading the
     * response of a specific ping
     */
    public void onReadable(SelectionKey key, ByteBuffer receiveBuffer);
};
