package net.ae97.pokebot.extensions.mcping.pings.impl;

import java.nio.channels.SelectionKey;

import net.ae97.pokebot.extensions.mcping.connection.PingResultCallback;
import net.ae97.pokebot.extensions.mcping.pings.PingImplementation;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;

public class ServerListPing implements PingImplementation {

    @Override
    public void ping() throws PingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReadable(SelectionKey key) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getLastActivityTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public PingResultCallback getCallback() {
        // TODO Auto-generated method stub
        return null;
    }

}
