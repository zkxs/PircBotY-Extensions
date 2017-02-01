package net.ae97.pokebot.extensions.mcping.pings.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import net.ae97.pokebot.extensions.mcping.connection.JsonPingSuccess;
import net.ae97.pokebot.extensions.mcping.connection.Manager;
import net.ae97.pokebot.extensions.mcping.connection.PingFailure;
import net.ae97.pokebot.extensions.mcping.connection.PingResultCallback;
import net.ae97.pokebot.extensions.mcping.pings.PingImplementation;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.UnexpectedPingException;
import net.ae97.pokebot.extensions.mcping.protocol.Packet;
import net.ae97.pokebot.extensions.mcping.protocol.datatypes.DataTypeException;
import net.ae97.pokebot.extensions.mcping.protocol.datatypes.PrefixedString;
import net.ae97.pokebot.extensions.mcping.protocol.datatypes.VarInt;

public class ServerListPing implements PingImplementation {

    private static final int PROTOCOL_VERSION = 47;
    private static final int HANDSHAKE_PACKET_ID = 0x00;
    private static final int REQUEST_PACKET_ID = 0x00;
    private static final int STATUS_STATE = 1;
    
    private Manager manager;
    private SocketChannel socketChannel;
    private PingResultCallback callback;
    private ByteBuffer receiveBuffer;
    private long lastActivityTime = System.currentTimeMillis();
    
    public ServerListPing(Manager manager, SocketChannel socketChannel, PingResultCallback callback) {
        this.manager = manager;
        this.socketChannel = socketChannel;
        this.callback = callback;
    }
    
    @Override
    public void ping() throws PingException {
        
        InetSocketAddress address;
        
        try {
            address = (InetSocketAddress) socketChannel.getRemoteAddress();
        } catch (IOException e) {
            throw new UnexpectedPingException(e);
        }
        
        final String host = address.getHostString();
        final short port = (short)address.getPort();
        
        final ByteBuffer sendBuffer = manager.getMemoryManager().allocate();
        sendBuffer.clear();
        sendBuffer.order(ByteOrder.BIG_ENDIAN);
        
        VarInt.write(PROTOCOL_VERSION, sendBuffer);
        PrefixedString.write(host, sendBuffer);
        sendBuffer.putShort(port);
        VarInt.write(STATUS_STATE, sendBuffer);
        
        try {
            Packet.send(HANDSHAKE_PACKET_ID, sendBuffer, socketChannel);
            sendBuffer.clear();
            Packet.send(REQUEST_PACKET_ID, sendBuffer, socketChannel);
        } catch (IOException e) {
            throw new UnexpectedPingException(e);
        }
        
        manager.getMemoryManager().free(sendBuffer);

        // register listener for server response
        manager.registerChannel(socketChannel, SelectionKey.OP_READ, this);
    }

    @Override
    public void onReadable(SelectionKey key) {
        
        lastActivityTime = System.currentTimeMillis();
        
        if (receiveBuffer == null) {
            receiveBuffer = manager.getMemoryManager().allocate();
            receiveBuffer.clear();
        }

        try {
            final int bytesRead = socketChannel.read(receiveBuffer);

            if (bytesRead != -1) {
                // we've read our bytes and will look at this again later.
                return;
            }

        } catch (IOException e) {
            callback.onComplete(new PingFailure("Error during read(): " + e.getMessage()));
            cleanup(key);
            return;
        }
        
        /* This try doesn't catch anything. It's just so I can use a finally to
         * free the receiveBuffer instead of doing so before every return.
         */
        try {
            
            // prepare for reading
            receiveBuffer.flip();
            receiveBuffer.order(ByteOrder.BIG_ENDIAN);
            
            // check packet length
            final int length = VarInt.read(receiveBuffer);
            if (length != receiveBuffer.remaining()) {
                callback.onComplete(new PingFailure("Invalid Response Packet (length mismatch)"));
                return;
            }
            
            // check packet id
            final int packetId = VarInt.read(receiveBuffer);
            if (packetId != REQUEST_PACKET_ID) {
                callback.onComplete(new PingFailure("Received wrong packet type: " + packetId));
                return;
            }
            
            final String jsonResponse = PrefixedString.read(receiveBuffer);
            
            callback.onComplete(new JsonPingSuccess(jsonResponse));
            return;
            
        } catch (DataTypeException e) {
            callback.onComplete(new PingFailure("Invalid Response Packet"));
            return;
        } finally {
            cleanup(key);
        }

    }
    
    /**
     * Free any resources this ping was using
     */
    private void cleanup(SelectionKey key) {
        manager.getMemoryManager().free(receiveBuffer);
        key.cancel();
    }

    @Override
    public long getLastActivityTime() {
        return lastActivityTime;
    }

    @Override
    public PingResultCallback getCallback() {
        return callback;
    }

}
