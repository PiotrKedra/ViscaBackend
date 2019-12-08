package backvisca.jsonmacros;

import backvisca.entity.Command;
import backvisca.entity.Macro;
import backvisca.jsonmacros.MacroReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MacroWriter {

    private static int lastID = 0;

    @Autowired
    private MacroReader macroReader;

    @SuppressWarnings("unchecked")
    public Macro save(Macro macro) {
        List<Macro> allMacros = macroReader.getAll();
        allMacros.add(macro);

        lastID = 0;
        List<JSONObject> jsonMacros = allMacros.stream()
                .map(this::mapToJSON)
                .collect(Collectors.toList());


        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(jsonMacros);

        try {
            Files.write(Paths.get("C:\\Users\\piotr\\IdeaProjects\\backvisca\\src\\main\\resources\\makrs.json"), jsonArray.toJSONString().getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        macro.setID(lastID);
        return macro;
    }

    @SuppressWarnings("unchecked")
    private JSONObject mapToJSON(Macro macro){
        JSONObject object = new JSONObject();
        object.put("id", lastID + 1);
        object.put("name", macro.getName());
        JSONArray commandsArray = getCommandsJSONArray(macro);
        object.put("commands", commandsArray);
        ++lastID;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("macro", object);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    private JSONArray getCommandsJSONArray(Macro macro) {
        List<JSONObject> allCommands = macro.getCommands().stream().map(this::mapToCommandJSON).collect(Collectors.toList());
        JSONArray commandsArray = new JSONArray();
        commandsArray.addAll(allCommands);
        return commandsArray;
    }

    @SuppressWarnings("unchecked")
    private JSONObject mapToCommandJSON(Command command) {
        JSONObject object = new JSONObject();
        object.put("command", command.getCommand());
        object.put("speed", command.getSpeed());

        return object;
    }
}
