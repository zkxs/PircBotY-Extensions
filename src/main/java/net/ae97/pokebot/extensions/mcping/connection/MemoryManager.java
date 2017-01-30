package net.ae97.pokebot.extensions.mcping.connection;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MemoryManager {
    private ConcurrentLinkedQueue<ByteBuffer> bufferQueue = new ConcurrentLinkedQueue<>();
    
    // max size of a TCP packet
    private static final int MAX_PACKET_SIZE = 65535;
    
    // maximum number of buffers to keep around
    private static final int MAX_BUFFERS = 5;
    
    public ByteBuffer allocate() {
        final ByteBuffer head = bufferQueue.poll();
        if (head == null) {
            return ByteBuffer.allocateDirect(MAX_PACKET_SIZE);
        } else {
            return head;
        }
    }
    
    public void free(ByteBuffer buf) {
        if (bufferQueue.size() < MAX_BUFFERS) {
            bufferQueue.offer(buf);
        }
    }
}
