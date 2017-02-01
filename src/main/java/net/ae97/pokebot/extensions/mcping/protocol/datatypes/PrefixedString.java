package net.ae97.pokebot.extensions.mcping.protocol.datatypes;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import net.ae97.pokebot.extensions.mcping.MCPingExtension;

/**
 * See http://wiki.vg/Protocol#Data_types
 */
public class PrefixedString {

    public static void write(String string, ByteBuffer buf) {
        VarInt.write(string.length(), buf);
        try {
            buf.put(string.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String read(ByteBuffer buf) throws DataTypeException {
        
        final int length = VarInt.read(buf);
        
        if (length < 0) {
            throw new DataTypeException("negative length prefix");
        }
        
        final byte[] stringBuf = new byte[length];
        buf.get(stringBuf);
        
        try {
            return new String(stringBuf, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
