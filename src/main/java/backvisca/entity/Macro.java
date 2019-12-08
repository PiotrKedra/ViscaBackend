package backvisca.entity;

import java.util.List;

public class Macro {
    private int id;
    private String name;
    private List<Command> commands;

    public Macro(int id, String name, List<Command> commands) {
        this.id = id;
        this.name = name;
        this.commands = commands;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setID(int id) {
        this.id = id;
    }
}
