package General_Purpose_IDE.Packages;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Package(description = "Instructions to interface with the COM ports. Can open/close connections, send/receive data")
public class Comport extends InstructionPackage {
    private static SerialPort[] ports;
    private static List<SerialPort> openPorts = new ArrayList<SerialPort>();

    @Instruction(description="Prints out a list of available COM ports")
    static String[] getPorts(@Param(Name = "printToConsole", Default = "true") boolean printToConsole) {
        return getPorts(printToConsole, true);
    }

    @Instruction(description="Prints out a list of available COM ports")
    static String[] getPorts(@Param(Name = "printToConsole", Default = "true") boolean printToConsole, @Param(Name = "descriptiveNames", Default = "true") boolean descriptiveNames) {
        ports = SerialPort.getCommPorts();
        if (printToConsole) {
            for (SerialPort p : ports) {
                if (descriptiveNames) {
                    System.out.println(p.getDescriptivePortName());
                }
                else {
                    System.out.println(p.getSystemPortName());
                }

            }
        }

        if (descriptiveNames) {
            return Arrays.stream(ports).map(SerialPort::getDescriptivePortName).
                    toArray(String[]::new);
        }
        else {
            return Arrays.stream(ports).map(SerialPort::getSystemPortName).
                    toArray(String[]::new);
        }

    }

    @Instruction(description = "returns true if COM port is open")
    static boolean portIsOpen(@Param(Name = "port", Default = "") String port) {
        return portIsOpen(port, true);
    }

    @Instruction(description = "returns true if COM port is open")
    static boolean portIsOpen(@Param(Name = "port", Default = "") String port, @Param(Name = "printToConsole", Default = "true") boolean printToConsole) {
        if (ports == null) {
            getPorts(false);
        }

        for (SerialPort p : openPorts) {
            if (p.getSystemPortName().compareTo(port) == 0) {
                if (printToConsole) {
                    System.out.println(p.isOpen());
                }
                return p.isOpen();
            }
        }
        if (printToConsole) {
            System.out.println("Port " + port + " is not open or does not exist");
        }
        return false;
    }



    @Instruction(description = "list all currently open ports")
    static String[] listOpenPorts(@Param(Name = "printToConsole", Default = "true") boolean printToConsole) {
        return listOpenPorts(printToConsole, true);
    }

    @Instruction(description = "list all currently open ports")
    static String[] listOpenPorts(@Param(Name = "printToConsole", Default = "true") boolean printToConsole, @Param(Name = "descriptiveName", Default = "true") boolean descriptiveName) {
        if (printToConsole) {
            for (SerialPort p : openPorts) {
                if (descriptiveName) {
                    System.out.println(p.getDescriptivePortName());
                } else {
                    System.out.println(p.getSystemPortName());
                }

            }
        }

        if (descriptiveName) {
            return Arrays.stream(openPorts.toArray(new SerialPort[0])).map(SerialPort::getDescriptivePortName).
                    toArray(String[]::new);
        } else {
            return Arrays.stream(openPorts.toArray(new SerialPort[0])).map(SerialPort::getSystemPortName).
                    toArray(String[]::new);
        }

    }

    @Instruction(description="Opens the specified COM port")
    static boolean open(@Param(Name = "port", Default = "") String port, @Param(Name = "baudrate", Default = "9600") int baudrate,
                     @Param(Name = "databits", Default = "8") int dataBits, @Param(Name = "stopbits", Default = "1") int stopBits,
                     @Param(Name = "parity", Default = "None") String parity) {
        if (ports == null) {
            getPorts(false);
        }
        for (SerialPort p : ports) {
            if (p.getSystemPortName().compareTo(port) == 0) {
                p.setBaudRate(baudrate);
                p.setNumDataBits(dataBits);
                p.setNumStopBits(stopBits);
                if (listContainsPort(openPorts, p)) {
                    System.out.println("Port " + port + " parameters updated");
                }
                else {
                    if (!p.openPort()) {
                        System.out.println("failed to open port " + port);
                    }
                    else {
                        System.out.println("Port " + port + " opened");
                        openPorts.add(p);
                    }
                }
                return p.isOpen();
            }
        }
        return false;
    }

    @Instruction(description="Closes the specified COM port")
    static void close(@Param(Name = "port", Default = "defaultValue") String port) {
        if (ports == null) {
            getPorts(false);
        }
        for (SerialPort p : openPorts) {
            if (p.getSystemPortName().compareTo(port) == 0) {
                if (!p.closePort()) {
                    System.out.println("failed to close port " + port);
                }
                else {
                    System.out.println("Port " + port + " closed");
                    if (listContainsPort(openPorts, p)) {
                        removeFromList(openPorts, p);
                    }
                }
                return;
            }
        }
    }

    @Instruction(description = "Send a message to the specified port. Do not listen for ack message")
    static byte[] sendMessage(@Param(Name = "port", Default = "") String port, @Param(Name="data", Default="") String data) {
        return sendMessage(port, data, false);
    }

    @Instruction(description = "Send a message to the specified port. Can listen for ack message.")
    static byte[] sendMessage(@Param(Name = "port", Default = "") String port, @Param(Name="data", Default="") String data, @Param(Name="getAck", Default="true") boolean awaitAck) {
        if (portIsOpen(port, false)) {
            for (SerialPort p : ports) {
                if (p.getSystemPortName().compareTo(port) == 0) {
                    p.writeBytes(data.getBytes(), data.getBytes().length);
                    if (awaitAck) {
                        byte[] receivedData = new byte[100];
                        p.readBytes(receivedData, receivedData.length);
                        return receivedData;
                    }
                    break;
                }
            }
        }
        return null;
    }


    private static boolean listContainsPort(List<SerialPort> list, SerialPort port) {
        for (SerialPort p : list) {
            boolean equals = true;
            equals = equals && p.getSystemPortName().compareTo(port.getSystemPortName()) == 0;
            if (equals) {
                return true;
            }
        }
        return false;
    }

    private static SerialPort removeFromList(List<SerialPort> list, SerialPort port) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getSystemPortName().compareTo(port.getSystemPortName()) == 0) {
                return list.remove(i);
            }
        }
        return null;
    }

}
