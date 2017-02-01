package net.ae97.pokebot.extensions.mcping.connection;

import java.util.LinkedList;
import java.util.List;

public class JsonPingSuccess implements PingResult {

    private List<String> response = new LinkedList<>();
    
    public JsonPingSuccess(String json) {
        response.add(json);
    }
    
    @Override
    public List<String> getMessage() {
        return response;
    }

}
