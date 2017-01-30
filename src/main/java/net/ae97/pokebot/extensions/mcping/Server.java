package net.ae97.pokebot.extensions.mcping;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import jline.internal.Nullable;

/**
 * Represents the server we will attempt to query
 * @author zkxs (zkxs00@gmail.com)
 */
public class Server {

    // default ports
    private static final int DEFAULT_SERVER_PORT = 25565;
    private static final int DEFAULT_QUERY_PORT = 25565;
    private static final int DEFAULT_RCON_PORT = 25575;

    /* ******************************* End of static stuff, beginning of fields ******************************* */

    private InetSocketAddress address;
    private boolean isSrvBroken = false; // was there some broken SRV stuff?
    private SrvRecord srvRecord; // if non-null, then this is what we're using

    public Server(String hostPort) throws NamingException, URISyntaxException {
        this(Server.parseHostPort(hostPort));
    }
    
    public Server(InetSocketAddress providedAddress) throws NamingException {
        
        /* The following gross conditional statements determine if the provided address:
         * 1. Is a hostname or IP
         * 2. If the hostname has a SRV record
         * 3. If the hostname has a broken SRV record
         * 
         * It then sets this.address to the appropriate address to actually try connecting to
         */
        
        final String ipAddress = getIp(providedAddress.getAddress());
        
        // check if we were given an ip address
        if (ipAddress == null || !ipAddress.equals(providedAddress.getHostString())) {
            // we were given a host name
            try {
                srvRecord = SrvRecord.resolveSRV(providedAddress.getHostString());
    
                if (srvRecord == null) {
                    // indicates that _minecraft._tcp.host exists but doesn't have a SRV record!?
                    // this means someone tried to make a SRV record, but it's not actually a SRV
                    // could be a CNAME or something...
                    
                    // We'll log it and fall back to the provided address
                    //TODO: log it
                    this.address = providedAddress;
                    isSrvBroken = true;
                } else {
                    // looks like a valid SRV record!
                    this.address = new InetSocketAddress(srvRecord.getTarget(), srvRecord.getPort());
                }
    
            } catch (NameNotFoundException e) {
                // indicates that there is no SRV record, which is fine.
                // we'll just use the provided address
                this.address = providedAddress;
            }
        } else {
            // we were given an ip address
            this.address = providedAddress;
        }
    }
    
    /**
     * Check if we're using a SRV record
     * @return true if this server's address came from a SRV record
     */
    public boolean isSrvRecord() {
        return srvRecord != null;
    }
    
    /**
     * Check if a broken SRV record was found
     * @return <code>true</code> if a broken SRV record was found
     */
    public boolean isSrvBroken() {
        return isSrvBroken;
    }
    
    /**
     * Get the address of this server, taking SRV records into account
     * @return The address of this server
     */
    public InetSocketAddress getAddress() {
        return address;
    }
    
    @Override
    public String toString() {
        return address.getHostString() + ":" + address.getPort();
    }

    /* ************************* End of object stuff, beginning of static util functions ************************* */

    /**
     * Get the IP address (e.g. "8.8.8.8") from an InetAddress
     * @param address the InetAddress
     * @return IPv4 or IPv6 address corresponding to <code>address</code>
     */
    @Nullable
    private String getIp(InetAddress address) {
        if (address == null) {
            return null;
        } else {
            return address.toString().split("/")[1];
        }
    }
    
    /**
     * Parse host-port strings
     * 
     * @param hostPort
     *            A string of the format "example.com" or example.com:25565"
     * @return an InetSocketAddress with the appropriate host and port
     * @throws URISyntaxException
     *             If the URI is invalid
     */
    public static InetSocketAddress parseHostPort(String hostPort) throws URISyntaxException {
        URI uri = new URI("minecraft://" + hostPort);
        final String host = uri.getHost();
        int port = uri.getPort();

        if (host == null) {
            throw new URISyntaxException(uri.toString(), "URI must have a host part");
        }

        // check if the URI actually has a port in it
        if (port == -1) {
            port = DEFAULT_SERVER_PORT;
        }

        return new InetSocketAddress(host, port);
    }

}
