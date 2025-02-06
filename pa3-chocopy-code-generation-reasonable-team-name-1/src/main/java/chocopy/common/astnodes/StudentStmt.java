package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 *
 * Non-sealed class that is an extension point for for student-defined
 * statements. To add custom statement types, extend this class; for example:
 *
 *   public final class MyCustomStmt extends StudentStmt { }
 *
 * It is not necessary (but it is permitted) to modify or extend this class.
 *
 */
public non-sealed abstract class StudentStmt extends Stmt {
    public StudentStmt(Location left, Location right) {
        super(left, right);
    }
}
