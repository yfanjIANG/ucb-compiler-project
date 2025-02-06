package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 *
 * Non-sealed class that is an extension point for for student-defined
 * expressions. To add custom expression types, extend this class; for example:
 *
 *   public final class MyCustomExpr extends StudentExpr { }
 *
 * It is not necessary (but it is permitted) to modify or extend this class.
 *
 */
public non-sealed abstract class StudentExpr extends Expr {
    public StudentExpr(Location left, Location right) {
        super(left, right);
    }
}
