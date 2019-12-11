package backvisca;

import backvisca.entity.Command;
import backvisca.entity.Commands;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

@Component
public class ViscaService {

    private static SerialPort serialPort;

    private static Map<String, byte[]> availableCommands;

    private static final byte COMMAND_ACCEPTED = 65;
    private static final byte COMMAND_EXECUTED = 81;

    private static byte lastResponse = 0;
    private static List<String> commandStatuses = new ArrayList<>();

    private static final byte END_OF_COMMAND = -1;

    static {
        byte[] up = {1, 6, 1, 5, 5, 3, 1};
        byte[] down = {1, 6, 1, 5, 5, 3, 2};
        byte[] left = {1, 6, 1, 5, 5, 1, 3};
        byte[] right = {1, 6, 1, 5, 5, 2, 3};
        byte[] change = {48,1};
        byte[] home = {1, 6, 4};
        byte[] reset = {1, 6, 5};
        availableCommands = new HashMap<>();
        availableCommands.put("up", up);
        availableCommands.put("down", down);
        availableCommands.put("left", left);
        availableCommands.put("right", right);
        availableCommands.put("change", change);
        availableCommands.put("home", home);
        availableCommands.put("reset", reset);
    }

    public ViscaService() {
        setUpSerial();
    }

    private void setUpSerial() {
        try {
            serialPort = new SerialPort("COM5");
            serialPort.openPort();
            serialPort.setParams(9600, 8, 1, 0);

            sleep(2000);
            for (int i = 1; i <= 8; i++) {
                sendCommand(availableCommands.get("reset"), i);
            }
        } catch (SerialPortException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    List<String> sendCommands(Commands commands) {
//        new Thread(() -> {
//            handleCommands(commands);
//        }).start();
        return handleCommands(commands);
    }

    private int setAddress = 1;
    private int oldAddress = 1;

    List<String> handleCommands(Commands commands) {
        commandStatuses = new ArrayList<>();
        int address = commands.getAddress();
        for (Command command : commands.getCommands()) {
            try {
                boolean response = false;
                if (handleHome(address, command)) break;
                if (handleChangeAddress(command)) continue;
                if (handleWait(command)) continue;
                boolean send = false;
                while (!response) {
                    if (!send) {
                        byte[] byteCommand = availableCommands.get(command.getCommand());
                        setSpeed(byteCommand, (int)(command.getSpeed()*20.0/100.0));
                        sendCommand(byteCommand, address);
                        send = true;
                    }
                    if(address!=setAddress){
                        commandStatuses.add("WRONG CAMERA");
                        break;
                    }
                    if (readResponse())
                        response = true;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            oldAddress=address;
        }
        return commandStatuses;
    }

    private boolean handleHome(int address, Command command) {
        if (command.getCommand().equals("home")){
            byte[] home = availableCommands.get("home");
            sendCommand(home, address);
            commandStatuses.add("COMMAND EXECUTED(HOME)");
            return true;
        }
        return false;
    }

    private boolean handleChangeAddress(Command command) {
        if (command.getCommand().equals("change")) {
            byte[] changes = availableCommands.get("change");
            changes[1] = (byte) command.getSpeed();
            sendCommand(changes, oldAddress);
            setAddress = command.getSpeed();
            commandStatuses.add("ADDRESS CHANGED: " + command.getSpeed());
            return true;
        }
        return false;
    }

    private boolean handleWait(Command command) throws InterruptedException {
        if (command.getCommand().equals("wait")) {
            sleep(command.getSpeed()*1000);
            return true;
        }
        return false;
    }

    private void setSpeed(byte[] data, int speed) {
        try {
            data[3] = (byte) speed;
            data[4] = (byte) speed;
        } catch (NumberFormatException | NullPointerException exp) {
            System.out.println("Default speed 5");
        }
    }

    private void sendCommand(byte[] command, int address) {
        byte[] data = createData(command, (byte) 0, (byte) address);
        try {
            serialPort.writeBytes(data);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

    }

    private byte[] createData(byte[] command, byte sourceAdr, byte destinationAdr) {
        byte[] data = new byte[command.length + 1 + 1];
        byte head = (byte) (128 | (sourceAdr << 4) | destinationAdr);
        byte tail = -1;
        System.arraycopy(command, 0, data, 1, command.length);
        data[0] = head;
        data[command.length + 1] = tail;
        StringBuilder builder = new StringBuilder();
        for (byte b : command) {
            builder.append(String.format("%02X ", b));
        }
        System.out.println(builder.toString());
        return data;
    }

    private boolean readResponse() {
        try {

            System.out.println("przed komenda");
            byte[] responses = ViscaResponseReader.readResponse(serialPort);
            System.out.println("po:" + responses);
            for (byte b : responses) {
                if (b == END_OF_COMMAND) {
                    if (lastResponse >= 81 && lastResponse <= 88) {
                        System.out.println("COMMAND EXECUTED");
                        commandStatuses.add("COMMAND EXECUTED");
                        return true;
                    }

                }
                if (lastResponse == 48 && b == 1){
                    System.out.println("NOT KNOWN RESPONSE");
                    commandStatuses.add("NOT KNOWN RESPONSE");
                    return true;
                }
                lastResponse = b;

            }
        } catch (ViscaResponseReader.TimeoutException | SerialPortException e) {
            e.printStackTrace();
            commandStatuses.add("NO CAMERA FOUND");
            return true;
        }
        return false;
    }
}
