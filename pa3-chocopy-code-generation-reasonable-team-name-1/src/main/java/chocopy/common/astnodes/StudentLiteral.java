package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 *
 * Non-sealed class that is an extension point for for student-defined
 * literals. To add custom literal types, extend this class; for example:
 *
 *   public final class MyCustomLiteral extends StudentLiteral { }
 *
 * It is not necessary (but it is permitted) to modify or extend this class.
 *
 */
public non-sealed abstract class StudentLiteral extends Literal {
    public StudentLiteral(Location left, Location right) {
        super(left, right);
    }
}
