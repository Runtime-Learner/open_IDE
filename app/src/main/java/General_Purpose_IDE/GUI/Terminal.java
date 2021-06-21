package General_Purpose_IDE.GUI;

import General_Purpose_IDE.app.Scheduler;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import General_Purpose_IDE.app.Interpreter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

public class Terminal extends OutputStream {
    public static DefaultSingleCDockable terminal;
    private static String currentDirectory = "";
    private static String shellPrompt = "$ ";
    private static volatile JTextArea textArea;
    private static Terminal terminalInstance = null;

    /** used to ensure user is always where we want them to be */
    private static int oldCaretPosition;
    private static volatile String previousText = "";
    private static volatile char lastCharacterPressed = '\0';
    private static ArrayBlockingQueue<String> instructionQueue = new ArrayBlockingQueue<String>(10);

    /** Fair lock for concurrent writing. */
    private static final Semaphore lock = new Semaphore(1);




    private Terminal() {
        terminalInstance = this;
    }

    private static void create() {
        if (terminalInstance != null) {
            return;
        }
        terminalInstance = new Terminal();

        String id = "Terminal";
        String title = "Terminal";
        JPanel panel = new JPanel();
        DefaultSingleCDockable dockable = new DefaultSingleCDockable( id, title, panel);
        dockable.setTitleText( title );
        dockable.setCloseable( false );

        //initialize textArea
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(true);
        textArea.setFont(new Font(null, 0, 20));
        textArea.addCaretListener(e -> caretMovedEvent());
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                keyPressedEvent(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        //initialize the command prompt text
        textArea.append(currentDirectory + shellPrompt);
        textArea.setCaretPosition(0);
        oldCaretPosition = 0;


        JScrollPane scrollPane = new JScrollPane(textArea);

        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.HORIZONTAL;

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        dockable.add(scrollPane);

        terminal = dockable;
    }



    public static void setAsOutputStream() {
        create();
        System.setOut(new PrintStream(terminalInstance));
    }

    public static void setAsErrorStream() {
        create();
        System.setErr(new PrintStream(terminalInstance));
    }

    /* This method creates a new Dockable with title "Terminal" and a single JPanel with
     * its background color set to "color". */
    public static DefaultSingleCDockable getComponent() {
        create();
        return terminal;
    }

    public static Object runCommand(String instruction) {
        //TODO: need to check to see if an instruction is currently running (maybe have a running stack)

        textArea.setEditable(false);
        Object returnValue = Scheduler.runCommand(instruction);
        if (textArea.getDocument().getLength() > getLineStart(textArea.getText())) {
            if (!textArea.getText().substring(getLineStart(textArea.getText())).contains("\n")) {
                textArea.append("\n");
            }
            textArea.append(currentDirectory + shellPrompt);
        }

        textArea.setEditable(true);
        return returnValue;
    }

    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    private static void keyPressedEvent(KeyEvent e) {
        lastCharacterPressed = e.getKeyChar();
        switch (e.getKeyChar()) {
            case KeyEvent.VK_ENTER:
                SwingUtilities.invokeLater(() -> {
//                    System.err.println(textArea.getText());
                    int commandBegins = getLineStart(textArea.getText(), 1) + currentDirectory.length() + shellPrompt.length();
                    String userCommand = textArea.getText().substring(commandBegins).replaceAll("\n", "");
                    textArea.setText(textArea.getText().substring(0, commandBegins) + userCommand);
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    textArea.append("\n");
                    System.err.println(userCommand);
                    runCommand(userCommand);
                });

                break;
            case KeyEvent.VK_TAB:
                SwingUtilities.invokeLater(() -> {
                    int tabPosition = textArea.getText().indexOf("\t");
                    textArea.setText(textArea.getText().replaceAll("\t", ""));
                    textArea.setCaretPosition(tabPosition);
                    String userCommand = textArea.getText().substring(getLineStart(textArea.getText()));

                    //save the current number of rows in textArea to ensure the lineStart is correct
                    int lastNewLine = textArea.getText().lastIndexOf("\n");
                    //call the interpreter autocomplete function
                    String newUserCommand = Interpreter.autocomplete(userCommand, textArea.getCaretPosition() - getLineStart(textArea.getText()));

                    //check to see if we are on a new line
                    if (lastNewLine != textArea.getText().lastIndexOf("\n")) {
                        textArea.append(currentDirectory + shellPrompt);
                    }
                    textArea.setText(textArea.getText().substring(0, getLineStart(textArea.getText())) + newUserCommand);
                });
                break;
        }
    }

    private static void caretMovedEvent() {
//        System.err.println(isLegalInput());
        SwingUtilities.invokeLater(() -> {
            if (!isLegalInput()) {
                textArea.setText(previousText);
                textArea.setCaretPosition(oldCaretPosition);
            }
            else {
                previousText = textArea.getText();
                oldCaretPosition = textArea.getCaretPosition();
            }
        });
    }

    public static void clear() {
        create();
        textArea.setText("");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public static String getUserInput() {
        //ensure we are on a new line
//        if (textArea.getDocument().getLength() > getLineStart()) {
//            if (!textArea.getText().substring(getLineStart()).contains("\n")) {
//                textArea.append("\n");
//            }
//            textArea.append(currentDirectory + shellPrompt);
//        }
//
//        textArea.setEditable(true);
        return null;
    }

    private static int getLineStart(String text) {
        return getLineStart(text, 0);
    }

    private static int getLineStart(String text, int fromLast) {
        int lastIndexOf = text.lastIndexOf("\n");
        for (int i = 0; i < fromLast && lastIndexOf != -1; i++) {
            lastIndexOf = text.substring(0, lastIndexOf).lastIndexOf("\n");
        }

        if (lastIndexOf == -1) {
            return 0;
        }

        return lastIndexOf + 1;
    }


    private static boolean isLegalInput() {
        //added on non-legal line
        //removed from non-legal line
        //overwrote section, including from non-legal line
        int lineStart = getLineStart(previousText);
        if (textArea.getText().length() < lineStart) {
            return false;
        }
        if (textArea.getText().substring(0, lineStart).compareTo(previousText.substring(0, lineStart)) == 0) {
            return true;
        }
        return false;
    }


}
