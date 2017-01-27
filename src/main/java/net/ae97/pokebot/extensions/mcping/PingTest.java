package net.ae97.pokebot.extensions.mcping;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;

import javax.naming.NamingException;

public class PingTest {
    public static void main(String[] args) throws Exception {
        try {
            InetSocketAddress providedAddress = Server.parseHostPort("foo.ftb.michaelripley.net:2/howdy%20boi");
            Server server = new Server(providedAddress);

            server.isSrvBroken();
            server.isSrvRecord();

            System.out.println(server);
        } catch (URISyntaxException e) {
            System.err.println(e.getMessage()); // TODO: bot output
        } catch (NamingException e) {
            System.err.println(e.getMessage()); // TODO: bot output
            // TODO: log e
        }
    }
}
