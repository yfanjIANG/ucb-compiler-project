package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** Return from function. */
public final class ReturnStmt extends Stmt {

    /** Returned value. */
    public final Expr value;

    /** The AST for
     *     return VALUE
     *  spanning source locations [LEFT..RIGHT].
     */
    public ReturnStmt(Location left, Location right, Expr value) {
        super(left, right);
        this.value = value;
    }

}
