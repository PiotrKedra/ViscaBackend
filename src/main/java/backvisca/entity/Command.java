package backvisca.entity;

public class Command {

    private String command;
    private int speed;

    public Command() {
    }

    public Command(String command, int speed) {
        this.command = command;
        this.speed = speed;
    }

    public String getCommand() {
        return command;
    }

    public int getSpeed() {
        return speed;
    }
}
