package net.ae97.pokebot.extensions.mcping.connection;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extensions.mcping.Server;
import net.ae97.pokebot.extensions.mcping.pings.PingImplementation;
import net.ae97.pokebot.extensions.mcping.pings.PingImplementationFactory;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.UnexpectedPingException;
import net.ae97.pokebot.extensions.mcping.pings.impl.LegacyStatus;

/**
 * Keeps track of all ongoing pings. Each ping consists of a PingImplementation. First, the PingImplementation's
 * ping() method is called, from the same thread that calls Manager.ping(). Then the PingImplementation is responsible
 * for calling registerChannel() to mark the channel as ready for reading.
 * 
 * Reading occurs in a ManagerThread. Each manager has at either 0 or 1 manager threads. Manager threads
 * are automatically shut down after a period of inactivity. This is to allow the extension to be garbage collected if
 * it has been unloaded. When the ManagerThread finds a readable channel, it calls onReadable() for the associated
 * PingImplementation.
 * 
 * The manager also periodically check to see if a ping has hung, and if it has closes its
 * connection. Pings can hang if they are made to a server that is not a minecraft server. This can be reproduced by
 * pinging a netcat server. 
 */
public class Manager {
    
    /** Multiplexes SocketChannels that we are reading from */
    private Selector selector;
    
    /** Is the manager thread running? */
    private volatile boolean threadRunning = false;
    
    /** Used for avoiding select/register race condition */
    private Lock lock = new ReentrantLock();
    
    /** Used to reuse large-ish bytebuffers */
    private MemoryManager memoryManager = new MemoryManager();
    
    public Manager() throws IOException {
        selector = Selector.open();
    }
    
    /**
     * Start the manager thread, if it is not already running.
     * This is synchronized to prevent two manager threads from being started.
     */
    private synchronized void startManager() {
        if (!threadRunning) {
            threadRunning = true;
            final Thread managerThread = new Thread(new ManagerThread(selector, this::threadClosedCallback, lock),
                    "McPing-Selector-" + System.currentTimeMillis());
            managerThread.setDaemon(true);
            PokeBot.getLogger().log(Level.INFO, "McPing Manager starting up");
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
    public SelectionKey registerChannel(SelectableChannel channel, int ops, PingImplementation attachment)
            throws PingException {
        try {
            channel.configureBlocking(false);
            
            
            /* channel.register() is dumb and blocks while selector.select() is blocking. This is bad because
             * selector.select() is blocking most of the time, and we WANT it to be to avoid constant polling.
             * 
             * We can sort of work around this by calling selector.wakeup() to force selector.select() to return.
             * However, this creates a race condition where sometimes selector.select() will be called again
             * before we are able to call channel.register().
             * 
             * The lock here is to prevent the ManagerThread from continuing execution until we have finished
             * registering the new channel.
             */
            
            lock.lock();
            selector.wakeup();
            final SelectionKey key = channel.register(selector, ops, attachment);
            lock.unlock();
            
            // start the manager, if it is not already started
            startManager();
            return key;
        } catch (ClosedChannelException e) {
            throw new PingException("Connection closed unexpectedly", e);
        } catch (IOException e) {
            throw new UnexpectedPingException(e);
        }
    }
    
    /**
     * Initiate a ping to server
     * @param commandEvent
     * @param server
     * @throws PingException
     */
    public void ping(CommandEvent commandEvent, Server server) throws PingException {
        
        SocketChannel socketChannel = null;
        
        try {
            socketChannel = SocketChannel.open(server.getAddress());
        } catch (UnresolvedAddressException e) {
            throw new PingException("Address Not resolved: " + server.getAddress().toString(), e);
        } catch (IOException e) {
            throw new PingException(e.getMessage(), e);
        }
        
        PingImplementationFactory factory = LegacyStatus::new;
        PingImplementation pingImpl = factory.construct(this, socketChannel, new PingResultCallback(commandEvent, server));
        pingImpl.ping();
    }
    
    /**
     * Called by the {@link ManagerThread} when this thread is closed.
     */
    private void threadClosedCallback() {
        threadRunning = false;
        PokeBot.getLogger().log(Level.INFO, "McPing Manager shutting down");
        // We can't just restart the thread here, as that would keep things from being garbage collected.
        // Therefore, we restart it next time we see a command.
    }
    
    /**
     * Get this Manager's MemoryManager instance
     * @return this Manager's MemoryManager instance
     */
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }
}
