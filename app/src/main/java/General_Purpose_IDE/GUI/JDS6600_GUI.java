package General_Purpose_IDE.GUI;

import General_Purpose_IDE.Packages.Comport;
import General_Purpose_IDE.Packages.JDS6600;
import General_Purpose_IDE.app.Interpreter;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import javafx.scene.control.TextFormatter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JDS6600_GUI {
    public static DefaultSingleCDockable component;

    private static void create() {
        if (component != null) {
            return;
        }

        String id = "JDS6600";
        String title = "JDS6600";
        JPanel panel = new JPanel();
        DefaultSingleCDockable dockable = new DefaultSingleCDockable( id, title, panel);
        dockable.setTitleText( title );
        dockable.setCloseable( false );

        // create checkbox
        JCheckBox c1 = new JCheckBox("CH1");
        JCheckBox c2 = new JCheckBox("CH2");

        c1.addChangeListener(e -> JDS6600.open(c1.isSelected(), c2.isSelected()));
        c2.addChangeListener(e -> JDS6600.open(c1.isSelected(), c2.isSelected()));

        //combobox to select the machine
        JComboBox machineBox = new JComboBox(JDS6600.findSigGens(false));
        machineBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String port = e.getItem().toString();
                String machinePortName = port.substring(port.lastIndexOf("(") + 1, port.lastIndexOf(")"));
                Terminal.runCommand("jds6600.connecttomachine " + machinePortName);
            }

        });
        machineBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                machineBox.removeAllItems();
                String[] availableMachines = JDS6600.findSigGens(false);
                for (String machine : availableMachines) {
                    machineBox.addItem(machine);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        // create a new panel
        JPanel p = new JPanel();

        // add checkbox to panel
        p.add(machineBox);
        p.add(c1);
        p.add(c2);
        p.add(createCHPanel());

        dockable.add(p);

        component = dockable;
    }

    private static JPanel createCHPanel() {
        JPanel panel = new JPanel();
        JSlider VsliderCH1 = GUI.generateJSlider("ch1.v", "jds6600.setv $ch1.v $ch2.v");
        VsliderCH1.setMinimum(0);
        VsliderCH1.setMaximum(20000);
        VsliderCH1.setMajorTickSpacing(1000);
        JSlider VsliderCH2 = GUI.generateJSlider("ch2.v", "jds6600.setv $ch1.v $ch2.v");
        VsliderCH2.setMinimum(0);
        VsliderCH2.setMaximum(20000);

        panel.add(VsliderCH1);
        panel.add(VsliderCH2);

        return panel;
    }

    /* This method creates a new Dockable with title "Terminal" and a single JPanel with
     * its background color set to "color". */
    public static DefaultSingleCDockable getComponent() {
        create();
        return component;
    }
}
