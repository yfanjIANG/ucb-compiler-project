package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** List-indexing expression. */
public final class IndexExpr extends Expr {

    /** Indexed list. */
    public final Expr list;
    /** Expression for index value. */
    public final Expr index;

    /** The AST for
     *      LIST[INDEX].
     *  spanning source locations [LEFT..RIGHT].
     */
    public IndexExpr(Location left, Location right, Expr list, Expr index) {
        super(left, right);
        this.list = list;
        this.index = index;
    }

}
