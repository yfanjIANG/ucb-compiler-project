package chocopy.common.astnodes;

import java.util.List;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** Conditional statement. */
public final class IfStmt extends Stmt {
    /** Test condition. */
    public final Expr condition;
    /** "True" branch. */
    public final List<Stmt> thenBody;
    /** "False" branch. */
    public final List<Stmt> elseBody;

    /** The AST for
     *      if CONDITION:
     *          THENBODY
     *      else:
     *          ELSEBODY
     *  spanning source locations [LEFT..RIGHT].
     */
    public IfStmt(Location left, Location right,
                  Expr condition, List<Stmt> thenBody, List<Stmt> elseBody) {
        super(left, right);
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

}
