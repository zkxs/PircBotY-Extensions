package net.ae97.pokebot.extensions.mcping.legacy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import net.ae97.pokebot.extensions.mcping.PingException;
import net.ae97.pokebot.extensions.mcping.UnexpectedPingException;
import net.ae97.pokebot.extensions.mcping.connection.Manager;
import net.ae97.pokebot.extensions.mcping.connection.PingCallback;
import net.ae97.pokebot.extensions.mcping.connection.PingFailure;
import net.ae97.pokebot.extensions.mcping.connection.PingImplementation;
import net.ae97.pokebot.extensions.mcping.connection.PingResult;
import net.ae97.pokebot.extensions.mcping.connection.PingSuccess;

public class LegacyStatus implements PingImplementation {
    
    private static final byte[] MAGIC_NUMBER = {(byte) 0xFE, 0x01};
    private static final byte KICK_PACKET = (byte) 0xFF;
    private static final String RESPONSE_PREFIX = "§1\0";
    private static final int PREFIX_LENGTH = RESPONSE_PREFIX.length();
    private static final String NULL_CHAR = "\0";
    
    private Manager manager;
    private SocketChannel socketChannel;
    private PingCallback callback;
    
    public LegacyStatus(Manager manager, SocketChannel socketChannel, PingCallback callback) {
        this.manager = manager;
        this.socketChannel = socketChannel;
        this.callback = callback;
    }
    
    public void ping() throws PingException {
        final ByteBuffer sendBuf = ByteBuffer.wrap(MAGIC_NUMBER);
        
        try {
            socketChannel.write(sendBuf);
        } catch (IOException e) {
            throw new UnexpectedPingException(e);
        }
        
        manager.registerChannel(socketChannel, SelectionKey.OP_READ, this::onReadable);
    }
    
    public void onReadable(SelectionKey key, ByteBuffer receiveBuffer) {
        receiveBuffer.clear();
        try {
            socketChannel.read(receiveBuffer);
            final int bytesRead = socketChannel.read(receiveBuffer);
            
            if (bytesRead != -1) {
                callback.onComplete(new PingFailure("Server did not close connection"));
                return;
            }
            
        } catch (IOException e) {
            callback.onComplete(new PingFailure("Error during read(): " + e.getMessage()));
            return;
        }
        
        receiveBuffer.flip();
        receiveBuffer.order(ByteOrder.BIG_ENDIAN);
        
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
            e.printStackTrace(); //TODO: log
            callback.onComplete(new PingFailure("Unsupported Encoding: " + e.getMessage()));
            return;
        }
        
        if (! responseString.startsWith(RESPONSE_PREFIX)) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        callback.onComplete(new PingSuccess(fields[0], fields[1], fields[2], currentPlayers, maxPlayers));
        return;
    }
}
