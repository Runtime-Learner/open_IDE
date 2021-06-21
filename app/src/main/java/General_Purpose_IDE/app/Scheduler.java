package General_Purpose_IDE.app;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

public class Scheduler extends Thread {
    private int id;

//    private static ArrayBlockingQueue<String> instructionQueue = new ArrayBlockingQueue<String>(10);
//
//    /** Fair lock for concurrent writing. */
//    private static final Semaphore lock = new Semaphore(1);
//
//    private static final Runnable instructionRunner = new Runnable() {
//        @Override
//        public void run() {
//            String ID = String.valueOf(Thread.currentThread().getId());
//            while (true) {
//                try {
//                    String instruction = instructionQueue.take();
//                    textArea.setEditable(false);
//                    Object returnValue = Interpreter.interpret(instruction);
//                    if (textArea.getDocument().getLength() > lineStart) {
//                        if (!textArea.getText().substring(lineStart).contains("\n")) {
//                            textArea.append("\n");
//                        }
//                        textArea.append(currentDirectory + shellPrompt);
//                        lineStart = textArea.getDocument().getLength();
//                    }
//                    textArea.setEditable(true);
//                } catch (InterruptedException e) {
//                    //TODO: sleep if interrupted?
//                    e.printStackTrace();
//                }
//            }
//        }
//    };

    public static Object runCommand(String instruction) {
        //TODO: need to check to see if an instruction is currently running (maybe have a running stack)
        return Interpreter.interpret(instruction);
    }

    @Override
    public void run() {

    }
}
