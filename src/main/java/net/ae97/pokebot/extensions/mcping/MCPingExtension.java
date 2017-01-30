/*
 * Copyright (C) 2016 Joshua
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ae97.pokebot.extensions.mcping;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javax.naming.NamingException;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionLoadFailedException;
import net.ae97.pokebot.extensions.mcping.connection.Manager;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.PingException;
import net.ae97.pokebot.extensions.mcping.pings.exceptions.UnexpectedPingException;

/**
 *
 * @author Joshua
 */
public class MCPingExtension extends Extension implements CommandExecutor {

    private Manager manager;
    
    @Override
    public void load() throws ExtensionLoadFailedException {
        PokeBot.getEventHandler().registerCommandExecutor(this);
        try {
            manager = new Manager();
        } catch (IOException e) {
            PokeBot.getLogger().log(Level.SEVERE, "Error starting McPing Manager", e);
        }
    }
    
    @Override
    public void runEvent(CommandEvent ce) {
        if (ce.getArgs().length != 1) {
            ce.getUser().send().notice("Usage: mcping <server ip>[:port]");
            return;
        }
        
        try {
            Server server = new Server(ce.getArgs()[0]);
            manager.ping(ce, server);
        } catch (URISyntaxException e) {
            ce.respond("Error: " + e.getMessage());
        } catch (NamingException e) {
            ce.respond("Error: " + e.getMessage());
            PokeBot.getLogger().log(Level.WARNING, "Error during DNS query", e);
        } catch (UnexpectedPingException e) {
            ce.respond("Error: " + e.getMessage());
            PokeBot.getLogger().log(Level.WARNING, "Error during ping", e);
        } catch (PingException e) {
            ce.respond("Error: " + e.getMessage());
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "mcping"
        };
    }

    @Override
    public String getName() {
        return "mcping";
    }
}
