package chocopy.common.astnodes;

import java.util.List;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** Method calls. */
public final class MethodCallExpr extends Expr {

    /** Expression for the bound method to be called. */
    public final MemberExpr method;
    /** Actual parameters. */
    public final List<Expr> args;

    /** The AST for
     *      METHOD(ARGS).
     *  spanning source locations [LEFT..RIGHT].
     */
    public MethodCallExpr(Location left, Location right,
                          MemberExpr method, List<Expr> args) {
        super(left, right);
        this.method = method;
        this.args = args;
    }

}
