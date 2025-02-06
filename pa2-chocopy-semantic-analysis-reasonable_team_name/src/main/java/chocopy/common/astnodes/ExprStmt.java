package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** Statements consisting of expressions. */
public final class ExprStmt extends Stmt {

    /** The expression I evaluate. */
    public final Expr expr;

    /** The AST for EXPR spanning source locations [LEFT..RIGHT]
     *  in a statement context. */
    public ExprStmt(Location left, Location right, Expr expr) {
        super(left, right);
        this.expr = expr;
    }

}
