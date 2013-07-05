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

import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.sender.Sender;

/**
 *
 * @author Joshua
 */
public class UCommand extends Listener {

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        Sender target = event.getChannel();
        if (target == null) {
            target = event.getSender();
        }
        if (target == null) {
            return;
        }
        if (event.getArgs().length == 0) {
            target.sendMessage(event.getSender().getNick() + ", " + "$u <user> [profile, posts, topics, warnings, videos, friends, pm, names, admin, edit, modcp, validate, warn, suspend, iphistory]");
        } else {
            String link = "http://u.mcf.li/" + event.getArgs()[0];
            if (event.getArgs().length >= 2) {
                link += "/" + event.getArgs()[1];
            }
            target.sendMessage(event.getSender().getNick() + ", " + link);
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "u"
        };
    }
}
