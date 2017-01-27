package net.ae97.pokebot.extensions.mcping;

import javax.naming.NamingException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SrvRecord {

    private static final Pattern srvPattern = Pattern.compile("(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(.*)\\.");

    private int priority;
    private int weight;
    private int port;
    private String target;

    public SrvRecord(String srvRecord) throws NamingException {
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
}
