package net.ae97.pokebot.extensions.mcping.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import net.ae97.pokebot.extensions.mcping.PingException;
import net.ae97.pokebot.extensions.mcping.Server;
import net.ae97.pokebot.extensions.mcping.UnexpectedPingException;
import net.ae97.pokebot.extensions.mcping.legacy.LegacyStatus;

public class Manager {
    private Selector selector;
    private Thread managerThread;
    private volatile boolean threadRunning = false;
    
    @FunctionalInterface
    public static interface ManagerCallback {
        public void onReadable(SelectionKey key, ByteBuffer receiveBuffer);
    };
    
    public Manager() throws IOException {
        selector = Selector.open();
        startManager();
    }
    
    private synchronized void startManager() {
        if (!threadRunning) {
            threadRunning = true;
            managerThread = new Thread(new ManagerThread(selector, this::threadClosedCallback), "McPing-Selector");
            managerThread.setDaemon(false);
            System.out.println("MANAGER STARTING UP"); //TODO: remove
            managerThread.start();
        }
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
            selector.wakeup();
            System.out.println("REG...");
            final SelectionKey key = channel.register(selector, ops, attachment);
            System.out.println("...DONE");
            startManager();
            return key;
        } catch (ClosedChannelException e) {
            throw new PingException("Connection closed unexpectedly", e);
        } catch (IOException e) {
            throw new UnexpectedPingException(e);
        }
    }
    
    public void ping(Server server) throws PingException {
        
        SocketChannel socketChannel = null;
        
        try {
            socketChannel = SocketChannel.open(server.getAddress());
        } catch (IOException e) {
            throw new PingException(e);
        }
        
        PingImplementationFactory factory = LegacyStatus::new;
        PingImplementation pingImpl = factory.construct(this, socketChannel, this::callback);
        pingImpl.ping();
    }
    
    private void callback(PingResult pingResult) {
        System.out.println(pingResult.getMessage());
        
        //TODO: don't exit
        //System.out.println("EXITING");
        //System.exit(0);
    }
    
    private void threadClosedCallback() {
        threadRunning = false;
        System.out.println("MANAGER SHUTTING DOWN"); //TODO: remove
        // We can't just restart the thread here, as that would keep things from being garbage collected.
        // Therefore, we restart it next time we see a command.
    }
}
