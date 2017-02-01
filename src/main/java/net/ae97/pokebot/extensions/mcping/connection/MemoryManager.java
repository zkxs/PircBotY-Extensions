package net.ae97.pokebot.extensions.mcping.connection;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Allows for reuse of bytebuffers. For simplicity, we simply make byte buffers of the maximal required size. Due to
 * this, they can be rather large. We reduce memory allocations by reusing byte buffers.
 * 
 * Additionally, we only keep up to MAX_BUFFERS buffers stored for reuse. This prevents a sudden spike of ping from
 * permanently increasing the memory used by this McPingExtension
 */
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
