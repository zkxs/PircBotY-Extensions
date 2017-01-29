package net.ae97.pokebot.extensions.mcping.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.ae97.pokebot.extensions.mcping.PingException;
import net.ae97.pokebot.extensions.mcping.Server;
import net.ae97.pokebot.extensions.mcping.legacy.LegacyStatus;

public class Pinger {
    
    private Manager manager;
    
    public Pinger(Manager manager) {
        this.manager = manager;
    }
    
    public void ping(Server server) throws PingException {
        
        SocketChannel socketChannel = null;
        
        try {
            socketChannel = SocketChannel.open(server.getAddress());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        
        PingImplementationFactory factory = LegacyStatus::new;
        PingImplementation pingImpl = factory.construct(manager, socketChannel, this::callback);
        pingImpl.ping();
    }
    
    private void callback(PingResult pingResult) {
        System.out.println(pingResult.getMessage());
        System.exit(0);
    }
}
