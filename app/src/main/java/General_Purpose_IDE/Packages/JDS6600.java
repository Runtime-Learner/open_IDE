package General_Purpose_IDE.Packages;

import General_Purpose_IDE.GUI.Terminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Package(description = "Instructions related to interfacing with a JDS6600 signal generator")
public class JDS6600 extends InstructionPackage {
    private static List<String> availableSigGens = null;
    private static String connectedToPort = null;

    @Instruction(description = "check COM ports for signal generators")
    public static String[] findSigGens(@Param(Name = "printToConsole", Default = "true") boolean printToConsole) {
        String[] allPorts = (String[]) Terminal.runCommand("comport.getports false");
        availableSigGens = new ArrayList<String>();

        boolean foundSigGen = false;
        for (String s : allPorts) {
            if (s.contains("USB-SERIAL CH340")) {
                if (printToConsole) {
                    System.out.println(s);
                }
                foundSigGen = true;
                availableSigGens.add(s);
            }
        }
        return availableSigGens.toArray(new String[] {});
    }

    @Instruction(description = "attempt to open a serial connection to a signal generator")
    public static boolean connectToMachine(@Param(Name = "port", Default = "") String port) {
        if (availableSigGens == null) {
            findSigGens(false);
        }
        boolean foundSigGen = false;
        for (String s : availableSigGens) {
            if (s.contains(port)) {
                foundSigGen = true;
            }
        }
        if (!foundSigGen) {
            System.out.println("No supported machines connected on port " + port);
            return false;
        }
        if (portIsOpen()  && connectedToPort.compareTo(port) == 0) {
            System.out.println("already connected to machine");
            return true;
        }
        else {
            if (Comport.open(port, 115200, 8, 1, "None")) {
                connectedToPort = port;
                System.err.println("Connected to machine at port " + port);
                return true;
            }
            return false;
        }
    }

    @Instruction(description = "turn CH1/CH2 on or off")
    public static boolean open(@Param(Name = "ch1", Default = "false") boolean CH1, @Param(Name = "ch2", Default = "false") boolean CH2) {
        if (!portIsOpen()) {
            System.err.println("Not connected to any machines");
            return false;
        }

        int ch1 = CH1 ? 1 : 0;
        int ch2 = CH2 ? 1 : 0;

        Comport.sendMessage(connectedToPort, ":w20=" + ch1 + "," + ch2 + ".\r\n");
        return true;
    }

    @Instruction(description = "set CH1/CH2 voltage")
    public static boolean setV(@Param(Name = "ch1", Default = "1") int CH1, @Param(Name = "ch2", Default = "1") int CH2) {
        if (!portIsOpen()) {
            System.err.println("Not connected to any machines");
            return false;
        }

        Comport.sendMessage(connectedToPort, ":w25=" + CH1 + ".\r\n");
        Comport.sendMessage(connectedToPort, ":w26=" + CH2 + ".\r\n");
        return true;
    }

    private static boolean portIsOpen() {
        if (connectedToPort == null) {
            return false;
        }
        String[] openPorts = Comport.listOpenPorts(false, false);
        for (int i = 0; i < openPorts.length; i++) {
            if (connectedToPort.compareTo(openPorts[i]) == 0) {
                return true;
            }
        }
        return false;
    }
}
