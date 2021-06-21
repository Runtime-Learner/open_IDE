package General_Purpose_IDE.GUI;

import General_Purpose_IDE.Packages.General;

import javax.swing.*;

public class GUI {
    public static JSlider generateJSlider(String name) {
        return generateJSlider(name, "");
    }
    public static JSlider generateJSlider(String name, String command) {
        JSlider slider = new JSlider();
        if (command != null && command.trim().length() > 0) {
            slider.addChangeListener(e -> {
//                System.err.println(Integer.toString(slider.getValue()));
                Terminal.runCommand("general.set " + name + " "  + slider.getValue());
                Terminal.runCommand(command);
            });
        }
        else {
            slider.addChangeListener(e -> {
                Terminal.runCommand("general.set " + name + " " + slider.getValue());
            });
        }
        return slider;
    }

}
