package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 *
 * Non-sealed class that is an extension point for for student-defined
 * declarations. To add custom declaration types, extend this class; for example:
 *
 *   public final class MyCustomDeclaration extends StudentDecl { }
 *
 * It is not necessary (but it is permitted) to modify or extend this class.
 *
 */
public non-sealed abstract class StudentDecl extends Declaration {
    public StudentDecl(Location left, Location right) {
        super(left, right);
    }
}
