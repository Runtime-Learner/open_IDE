package General_Purpose_IDE.GUI;

import General_Purpose_IDE.Packages.Comport;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBar {
    private static JMenuBar menuBar;

    private static void create() {
        menuBar = new JMenuBar();
        JMenu File = new JMenu( "File" );

        menuBar.add( File );
        populateFileMenu(File);
    }

    private static void populateFileMenu(JMenu file) {

        //comport menu
        JMenu Port = new JMenu( "Port" );
        Port.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                String[] availablePorts = (String[]) Terminal.runCommand("comport.getports false");
                Port.removeAll();
                for (String port : availablePorts) {
                    String machinePortName = port.substring(port.lastIndexOf("(") + 1, port.lastIndexOf(")"));
                    JMenuItem item;
                    if ((Boolean)Terminal.runCommand("comport.portisopen " + machinePortName + " false")) {
                        item = createMenuItem(port, "comport.close " + machinePortName);
                        item.setBackground(new Color(44, 196, 166));
                    } else {
                        item = createMenuItem(port, "comport.open " + machinePortName +
                                " @default @default @default @default");
                    }
                    Port.add(item);
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {

            }

            @Override
            public void menuCanceled(MenuEvent e) {

            }
        });


        //exit button
        JMenuItem exit = createMenuItem("Exit", "exit");

        file.add(Port);
        file.add(exit);
    }

    private static JMenuItem createMenuItem(String name) {
        JMenuItem item = new JMenuItem(name);
        return item;
    }

    private static JMenuItem createMenuItem(String name, String command) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(e -> Terminal.runCommand(command));
        return item;
    }

    public static JMenuBar getComponent() {
        if (menuBar == null) {
            create();
        }
        return menuBar;
    }
}
