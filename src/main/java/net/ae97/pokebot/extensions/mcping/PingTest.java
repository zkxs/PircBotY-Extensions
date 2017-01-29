package net.ae97.pokebot.extensions.mcping;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;

import javax.naming.NamingException;

import net.ae97.pokebot.extensions.mcping.connection.Manager;
import net.ae97.pokebot.extensions.mcping.connection.Pinger;

public class PingTest {
    public static void main(String[] args) {
        try {
            Manager manager = new Manager();
            
            InetSocketAddress providedAddress = Server.parseHostPort("michaelripley.net:22");
            Server server = new Server(providedAddress);

            server.isSrvBroken();
            server.isSrvRecord();
            
            System.out.println(server);
            
            Pinger pinger = new Pinger(manager);
            pinger.ping(server);
            
            
            InetSocketAddress addr2 = Server.parseHostPort("mc.michaelripley.net");
            Server server2 = new Server(addr2);
            pinger.ping(server);
            

        } catch (URISyntaxException e) {
            System.err.println(e.getMessage()); // TODO: bot output
        } catch (NamingException e) {
            System.err.println(e.getMessage()); // TODO: bot output
            // TODO: log e
        } catch (UnexpectedPingException e) {
            e.printStackTrace(); // TODO: bot output
            // TODO: log e
        } catch (PingException e) {
            e.printStackTrace(); // TODO: bot output
        } catch (IOException e) {
            e.printStackTrace(); // TODO bot output
            // TODO log e
        }
    }
}
