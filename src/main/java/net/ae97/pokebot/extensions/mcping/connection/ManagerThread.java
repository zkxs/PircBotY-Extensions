package net.ae97.pokebot.extensions.mcping.connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extensions.mcping.pings.PingImplementation;

/**
 * Companion class to {@link Manager}.
 * This is the thread that handles reading responses to pings.
 */
public class ManagerThread implements Runnable {
    // TODO: get these from Config
    private static final long SELECTOR_CLOSE_TIMEOUT = 30000L;
    private static final long SELECTOR_SELECT_TIMEOUT = 500L;
    
    /**
     * How old does a connection have to be before we consider it hung?
     * Should be much shorter than SELECTOR_CLOSE_TIMEOUT, or else the ManagerThread may shut down
     * with this key still in the keyset. This would prevent its resources from being garbage collected
     * until the next time someone runs a ping.
     */
    private static final long CONNECTION_KILL_TIMEOUT = 3000L;
    
    private Selector selector;
    
    /** called as this thread shuts down */
    private Runnable callback;
    
    /** last time something interesting happened */
    private long lastEventTime;
    
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
                
                // Scan for keys that have been lying around way too long and cancel them
                /* restrict scope */ {
                    final Set<SelectionKey> allKeys = selector.keys();
                    final Iterator<SelectionKey> iterator = allKeys.iterator();
                    final long currentTime = System.currentTimeMillis();
                    while (iterator.hasNext()) {
                        final SelectionKey key = iterator.next();
                        final PingImplementation cb = (PingImplementation) key.attachment();
                        if (currentTime - cb.getLastActivityTime() > CONNECTION_KILL_TIMEOUT) {
                            cb.getCallback().onComplete(new PingFailure("Connection hung"));
                            key.cancel();
                            try {
                                key.channel().close();
                            } catch (IOException e) {
                                // We don't care if a hung connection can't be closed.
                                // This is just to wrap up loose ends.
                            }
                        }
                    }
                }
                
                if (selected == 0) {
                    // if nothing is selected, skip back to the top of the while()
                    continue;
                } else {
                    // something is selected. "touch" the Manager thread so it doesn't shut down.
                    lastEventTime = System.currentTimeMillis();
                }
                
                // iterate over selected keys
                /* restrict scope */ {
                    final Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    final Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    while (iterator.hasNext()) {
                        final SelectionKey key = iterator.next();
                        
                        // Call this key's callback. See PingImplementation.
                        final PingImplementation cb = (PingImplementation) key.attachment();
                        cb.onReadable(key);
                        
                        // Remove this key from the selection set.
                        iterator.remove();
                    }
                }
                
            } catch (IOException e) {
                // Could be thrown by selector.select
                PokeBot.getLogger().log(Level.SEVERE, "Selector.select() I/O error", e);
            }
        }
        
        // inform the Manager that the ManagerThread is shutting down
        callback.run();
    }
}
