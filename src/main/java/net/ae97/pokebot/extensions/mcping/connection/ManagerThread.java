package net.ae97.pokebot.extensions.mcping.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import net.ae97.pokebot.extensions.mcping.connection.Manager.ManagerCallback;

public class ManagerThread implements Runnable {
    private static final long SELECTOR_CLOSE_TIMEOUT = 30000L;
    private static final long SELECTOR_SELECT_TIMEOUT = 500L;
    private static final int MAX_PACKET_SIZE = 65535;
    
    private Selector selector;
    private Runnable callback;
    private long lastEventTime;
    private ByteBuffer receiverBuffer = ByteBuffer.allocate(65535);
    
    public ManagerThread(Selector selector, Runnable callback) {
        this.selector = selector;
        this.callback = callback;
    }
    
    @Override
    public void run() {
        lastEventTime = System.currentTimeMillis();
        
        // while the selector has had recent events
        while (System.currentTimeMillis() - lastEventTime < SELECTOR_CLOSE_TIMEOUT) {
            try {
                final int selected = selector.select(SELECTOR_SELECT_TIMEOUT);
                System.out.print("s");
                
                if (selected == 0) {
                    continue;
                } else {
                    lastEventTime = System.currentTimeMillis();
                }
                
                final Set<SelectionKey> selectedKeys = selector.selectedKeys();
                final Iterator<SelectionKey> iterator = selectedKeys.iterator();
                
                while (iterator.hasNext()) {
                    final SelectionKey key = iterator.next();
                    
                    key.cancel();
                    final ManagerCallback cb = (ManagerCallback) key.attachment();
                    cb.onReadable(key, receiverBuffer);
                    
                    iterator.remove();
                }
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        callback.run();
    }
}
