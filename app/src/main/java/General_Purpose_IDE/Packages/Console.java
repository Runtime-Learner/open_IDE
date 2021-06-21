package General_Purpose_IDE.Packages;

import General_Purpose_IDE.GUI.Terminal;

@Package(description = "Instructions related to the console, e.g: clearing the terminal")
public class Console extends InstructionPackage {
    @Instruction(description="clears the terminal")
     static void clear() {
        Terminal.clear();
    }
}
