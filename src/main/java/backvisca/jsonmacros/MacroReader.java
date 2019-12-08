package backvisca.jsonmacros;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import backvisca.entity.Command;
import backvisca.entity.Macro;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

@Component
public class MacroReader {

    public List<Macro> getAll() {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("C:\\Users\\piotr\\IdeaProjects\\backvisca\\src\\main\\resources\\makrs.json"))
        {
            Object obj = jsonParser.parse(reader);

            JSONArray commandsList = (JSONArray) obj;
            System.out.println(commandsList);

            return mapToMacros(commandsList);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return new ArrayList<Macro>();
    }

    @SuppressWarnings("unchecked")
    private List<Macro> mapToMacros(JSONArray employeeList) {
        return (List<Macro>) employeeList.stream()
                .map(emp -> parseMacroObject((JSONObject) emp))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Macro parseMacroObject(JSONObject employee) {
        JSONObject macro = (JSONObject) employee.get("macro");

        JSONArray commandsArray = (JSONArray) macro.get("commands");

        List<Command> commands  = (List<Command>) commandsArray.stream()
                .map(object -> mapToCommand((JSONObject)object))
                .collect(Collectors.toList());
        int id = (int)((long)macro.get("id"));
        String name = (String) macro.get("name");
        int address = (int)((long)macro.get("address"));


        return new Macro(id, name, address, commands);
    }

    private Command mapToCommand(JSONObject object) {
        String command = (String) object.get("command");
        int speed = (int)((long)object.get("speed"));
        return new Command(command, speed);
    }
}
