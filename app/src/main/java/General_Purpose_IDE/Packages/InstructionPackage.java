package General_Purpose_IDE.Packages;

import org.atteo.classindex.IndexSubclasses;


@IndexSubclasses
public abstract class InstructionPackage {


  public String getPkgName() {
    return this.getClass().getSimpleName().toLowerCase();
  }
  
  @Override
  public String toString() {
    return getPkgName();
  }
  
  public void printDescription() {
    //print out function name
    System.out.println("NAME");
    System.out.println("\t" + getPkgName() + "\n");
    
    //print out instruction description
    System.out.println("DESCRIPTION");
    System.out.println(this.getClass().getAnnotation(Package.class).description() + "\n");
  }
}
