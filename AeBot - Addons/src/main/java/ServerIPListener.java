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

import net.ae97.aebot.AeBot;
import net.ae97.aebot.api.EventField;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.Priority;
import net.ae97.aebot.api.channels.Channel;
import net.ae97.aebot.api.events.ActionEvent;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.events.MessageEvent;
import net.ae97.aebot.api.events.PartEvent;
import net.ae97.aebot.api.events.QuitEvent;
import net.ae97.aebot.api.users.BotUser;
import net.ae97.aebot.api.users.User;
import net.ae97.aebot.settings.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerIPListener extends Listener {

    protected final List<String> triggered = new ArrayList();
    protected final List<String> ignorePeople = new ArrayList();
    private final Set<String> channels = new HashSet<>();

    @Override
    public void onLoad() {
        channels.clear();
        channels.addAll(new Settings(new File("settings", "iplistener.yml")).getStringList("channels"));
    }

    @Override
    public void onUnload() {
        triggered.clear();
        ignorePeople.clear();
        channels.clear();
    }

    @Override
    @EventType(event = EventField.Message, priority = Priority.HIGH)
    public void runEvent(MessageEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String message = event.getMessage();
        User sender = event.getUser();
        Channel channel = event.getChannel();
        boolean silence = false;
        if (ignorePeople.contains(sender.getNick().toLowerCase()) || ignorePeople.contains(channel.getName().toLowerCase())) {
            return;
        }

        if (triggered.contains(sender.getNick().toLowerCase())) {
            silence = true;
        } else if (triggered.contains(event.getHostname().toLowerCase())) {
            silence = true;
        }
        String[] messageParts = message.split(" ");
        for (String part : messageParts) {
            if (isServer(part)) {
                if (!silence) {
                    channel.sendMessage(sender.getNick() + ", please do not advertise servers here");
                    triggered.remove(sender.getNick().toLowerCase());
                    triggered.remove(event.getHostname().toLowerCase());
                    triggered.add(sender.getNick().toLowerCase());
                    triggered.add(event.getHostname().toLowerCase());
                } else if (triggered.contains(sender.getNick().toLowerCase())) {
                    BotUser.getBotUser().kick(sender.getNick(), channel.getName(), "Server advertisement");
                    if (AeBot.getDebugMode()) {
                        BotUser.getBotUser().sendMessage(Settings.getGlobalSettings().getString("debug-channel"), event.getUser().getNick() + " triggered the ip censor with his line: " + event.getMessage());
                    }
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @Override
    @EventType(event = EventField.Action, priority = Priority.HIGH)
    public void runEvent(ActionEvent event) {
        if (!channels.contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String message = event.getAction();
        User sender = event.getUser();
        Channel channel = event.getChannel();
        boolean silence = false;
        if (ignorePeople.contains(sender.getNick().toLowerCase()) || ignorePeople.contains(channel.getName().toLowerCase())) {
            return;
        }

        if (triggered.contains(sender.getNick().toLowerCase())) {
            silence = true;
        } else if (triggered.contains(event.getUser().getIP().toLowerCase())) {
            silence = true;
        }
        String[] messageParts = message.split(" ");
        for (String part : messageParts) {
            if (isServer(part)) {
                if (!silence) {
                    channel.sendMessage(sender.getNick() + ", please do not advertise servers here");
                    triggered.remove(event.getUser().getIP().toLowerCase());
                    triggered.remove(event.getUser().getIP().toLowerCase());
                    triggered.add(sender.getNick().toLowerCase());
                    triggered.add(event.getUser().getIP().toLowerCase());
                } else if (triggered.contains(sender.getNick().toLowerCase())) {
                    BotUser.getBotUser().kick(sender.getNick(), channel.getName(), "Server advertisement");
                    if (AeBot.getDebugMode()) {
                        BotUser.getBotUser().sendMessage(Settings.getGlobalSettings().getString("debug-channel"), event.getUser().getNick() + " triggered the ip censor with his line: " + event.getAction());
                    }
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @Override
    @EventType(event = EventField.Part)
    public void runEvent(PartEvent event) {
        triggered.remove(event.getUser().getNick().toLowerCase());
    }

    @Override
    @EventType(event = EventField.Quit)
    public void runEvent(QuitEvent event) {
        triggered.remove(event.getUser().getNick().toLowerCase());
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        if (!event.getUser().hasOP(event.getChannel().getName())) {
            return;
        }
        if (event.getCommand().equalsIgnoreCase("ignoread")) {
            if (event.getArgs().length == 1) {
                ignorePeople.add(event.getArgs()[0].toLowerCase());
                event.getChannel().sendMessage("He will be ignored with IPs now");

            }
        } else if (event.getCommand().equalsIgnoreCase("unignoread")) {
            if (event.getArgs().length == 1) {
                if (ignorePeople.remove(event.getArgs()[0].toLowerCase())) {
                    event.getChannel().sendMessage("He will be not ignored with IPs now");
                }

            }
        } else if (event.getCommand().equalsIgnoreCase("reset")) {
            if (event.getArgs().length == 1) {
                if (triggered.remove(event.getArgs()[0].toLowerCase())) {
                    event.getChannel().sendMessage("His counter was removed");
                }

            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "reset",
            "unignoread",
            "ignoread"
        };
    }

    private boolean isServer(String testString) {
        String test = testString.toLowerCase().trim();

        String[] parts = split(test, ".");
        if (parts.length == 4) {
            if (parts[3].contains(":")) {
                parts[3] = parts[3].split(":")[0];
            }
            for (int i = 0; i < 4; i++) {
                try {
                    Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private String[] split(String message, String lookFor) {
        List<String> parts = new ArrayList<>();
        String test = message.toString();
        while (test.contains(lookFor)) {
            int id = test.indexOf(lookFor);
            if (id == -1) {
                break;
            }
            parts.add(test.substring(0, id));
            test = test.substring(id + 1);
        }
        parts.add(test);

        return parts.toArray(new String[0]);
    }
}