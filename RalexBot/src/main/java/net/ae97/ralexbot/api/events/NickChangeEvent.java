/*
 * Copyright (C) 2013 Lord_Ralex
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
package net.ae97.ralexbot.api.events;

import net.ae97.ralexbot.api.users.User;

public class NickChangeEvent implements UserEvent, CancellableEvent {

    private final String oldNick, newNick;
    private final User sender;
    private boolean isCancelled = false;

    public NickChangeEvent(org.pircbotx.hooks.events.NickChangeEvent event) {
        oldNick = event.getOldNick();
        newNick = event.getNewNick();
        sender = new User(event.getUser());
    }

    public String getOldNick() {
        return oldNick;
    }

    public String getNewNick() {
        return newNick;
    }

    @Override
    public User getUser() {
        return sender;
    }

    @Override
    public void setCancelled(boolean state) {
        isCancelled = state;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
}