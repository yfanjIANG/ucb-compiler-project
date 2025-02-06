package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 *
 * Non-sealed class that is an extension point for for student-defined
 * type annotations. To add custom type annotation types, extend this class;
 * for example:
 *
 *   public final class MyCustomType extends StudentType { }
 *
 * It is not necessary (but it is permitted) to modify or extend this class.
 *
 */
public non-sealed abstract class StudentType extends TypeAnnotation {
    public StudentType(Location left, Location right) {
        super(left, right);
    }
}
