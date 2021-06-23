package General_Purpose_IDE.Packages;

import General_Purpose_IDE.app.Interpreter;

import java.util.Dictionary;
import java.util.Hashtable;

@Package(description = "Instructions related to the App as a whole. E.g: exiting the application")
public class General extends InstructionPackage {
    private static Dictionary<String, Parameter> variables = new Hashtable<String, Parameter>();

    @Instruction(description="Closes the application")
    static void exit() {
        System.exit(0);
    }

    @Instruction(description = "print out all instructions available to user")
    static void help() {
        String[] instructionSet = Interpreter.getInstructionSet();
        for (String instruction : instructionSet) {
            System.out.println(instruction);
        }
    }

    @Instruction(description = "create a variable")
    static boolean set(@Param(Name = "name", Default = "") String name,
                             @Param(Name = "value", Default = "") String value) {
        if (name.trim().length() == 0) {
            System.out.println("Variable name cannot be empty");
            return false;
        }
        if (variables.get(name) != null) {
            variables.get(name).setValue(value);
            return true;
        }
        try {
            Parameter p = new Parameter(name, value);
            variables.put(name, p);
            return true;
        } catch (Exception e) {
            System.out.print("Could not create variable: ");
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Instruction(description = "retrieve variable value")
    static Object get(@Param(Name = "name", Default = "") String name) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (name.trim().length() == 0) {
            System.out.println("Variable name cannot be empty");
            return null;
        }
        if (variables.get(name) != null) {
            return variables.get(name).getConvertedValue();
        }

        System.out.println("Variable \'" + name + "\' does not exist");
        return null;
    }

    @Instruction(description = "retrieve variable value")
    static String getString(@Param(Name = "name", Default = "") String name) {
        if (name.trim().length() == 0) {
            System.out.println("Variable name cannot be empty");
            return null;
        }
        if (variables.get(name) != null) {
            System.out.println(variables.get(name).getValue());
            return variables.get(name).getValue();
        }

        System.out.println("Variable \'" + name + "\' does not exist");
        return null;
    }
}
