package net.ae97.pokebot.extensions.mcping.protocol;

import java.nio.ByteBuffer;

import net.ae97.pokebot.extensions.mcping.connection.Manager;

public class Packet {
    private ByteBuffer dataBuffer;
    
    public Packet(Manager manager) {
        dataBuffer = manager.getMemoryManager().allocate();
        // TODO: this is where you left off
    }
}
