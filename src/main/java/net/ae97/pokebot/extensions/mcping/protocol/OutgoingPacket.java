package net.ae97.pokebot.extensions.mcping.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.ae97.pokebot.extensions.mcping.connection.Manager;
import net.ae97.pokebot.extensions.mcping.protocol.datatypes.VarInt;

public class OutgoingPacket {
    private ByteBuffer data;

    public OutgoingPacket(Manager manager) {
        data = manager.getMemoryManager().allocate();
        data.clear();
    }

    public void send(final int packetId, SocketChannel socket) throws IOException {
        final ByteBuffer header = ByteBuffer.allocateDirect(10); // 10 is the max size of two varints

        // determine the length of the packet id
        VarInt.write(packetId, header);
        final int packetIdLength = header.position() - 1;

        header.clear();
        data.flip(); // prepare data for writing
        VarInt.write(packetIdLength + data.remaining(), header);
        VarInt.write(packetId, header);
        header.flip(); // prepare header for writing

        // write packet to socket
        socket.write(header);
        socket.write(data);
    }

    public ByteBuffer getDataBuffer() {
        return data;
    }

}
