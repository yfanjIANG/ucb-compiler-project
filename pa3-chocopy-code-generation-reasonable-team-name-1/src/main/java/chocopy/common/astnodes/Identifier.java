package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** A simple identifier. */
public final class Identifier extends Expr {

    /** Text of the identifier. */
    public final String name;

    /** An AST for the variable, method, or parameter named NAME, spanning
     *  source locations [LEFT..RIGHT]. */
    public Identifier(Location left, Location right, String name) {
        super(left, right);
        this.name = name;
    }

}
