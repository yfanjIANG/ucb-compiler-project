package chocopy.common.astnodes;

import java.util.List;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** A function call. */
public final class CallExpr extends Expr {

    /** The called function. */
    public final Identifier function;
    /** The actual parameter expressions. */
    public final List<Expr> args;

    /** AST for FUNCTION(ARGS) at [LEFT..RIGHT]. */
    public CallExpr(Location left, Location right, Identifier function,
                    List<Expr> args) {
        super(left, right);
        this.function = function;
        this.args = args;
    }

}
