package net.ae97.pokebot.extensions.mcping.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import net.ae97.pokebot.extensions.mcping.PingException;
import net.ae97.pokebot.extensions.mcping.UnexpectedPingException;

public class Manager {
    private Selector selector;
    private Thread managerThread;
    private volatile boolean threadRunning;
    
    @FunctionalInterface
    public static interface ManagerCallback {
        public void onReadable(SelectionKey key, ByteBuffer receiveBuffer);
    };
    
    public Manager() throws IOException {
        selector = Selector.open();
        threadRunning = true;
        managerThread = new Thread(new ManagerThread(selector, this::threadClosedCallback));
        managerThread.setDaemon(false);
        managerThread.start();
    }

    /**
     * Calls {@link SelectableChannel#register(Selector, int)} with <code>channel</code> and <code>ops</code>.
     * @see SelectableChannel#register(Selector, int)
     * @param channel
     * @param ops
     * @throws ClosedChannelException
     */
    public SelectionKey registerChannel(SelectableChannel channel, int ops, ManagerCallback attachment)
            throws PingException {
        try {
            channel.configureBlocking(false);
            return channel.register(selector, ops, attachment);
        } catch (ClosedChannelException e) {
            throw new PingException("Connection closed unexpectedly", e);
        } catch (IOException e) {
            throw new UnexpectedPingException(e);
        }
    }
    
    private void threadClosedCallback() {
        threadRunning = false;
        // We can't just restart the thread here, as that would keep things from being garbage collected.
        // Therefore, we restart it next time we see a command.
    }
}
