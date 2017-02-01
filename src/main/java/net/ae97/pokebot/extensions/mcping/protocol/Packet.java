package net.ae97.pokebot.extensions.mcping.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.ae97.pokebot.extensions.mcping.protocol.datatypes.VarInt;

public class Packet {

    public static void send(final int packetId, ByteBuffer data, SocketChannel socket) throws IOException {
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

}
