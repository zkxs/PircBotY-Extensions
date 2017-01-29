package net.ae97.pokebot.extensions.mcping.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import net.ae97.pokebot.extensions.mcping.connection.Manager.ManagerCallback;

public class ManagerThread implements Runnable {
    
    private static final long SELECTOR_TIMEOUT = Long.MAX_VALUE;
    private static final int MAX_PACKET_SIZE = 65535;
    
    private Selector selector;
    private Runnable callback;
    private long lastEventTime = System.currentTimeMillis();
    private ByteBuffer receiverBuffer = ByteBuffer.allocate(65535);
    
    public ManagerThread(Selector selector, Runnable callback) {
        this.selector = selector;
        this.callback = callback;
    }
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
        // while the selector has had recent events
        while (System.currentTimeMillis() - lastEventTime < SELECTOR_TIMEOUT) {
            try {
                final int selected = selector.select(SELECTOR_TIMEOUT);
                
                if (selected == 0) {
                    continue;
                } else {
                    lastEventTime = System.currentTimeMillis();
                }
                
                final Set<SelectionKey> selectedKeys = selector.selectedKeys();
                final Iterator<SelectionKey> iterator = selectedKeys.iterator();
                
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    
                    if (key.isReadable()) {
                        // TODO: 
                        final ManagerCallback cb = (ManagerCallback) key.attachment();
                        cb.onReadable(key, receiverBuffer);
                    } else {
                        // TODO: what is this?
                        System.err.println("NOT READABLE");
                    }
                    
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
