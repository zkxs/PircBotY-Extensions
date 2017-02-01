package net.ae97.pokebot.extensions.mcping.protocol.datatypes;

import java.nio.ByteBuffer;
import java.util.logging.Level;

import net.ae97.pokebot.extensions.mcping.MCPingExtension;

/**
 * See http://wiki.vg/Protocol#VarInt_and_VarLong
 */
public class VarInt {

    public static void write(int value, ByteBuffer buf) {
        do {
            byte temp = (byte)(value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buf.put(temp);
        } while (value != 0);
    }

    public static int read(ByteBuffer buf) throws DataTypeException {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buf.get();
            final int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new DataTypeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

}
