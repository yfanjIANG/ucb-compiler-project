package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** Declaration of global variable. */
public final class GlobalDecl extends Declaration {

    /** The declared variable. */
    public final Identifier variable;

    /** The AST for the declaration
     *      global VARIABLE
     *  spanning source locations [LEFT..RIGHT].
     */
    public GlobalDecl(Location left, Location right, Identifier variable) {
        super(left, right);
        this.variable = variable;
    }

    @Override
    public Identifier getIdentifier() {
        return this.variable;
    }
}
