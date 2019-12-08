package backvisca.entity;

import java.util.List;

public class Macro {
    private int id;
    private String name;
    private int address;
    private List<Command> commands;

    public Macro(int id, String name, int address, List<Command> commands) {
        this.id = id;
        this.name = name;
        this.address = address;
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

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public void setID(int id) {
        this.id = id;
    }
}
