package net.ae97.pokebot.extensions.mcping;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.naming.NamingException;

import net.ae97.pokebot.extensions.mcping.connection.Manager;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.UnexpectedPingException;

public class PingTest {
    public static void main(String[] args) throws InterruptedException {
        try {
            Manager manager = new Manager();

            Server server1 = new Server("localhost");
            Server server2 = new Server("mc.michaelripley.net");

            server1.isSrvBroken();
            server1.isSrvRecord();

            System.out.println(server2);
            manager.ping(server2);
            
            Thread.sleep(11000);

            System.out.println(server1);
            manager.ping(server1);

        } catch (URISyntaxException e) {
            System.err.println(e.getMessage()); // TODO: bot output
        } catch (NamingException e) {
            System.err.println(e.getMessage()); // TODO: bot output
            // TODO: log e
        } catch (UnexpectedPingException e) {
            System.err.println(e.getMessage()); // TODO: bot output
            // TODO: log e
        } catch (PingException e) {
            System.err.println(e.getMessage()); // TODO: bot output
        } catch (IOException e) {
            System.err.println(e.getMessage()); // TODO: bot output
            // TODO log e
        }
    }
}
