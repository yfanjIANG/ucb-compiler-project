package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** An expression applying a unary operator. */
public final class UnaryExpr extends Expr {

    /** The text representation of the operator. */
    public final String operator;
    /** The operand to which it is applied. */
    public final Expr operand;

    /** The AST for
     *      OPERATOR OPERAND
     *  spanning source locations [LEFT..RIGHT].
     */
    public UnaryExpr(Location left, Location right,
                     String operator, Expr operand) {
        super(left, right);
        this.operator = operator;
        this.operand = operand;
    }

}
