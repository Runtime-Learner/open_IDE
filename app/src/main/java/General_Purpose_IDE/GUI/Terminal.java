package General_Purpose_IDE.GUI;

import General_Purpose_IDE.app.Scheduler;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import General_Purpose_IDE.app.Interpreter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
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
    private static volatile JTextPane textArea;
    private static Terminal terminalOut = null;
    private static Terminal terminalErr = null;

    /** used to ensure user is always where we want them to be */
    private static int oldCaretPosition;
    private static volatile String previousText = "";
    private static volatile char lastCharacterPressed = '\0';
    private static ArrayBlockingQueue<String> instructionQueue = new ArrayBlockingQueue<String>(10);

    /** Fair lock for concurrent writing. */
    private static final Semaphore lock = new Semaphore(1);
    private static volatile int runningInstructions = 0;
    private static volatile String savedUserCommand = "";




    private Terminal() {
    }

    private static void create() {
        if (terminalOut != null) {
            return;
        }
        terminalOut = new Terminal();
        terminalErr = new Terminal();

        String id = "Terminal";
        String title = "Terminal";
        JPanel panel = new JPanel();
        DefaultSingleCDockable dockable = new DefaultSingleCDockable( id, title, panel);
        dockable.setTitleText( title );
        dockable.setCloseable( false );

        //initialize textArea
        textArea = new JTextPane();
        //TODO: fix linewrap
//        textArea.setLineWrap(true);
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
        System.setOut(new PrintStream(terminalOut));
    }

    public static void setAsErrorStream() {
        create();
        System.setErr(new PrintStream(terminalErr));
    }

    /* This method creates a new Dockable with title "Terminal" and a single JPanel with
     * its background color set to "color". */
    public static DefaultSingleCDockable getComponent() {
        create();
        showUserPrompt();
        return terminal;
    }

    public static synchronized Object runCommand(String instruction) {
        //TODO: need to check to see if an instruction is currently running (maybe have a running stack)
        if (userPromptIsVisible()) {
            hideUserPrompt(true);
//            textArea.setText(textArea.getText().substring(0, getLineStart(textArea.getText())));
        }
        runningInstructions++;
//        System.err.println("running instruction " + instruction);
        Object returnValue = Scheduler.runCommand(instruction);

        runningInstructions--;
        if (runningInstructions == 0) {
            showUserPrompt();
        }
        return returnValue;
    }

    private static void hideUserPrompt(boolean saveUserInput) {
        if (userPromptIsVisible()) {
            int commandBegins = getLineStart(textArea.getText()) + currentDirectory.length() + shellPrompt.length();
            if (saveUserInput) {
                savedUserCommand = textArea.getText().substring(commandBegins).replaceAll("\n", "");
            }

            textArea.setText(textArea.getText().substring(0, getLineStart(textArea.getText())));
            textArea.setCaretPosition(textArea.getDocument().getLength());
            previousText = textArea.getText();
            textArea.setEditable(false);
        }
    }

    private static void showUserPrompt() {
        if (!userPromptIsVisible()) {
            if (textArea.getText().substring(getLineStart(textArea.getText())).length() != 0) {
                textArea.setText(textArea.getText() + "\n");
            }
            textArea.setText(textArea.getText() + currentDirectory + shellPrompt + savedUserCommand);
            textArea.setCaretPosition(textArea.getDocument().getLength());
            previousText = textArea.getText();
            textArea.setEditable(true);
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (this.equals(terminalErr)) {
                textArea.setForeground(new Color(100, 0, 0));
            } else {
                textArea.setForeground(new Color(000, 0, 0));
        }
        // redirects data to the text area
        int printToPosition = getPrintPosition();

        String text1 = textArea.getText().substring(0, printToPosition);
        String text2 = textArea.getText().substring(printToPosition);
        textArea.setText(text1 + (char) b + text2);
//        System.err.println("<" + textArea.getText() + ">");

        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
        previousText = textArea.getText();
    }

    private int getPrintPosition() {
        int lineStart;
        if (userPromptIsVisible()) {
            if (getLineStart(textArea.getText()) == 0) {
                textArea.setText("\n" + textArea.getText());
                lineStart = 0;
            } else {
                lineStart = getLineStart(textArea.getText()) - 1;
            }
        } else {
            lineStart = textArea.getDocument().getLength();
        }

        return lineStart;
    }

    private static boolean userPromptIsVisible() {
        int lineStart = getLineStart(textArea.getText());
        String substring = textArea.getText().substring(lineStart);
        String prompt = currentDirectory + shellPrompt;
        return substring.startsWith(prompt);
    }

    private static void keyPressedEvent(KeyEvent e) {
        lastCharacterPressed = e.getKeyChar();
        SwingUtilities.invokeLater(() -> {
            if (!isLegalInput()) {
                try {
                    System.err.println("oldCaretPos: " + oldCaretPosition + ", lineStart: " + textArea.getText().length() + ", docLen: " + textArea.getDocument().getText(0, textArea.getDocument().getLength()));
                } catch (BadLocationException badLocationException) {
                    badLocationException.printStackTrace();
                }
                textArea.setCaretPosition(Math.max(oldCaretPosition, getLineStart(textArea.getText())));
            }
            else {
                previousText = textArea.getText();
                oldCaretPosition = textArea.getCaretPosition();
                int commandBegins;
                String userCommand;
                switch (e.getKeyChar()) {
                    case KeyEvent.VK_ENTER:
//                    System.err.println(textArea.getText());
                        commandBegins = getLineStart(textArea.getText(), 1) + currentDirectory.length() + shellPrompt.length();
                        userCommand = textArea.getText().substring(commandBegins).replaceAll("\n", "");
                        textArea.setText(textArea.getText().substring(0, commandBegins) + userCommand);
                        textArea.setCaretPosition(textArea.getDocument().getLength());
                        textArea.setText(textArea.getText() + "\n");
                        System.err.println("command to execute: " + userCommand);
                        runCommand(userCommand);

                        break;
                    case KeyEvent.VK_TAB:
                        int tabPosition = textArea.getText().indexOf("\t");
                        textArea.setText(textArea.getText().replaceAll("\t", ""));
                        textArea.setCaretPosition(tabPosition);
                        commandBegins = getLineStart(textArea.getText()) + currentDirectory.length() + shellPrompt.length();
                        userCommand = textArea.getText().substring(commandBegins);

                        //save the current number of rows in textArea to ensure the lineStart is correct
                        int lastNewLine = textArea.getText().lastIndexOf("\n");
                        //call the interpreter autocomplete function
                        String newUserCommand = Interpreter.autocomplete(userCommand, textArea.getCaretPosition() - getLineStart(textArea.getText()));

                        commandBegins = getLineStart(textArea.getText()) + currentDirectory.length() + shellPrompt.length();
                        textArea.setText(textArea.getText().substring(0, commandBegins) + newUserCommand);
                        textArea.setCaretPosition(textArea.getDocument().getLength());
                        break;
                }
            }
        });

    }

    private static void caretMovedEvent() {
//        System.err.println(isLegalInput());
//        SwingUtilities.invokeLater(() -> {
//            if (!isLegalInput()) {
//                textArea.setText(previousText);
//                textArea.setCaretPosition(oldCaretPosition);
//            }
//            else {
//                previousText = textArea.getText();
//                oldCaretPosition = textArea.getCaretPosition();
//            }
//        });
    }

    public static void clear() {
        create();
        previousText = "";
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
        System.err.println("prev: " + previousText.length() + ", new: " + textArea.getText().length() + ", docLen: " + textArea.getDocument().getLength());
        if (textArea.getText().length() < lineStart) {
            return false;
        }
        if (textArea.getText().substring(0, lineStart).compareTo(previousText.substring(0, lineStart)) == 0) {
            return true;
        }
        return false;
    }


}
