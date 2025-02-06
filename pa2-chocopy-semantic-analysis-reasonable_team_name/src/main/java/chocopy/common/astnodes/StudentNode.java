package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 *
 * Non-sealed class that is an extension point for for student-defined
 * nodes. To add custom node types, extend this class; for example:
 *
 *   public final class MyCustomNode extends StudentNode { }
 *
 * It is not necessary (but it is permitted) to modify or extend this class.
 *
 */
public non-sealed abstract class StudentNode extends Node {
    public StudentNode(Location left, Location right) {
        super(left, right);
    }
}
