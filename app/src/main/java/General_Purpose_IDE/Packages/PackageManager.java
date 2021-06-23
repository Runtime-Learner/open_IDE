package General_Purpose_IDE.Packages;

import General_Purpose_IDE.app.Interpreter;
import org.atteo.classindex.ClassIndex;
import org.atteo.classindex.IndexSubclasses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

import static General_Purpose_IDE.Packages.Parameter.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Instruction {
  String description();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Package {
  String description();
}

/**
 * See legalParameterTypes array for legal parameter types
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface Param {
  String Name();
  String Default();
}


@IndexSubclasses
public class PackageManager {
  /** Used to format instruction manifest*/
  public static int consoleWidth = 60;

  private static Dictionary<String, InstructionPackage> map_str_pkg;
  private static Dictionary<InstructionPackage, ArrayList<Method>> map_pkg_instr = new Hashtable<InstructionPackage, ArrayList<Method>>();
  private static Dictionary<InstructionPackage, ArrayList<Method>> map_pkg_genericInstr = new Hashtable<InstructionPackage, ArrayList<Method>>();


  public static Dictionary<String, InstructionPackage> getInstructionPackages() {
    if (map_str_pkg == null) {
      map_str_pkg = new Hashtable<String, InstructionPackage>();

      for (InstructionPackage instructionPackage : getAllPackages()) {
        map_str_pkg.put(instructionPackage.getPkgName(), instructionPackage);
      }
    }

    return map_str_pkg;
  }


  private static List<InstructionPackage> getAllPackages() {
    List<InstructionPackage> instructionPackageList = new ArrayList<InstructionPackage>();
    try {
      for (Class<? extends InstructionPackage> klass : ClassIndex.getSubclasses(InstructionPackage.class)) {
        InstructionPackage pkg = klass.getDeclaredConstructor().newInstance((Object[]) null);

        //instruction package must have @Package tag
        if (pkg.getClass().isAnnotationPresent(Package.class)) {
          instructionPackageList.add(pkg);
        }
        else {
          System.err.println("Package " + pkg.getPkgName() + " must include @Package tag to be loaded");
        }

     }
   } catch (Exception e) {
      System.out.println("Error loading packages!");
      System.exit(1);
    }

    return instructionPackageList;
  }

  public static Object run(String method, String[] tokens) {
    String[] methodData = method.split("\\.");
    InstructionPackage pkg = map_str_pkg.get(methodData[0]);
    String method_name = methodData[1];
    int numberOfParameters = Interpreter.tokenlen(tokens) - 1;
    for (Method m : map_pkg_instr.get(pkg)) {
      //check all non-general purpose methods
      if (m.getName().toLowerCase().compareTo(method_name) == 0 &&
      m.getParameters().length == numberOfParameters) {
        try {
          if (!validateTokens(m, tokens)) {
            return 1;
          }

          Object[] paramList = new Object[m.getParameters().length];
          Arrays.fill(paramList, null);
          if (convertParameters(paramList, m.getParameters(), tokens)) {
            return m.invoke(null, paramList);
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }

    for (Method m : map_pkg_genericInstr.get(pkg)) {
      if (m.getName().toLowerCase().compareTo(method_name) == 0) {
        try {
          String[] paramList = new String[Interpreter.tokenlen(tokens)];
          for (int i = 0; i < Interpreter.tokenlen(tokens); i++) {
            paramList[i] = tokens[i];
          }
          Object param = (Object) paramList;

          m.invoke(null, param);
          return 0;
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }
    return 1;
  }

  /**
   *
   * @param paramList
   * @param parameters
   * @param tokens
   * @return 1 on success. 0 if illegal parameter type encountered or too many tokens passed to method
   */
  private static boolean convertParameters(Object[] paramList, Parameter[] parameters, String[] tokens) {
    int i;
    for (i = 1; i < tokens.length && tokens[i] != null && i - 1 < parameters.length; i++) {
      try {
        //TODO: more generalized method to compare tokens to special keywords? not hardcoded is the goal
          if (tokens[i].compareTo("@default") == 0 || tokens[i].compareTo("@") == 0) {
            //User want to pass the default value to the instruction
            paramList[i-1] = convert(parameters[i - 1].getAnnotation(Param.class).Default(),
                    parameters[i - 1].getParameterizedType().getTypeName());
          } else {
            paramList[i - 1] = convert(tokens[i], parameters[i - 1].getParameterizedType().getTypeName());
          }
      } catch (Exception e) {
        System.err.println("Illegal argument " + tokens[i]);
        return false;
      }
    }
    return true;
  }

  /**
   *
   * @param method
   * @param tokens
   * @return
   */
  private static boolean validateTokens(Method method, String[] tokens) {
    if (Interpreter.tokenlen(tokens) - 1 > method.getParameters().length) {
      System.err.println("Too many parameters for instruction. Expected " + method.getParameters().length + ", got " +
              (Interpreter.tokenlen(tokens) - 1));
      return false;
    }
    return true;
  }

  /**
   * Iterate over all methods in package class and returns a list of methods that can be considered to be
   * valid instructions.
   * @return list of names of package instructions in format package.method
   */
  public static List<String> getPackageInstructions(InstructionPackage pkg) {
    if (map_pkg_instr.get(pkg) == null && map_pkg_genericInstr.get(pkg) == null) {
      ArrayList<Method> methodSet = new ArrayList<>();
      ArrayList<Method> genericMethodSet = new ArrayList<>();

      for (Method method : pkg.getClass().getDeclaredMethods()) {
        if (method.getDeclaredAnnotation(Instruction.class) != null && validateInstruction(method, pkg.getPkgName())) {
          if (method.getParameters().length == 1 && method.getParameters()[0].getParameterizedType().getTypeName().compareTo(strArr) == 0) {
            genericMethodSet.add(method);
          }
          else {
            methodSet.add(method);
          }
        }
      }
      map_pkg_instr.put(pkg, methodSet);
      map_pkg_genericInstr.put(pkg, genericMethodSet);
    }

    List<String> instructionSet = new ArrayList<String>();
    for (Method method : map_pkg_instr.get(pkg)) {
      instructionSet.add(pkg.getClass().getSimpleName().toLowerCase() + "." + method.getName().toLowerCase() +
                "." + method.getParameters().length);
    }
    for (Method method : map_pkg_genericInstr.get(pkg)) {
      instructionSet.add(pkg.getClass().getSimpleName().toLowerCase() + "." + method.getName().toLowerCase() +
                ".any");
    }

    return instructionSet;
  }

  /**
   * ensure that the instruction we are attempting to load follows all the proper protocol
   *  - method must be static
   *  TODO: method must be inside the Packages package
   *  TODO: method must have default visibility
   *  - all parameters must include @Param tag
   *  - all parameters must be of a supported type (see legalParameterTypes variable for a list of supported types)
   *  - all parameter @Param(default) values must be of the same type as the parameter type (can convert from string to type)
   *  - all parameter @Param(name) values must be unique within an instruction
   *
   * TODO: if method has only one parameter of type String[], make it package.instruction.all
   *
   * @param method the method that is considered as an instruction
   * @param packageName the name of the package the method comes from
   * @return true if the method follows protocol. false otherwise
   */
  private static boolean validateInstruction(Method method, String packageName) {
    String error = "";
    boolean isValid = true;

    //all instructions must be static methods
    if (!Modifier.isStatic(method.getModifiers())) {
      error += "\t" + packageName + "." + method.getName() + " method must be static\n";
      isValid = false;
    }

    Parameter[] parameters = method.getParameters();

    if (parameters.length == 1 && parameters[0].getParameterizedType().getTypeName().compareTo(strArr) == 0) {
      //this is a general purpose instruction that takes any number of tokens
      isValid = true;
    }
    else
    {
      //this is an instruction that takes a set number of parameters
      for (Parameter p : parameters) {
        //all instruction parameters must have a @Param tag
        if (p.getAnnotation(Param.class) == null) {
          error += "\t" + packageName + "." + method.getName() + " parameter " + p.getName() + " must use @Param tag\n";
          isValid = false;
          continue;
        }

        //ensure that parameter is of supported type
        if (!legalParameterTypes.contains(p.getParameterizedType().getTypeName())) {
          error += "\t" + packageName + "." + method.getName() + " parameter " + p.getName() + " is of unsupported type \'" +
                  p.getParameterizedType().getTypeName() + "\'\n";
          isValid = false;
        }

        //ensure that default value is of same type as parameter type
        String defaultValue = p.getAnnotation(Param.class).Default();
        try {
          convert(defaultValue, p.getParameterizedType().getTypeName());
        } catch (Exception e){
          error += "\t" + packageName + "." + method.getName() + " parameter " + p.getName() + " default value \'" + defaultValue +
                  "\' cannot be converted to parameter type \'" +
                  p.getParameterizedType().getTypeName() + "\'\n";
          isValid = false;
        }
      }

      //if instruction is valid, ensure parameter names are unique
      if (isValid) {
        for (int param1 = 0; param1 < parameters.length; param1++) {
          for (int param2 = param1 + 1; param2 < parameters.length; param2++) {
            if (parameters[param1].getAnnotation(Param.class).Name().compareTo(
                    parameters[param2].getAnnotation(Param.class).Name()) == 0) {
              error += "\t" + packageName + "." + method.getName() + " parameter " + parameters[param1].getName() + " name \'" +
                      parameters[param1].getAnnotation(Param.class).Name() +
                      "\' is that same as parameter " +
                      parameters[param2].getName() + "\'s\n";
              isValid = false;
            }
          }
        }
      }
    }

    if (!isValid) {
      System.err.println("Instruction " + packageName + "." + method.getName() + " failed to load.");
      System.err.print(error);
    }
    return isValid;
  }
}
