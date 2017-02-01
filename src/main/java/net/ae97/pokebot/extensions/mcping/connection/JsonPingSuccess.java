package net.ae97.pokebot.extensions.mcping.connection;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JsonPingSuccess implements PingResult {

    private List<String> lines = new LinkedList<>();

    public JsonPingSuccess(String json) {
        final Gson gson = new Gson();
        try {
            final Response response = gson.fromJson(json, Response.class);

            lines.add(String.format("Found \"%s\" running version %s with %d/%d players", response.description.text,
                    response.version.name, response.players.online, response.players.max));

        } catch (JsonSyntaxException e) {
            lines.add("Invalid JSON in server response");
        }
    }

    @Override
    public List<String> getMessage() {
        return lines;
    }

    private static final class Response {
        Version version;
        Players players;
        Description description;
        String favicon;
    }

    private static final class Version {
        String name;
        String protocol;
    }

    private static final class Players {
        Integer max;
        Integer online;
        Player[] sample;
    }

    private static final class Player {
        String name;
        String id;
    }

    private static final class Description {
        String text;
    }

}
