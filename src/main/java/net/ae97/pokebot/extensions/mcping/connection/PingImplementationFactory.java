package net.ae97.pokebot.extensions.mcping.connection;

import java.nio.channels.SocketChannel;

public interface PingImplementationFactory {
    PingImplementation construct(Manager manager, SocketChannel socketChannel, PingCallback callback);
}
