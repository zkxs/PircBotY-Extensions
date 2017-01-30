package net.ae97.pokebot.extensions.mcping.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * Companion class to {@link Manager}.
 * This is the thread that handles reading responses to pings.
 */
public class ManagerThread implements Runnable {
    private static final long SELECTOR_CLOSE_TIMEOUT = 30000L;
    private static final long SELECTOR_SELECT_TIMEOUT = 500L;
    private static final int MAX_PACKET_SIZE = 65535;
    
    private Selector selector;
    
    /** called as this thread shuts down */
    private Runnable callback;
    
    /** last time something interesting happened */
    private long lastEventTime;
    
    /** I know it's bigish, but there is only one at any given time. */
    private ByteBuffer receiverBuffer = ByteBuffer.allocate(65535);
    
    /** Used for avoiding select/register race condition */
    private Lock lock;
    
    /**
     * @param selector Selector from Manager
     * @param callback Called when this ManagerThread shuts down
     * @param lock Lock from Manager
     */
    public ManagerThread(Selector selector, Runnable callback, Lock lock) {
        this.selector = selector;
        this.callback = callback;
        this.lock = lock;
    }
    
    @Override
    public void run() {
        lastEventTime = System.currentTimeMillis();
        
        // while the selector has had recent events
        while (System.currentTimeMillis() - lastEventTime < SELECTOR_CLOSE_TIMEOUT) {
            try {
                final int selected = selector.select(SELECTOR_SELECT_TIMEOUT);
                
                /* Prevent a race condition.
                 * See comment in Manager.registerChannel() for full explanation.
                 */
                lock.lock();
                lock.unlock();
                
                if (selected == 0) {
                    // if nothing is selected, skip back to the top of the while()
                    continue;
                } else {
                    // something is selected. "touch" the Manager thread so it doesn't shut down.
                    lastEventTime = System.currentTimeMillis();
                }
                
                // iterate over selected keys
                final Set<SelectionKey> selectedKeys = selector.selectedKeys();
                final Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    final SelectionKey key = iterator.next();
                    
                    // I never want to see this key again, so I remove it from the selector.
                    key.cancel();
                    
                    // Call this key's callback. See PingImplementation.
                    final PingReadCallback cb = (PingReadCallback) key.attachment();
                    cb.onReadable(key, receiverBuffer);
                    
                    // Remove this key from the selection set.
                    iterator.remove();
                }
                
            } catch (IOException e) {
                // Could be thrown by selector.select
                e.printStackTrace(); // TODO: log
            }
        }
        
        // inform the Manager that the ManagerThread is shutting down
        callback.run();
    }
}
