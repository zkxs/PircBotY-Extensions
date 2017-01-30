package net.ae97.pokebot.extensions.mcping;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import jline.internal.Nullable;

import java.util.regex.Pattern;
import java.util.Hashtable;
import java.util.regex.Matcher;

public class SrvRecord {

    private static final String SRV_PREFIX = "_minecraft._tcp."; // DNS SRV query prefix
    private static final String[] DESIRED_RECORD = { "SRV" }; // desired DNS records

    // environment used for making DNS queries
    private static final Hashtable<String, String> DNS_ENVIRONMENT = new Hashtable<>();

    // regex for matching SRV responses
    private static final Pattern srvPattern = Pattern.compile("(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(.*)\\.");
    
    static {
        // Seems to work fine on OpenJDK, despite being "com.sun..."
        DNS_ENVIRONMENT.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        // use system DNS settings
        DNS_ENVIRONMENT.put(Context.PROVIDER_URL, "dns:");
    }
    
    // standard SRV record fields
    private int priority;
    private int weight;
    private int port;
    private String target;

    /**
     * Construct SrvRecord from the response of a SRV query
     * @param srvRecord the response of a SRV query
     * @throws NamingException if the response was invalid
     */
    private SrvRecord(String srvRecord) throws NamingException {
        Matcher matcher = srvPattern.matcher(srvRecord);
        if (matcher.matches()) {
            priority = Integer.parseInt(matcher.group(1));
            weight = Integer.parseInt(matcher.group(2));
            port = Integer.parseInt(matcher.group(3));
            target = matcher.group(4);
        } else {
            throw new NamingException(String.format("\"%s\" is not a valid SRV record", srvRecord));
        }
    }

    public int getPriority() {
        return priority;
    }

    public int getWeight() {
        return weight;
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return String.format("SRV %d %d %d %s.", priority, weight, port, target);
    }
    
    /**
     * Resolve a SRV record
     * @param host something like "mc.michaelripley.net"
     * @return a SrvRecord object, or null if there was no SRV record
     * @throws NamingException If the SRV record was broken or invalid in some way
     */
    @Nullable
    public static SrvRecord resolveSRV(String host) throws NamingException {
        final DirContext ctx = new InitialDirContext(DNS_ENVIRONMENT);
        final Attributes attrs = ctx.getAttributes(SRV_PREFIX + host, DESIRED_RECORD);
        final Attribute attr = attrs.get("srv"); // might be null if host exists but has no SRV record
        if (attr == null) {
            return null;
        } else {
            return new SrvRecord((String) attr.get());
        }
    }
}
