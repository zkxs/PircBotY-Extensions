package net.ae97.pokebot.extensions.mcping.pings.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

import net.ae97.pokebot.extensions.mcping.MCPingExtension;
import net.ae97.pokebot.extensions.mcping.connection.Manager;
import net.ae97.pokebot.extensions.mcping.connection.PingFailure;
import net.ae97.pokebot.extensions.mcping.connection.PingResultCallback;
import net.ae97.pokebot.extensions.mcping.connection.LegacyPingSuccess;
import net.ae97.pokebot.extensions.mcping.pings.PingImplementation;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.UnexpectedPingException;

/**
 * Works on clients version 1.4 and up.
 * <br>See <a href="http://wiki.vg/Server_List_Ping">http://wiki.vg/Server_List_Ping</a>.
 */
public class LegacyStatus implements PingImplementation {
    
    private static final byte[] MAGIC_NUMBER = {(byte) 0xFE, 0x01};
    private static final byte KICK_PACKET = (byte) 0xFF;
    private static final String RESPONSE_PREFIX = "§1\0";
    private static final int PREFIX_LENGTH = RESPONSE_PREFIX.length();
    private static final String NULL_CHAR = "\0";
    
    private Manager manager;
    private SocketChannel socketChannel;
    private PingResultCallback callback;
    private ByteBuffer receiveBuffer;
    private long lastActivityTime = System.currentTimeMillis();
    
    public LegacyStatus(Manager manager, SocketChannel socketChannel, PingResultCallback callback) {
        this.manager = manager;
        this.socketChannel = socketChannel;
        this.callback = callback;
    }
    
    @Override
    public void ping() throws PingException {
        final ByteBuffer sendBuf = ByteBuffer.wrap(MAGIC_NUMBER);
        
        try {
            socketChannel.write(sendBuf);
        } catch (IOException e) {
            throw new UnexpectedPingException(e);
        }
        
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

            receiveBuffer.flip();
            receiveBuffer.order(ByteOrder.BIG_ENDIAN);

            if (receiveBuffer.remaining() < 3) {
                callback.onComplete(
                        new PingFailure(String.format("Invalid response size (%d)", receiveBuffer.remaining())));
                return;
            }

            final byte packetType = receiveBuffer.get();
            if (packetType != KICK_PACKET) {
                callback.onComplete(new PingFailure("Invalid response (incorrect magic number)"));
                return;
            }

            final int lengthPrefix = receiveBuffer.getShort() & 0xFFFF;
            if (lengthPrefix * 2 != receiveBuffer.remaining()) {
                callback.onComplete(new PingFailure("Invalid response (incorrect length-prefix)"));
                return;
            }

            byte[] stringBuf = new byte[receiveBuffer.remaining()];
            receiveBuffer.get(stringBuf, 0, receiveBuffer.remaining());

            String responseString = null;
            try {
                responseString = new String(stringBuf, "UTF-16BE");
            } catch (UnsupportedEncodingException e) {
                MCPingExtension.getMcPingLogger().log(Level.SEVERE, "What kind of JRE doesn't support UTF-16BE?", e);
                callback.onComplete(new PingFailure("Unsupported Encoding: " + e.getMessage()));
                return;
            }

            if (!responseString.startsWith(RESPONSE_PREFIX)) {
                callback.onComplete(new PingFailure("Invalid response (incorrect string-prefix)"));
                return;
            }

            if (responseString.length() < PREFIX_LENGTH + 1) {
                callback.onComplete(new PingFailure("Invalid response (nothing after string-prefix)"));
                return;
            }

            final String[] fields = responseString.substring(PREFIX_LENGTH).split(NULL_CHAR);
            if (fields.length != 5) {
                callback.onComplete(new PingFailure("Invalid response (incorrect number of fields)"));
                return;
            }

            int currentPlayers = -1;
            int maxPlayers = -1;
            try {
                currentPlayers = Integer.parseInt(fields[3]);
                maxPlayers = Integer.parseInt(fields[4]);
            } catch (NumberFormatException e) {
                callback.onComplete(new PingFailure("Invalid response (non-integer player count)"));
                return;
            }

            try {
                socketChannel.close();
            } catch (IOException e) {
                // We really don't care if we can't close the socket at this point. We're done with it.
                MCPingExtension.getMcPingLogger().log(Level.WARNING, "Error closing SocketChannel", e);
            }

            callback.onComplete(new LegacyPingSuccess(fields[0], fields[1], fields[2], currentPlayers, maxPlayers));
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
