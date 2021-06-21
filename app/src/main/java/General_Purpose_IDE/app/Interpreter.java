package General_Purpose_IDE.app;

import General_Purpose_IDE.GUI.Terminal;
import General_Purpose_IDE.Packages.General;
import General_Purpose_IDE.Packages.InstructionPackage;
import General_Purpose_IDE.Packages.PackageManager;

import java.util.*;

public class Interpreter {
  private static Dictionary<String, InstructionPackage> packages;
  private static Dictionary<String, String> instruction_mapping;

  /**
   * Fetch all available instruction packages
   * Load all instructions in each individual package
   * Instructions are encoded as
   *  package.instruction
   *
   * If instruction name is unique, remove the package name from the name
   */
  public static void init() {
    //load all instruction packages
    packages = PackageManager.getInstructionPackages();
    instruction_mapping = new Hashtable<String, String>();

    //iterate over all instruction packages and load their instructions
    List<String> completeInstructionList = new ArrayList<String>();
    for (InstructionPackage pkg : Collections.list(packages.elements())) {
      completeInstructionList.addAll(PackageManager.getPackageInstructions(pkg));
    }

    //check if instruction name is unique
    String[] instructions = completeInstructionList.toArray(new String[0]);
    boolean[] isUnique = new boolean[instructions.length];
    Arrays.fill(isUnique, true);

    for (int index_inst1 = 0; index_inst1 < instructions.length; index_inst1++) {
      if (!isUnique[index_inst1]) {
        continue;
      }

      String[] inst1 = instructions[index_inst1].split("\\.");

      for (int index_inst2 = index_inst1 + 1; index_inst2 < instructions.length; index_inst2++) {
        String[] inst2 = instructions[index_inst2].split("\\.");
        if (inst1[1].compareTo(inst2[1]) == 0 && inst1[0].compareTo(inst2[0]) != 0) {
          System.err.println(instructions[index_inst1] + " matches " + instructions[index_inst2]);
          isUnique[index_inst1] = false;
          isUnique[index_inst2] = false;
        }
      }
    }

    //if instruction name is unique, mapping is instruction -> package.instruction
    for (int index = 0; index < isUnique.length; index++) {
      if (isUnique[index]) {
        String inst = instructions[index].split("\\.")[1];
        instruction_mapping.put(inst, instructions[index]);
      }
      instruction_mapping.put(instructions[index].split("\\.")[0] + "." + instructions[index].split("\\.")[1], instructions[index]);
    }
  }

  public static String autocomplete(String userCommand, int relativePosition) {
    String[] tokens = parse(userCommand);
    int numberOfTokens = tokenlen(tokens);

    //autocomplete instructionset
    if (numberOfTokens  <= 1) {
      return Autocomplete.autocomplete(userCommand, Collections.list(instruction_mapping.keys()));
    }

    //autocomplete instruction specific parameters
    else {
      return "";
    }
  }
  public static Object interpret(String userCommand) {
    String[] tokens = parse(userCommand);
    if (tokenlen(tokens) == 0) {
      return null;
    }

    if (instruction_mapping.get(tokens[0]) != null) {
      String inst = instruction_mapping.get(tokens[0]);
      String pkg = inst.substring(0, inst.indexOf('.'));
      if (packages.get(pkg) != null) {
        return PackageManager.run(inst, tokens);
      }
    }
    return null;
  }
  public static String[] parse(String userCommand) {
    String[] tokens = new String[500];
    Arrays.fill(tokens, null);

    if (userCommand.length() == 0) {
      return tokens;
    }

    int index = 0;
    int tokenIndex = 0;

    while (index < userCommand.length()) {
      while (index < userCommand.length() && Character.isWhitespace((int)userCommand.charAt(index))) {
        index++;
      }
      int endIndex = index;
      while (endIndex < userCommand.length() && !Character.isWhitespace((int)userCommand.charAt(endIndex))) {
        endIndex++;
      }
      if (index != endIndex) {
        /*TODO: this does not allow us to have math equations, but it allows us to do simple substitutions w/ variables,
          which allows us to link GUI components to the interpreter
        * */
        tokens[tokenIndex] = parseToken(userCommand.substring(index, endIndex));
        index = endIndex;
        tokenIndex++;
      }
    }
    if (Character.isWhitespace((int)userCommand.charAt(userCommand.length() - 1))) {
      tokens[tokenIndex] = "";
    }

    return tokens;
  }

  private static String parseToken(String token) {
    if (token.length() == 0) {
      return token;
    }

    if (token.charAt(0) == '$') {
      String retrievedData = (String) Terminal.runCommand("general.getstring " + token.substring(1));
      if (retrievedData != null) {
        return retrievedData;
      }
      return "@default";
    }

    return token;

  }

  public static int tokenlen(String[] tokens) {
    int counter = 0;
    while (tokens[counter] != null) {
      counter++;
    }
    return counter;
  }

  public static String[] getInstructionSet() {
    ArrayList<String> list = Collections.list(instruction_mapping.keys());
    return list.toArray(new String[0]);
  }
}
