package backvisca;

import backvisca.entity.Command;
import backvisca.entity.Commands;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

@Component
public class ViscaService {

    private static SerialPort serialPort;

    private static Map<String, byte[]> availableCommands;

    private static final byte COMMAND_ACCEPTED = 65;
    private static final byte COMMAND_EXECUTED = 81;

    static {
        byte[] up = {1,6,1,5,5,3,1};
        byte[] down = {1,6,1,5,5,3,2};
        byte[] left = {1,6,1,5,5,1,3};
        byte[] right = {1,6,1,5,5,2,3};
        availableCommands = new HashMap<>();
        availableCommands.put("up", up);
        availableCommands.put("down", down);
        availableCommands.put("left", left);
        availableCommands.put("right", right);
    }

    public ViscaService() {
//        setUpSerial();
    }

    private void setUpSerial(){
        try {
            serialPort = new SerialPort("COM5");
            serialPort.openPort();
            serialPort.setParams(9600, 8,1,0);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    void sendCommands(Commands commands){
        new Thread(() -> {
            handleCommands(commands);
        }).start();
    }

    void handleCommands(Commands commands) {
        int address = commands.getAddress();
        for (Command command : commands.getCommands()) {
            try {
                boolean response = false;
                if (command.getCommand().equals("wait")) {
                    sleep(command.getSpeed());
                    continue;
                }
                while (!response) {
                    byte[] byteCommand = availableCommands.get(command.getCommand());
                    setSpeed(byteCommand, command.getSpeed());
                    sendCommand(byteCommand, address);
                    if (readResponse())
                        response = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setSpeed(byte[] data, int speed) {
        try {
            data[3] = (byte) speed;
            data[4] = (byte) speed;
        }catch (NumberFormatException | NullPointerException exp){
            System.out.println("Default speed 5");
        }
    }

    private void sendCommand(byte[] command, int address){
        byte[] data = createData(command, (byte) 0, (byte) address);
        try {
            serialPort.writeBytes(data);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

    }

    private byte[] createData(byte[] command, byte sourceAdr, byte destinationAdr) {
        byte[] data = new byte[command.length + 1 + 1];
        byte head = (byte)( 128 | (sourceAdr << 4) |  destinationAdr);
        byte tail = -1;
        System.arraycopy(command, 0 , data,1, command.length);
        data[0]=head;
        data[command.length + 1] = tail;
        StringBuilder builder = new StringBuilder();
        for(byte b: command){
            builder.append(String.format("%02X ", b));
        }
        System.out.println(builder.toString());
        return data;
    }

    private boolean readResponse() {
        try {
            for (byte b : ViscaResponseReader.readResponse(serialPort)) {
                System.out.print(b);
                switch (b){
                    case COMMAND_ACCEPTED:
                        System.out.println("COMMAND ACCEPTED");
                        break;
                    case COMMAND_EXECUTED:
                        System.out.println("COMMAND EXECUTED");
                        return true;
                }
            }
        } catch (ViscaResponseReader.TimeoutException | SerialPortException e) {
            e.printStackTrace();
        }
        return false;
    }
}
