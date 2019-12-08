package backvisca.entity;

import java.util.List;

public class Commands {

    private int address;
    private List<Command> commands;

    public Commands() {
    }

    public Commands(int address, List<Command> commands) {
        this.address = address;
        this.commands = commands;
    }

    public int getAddress() {
        return address;
    }

    public List<Command> getCommands() {
        return commands;
    }
}
