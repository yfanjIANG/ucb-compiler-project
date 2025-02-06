package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** The expression 'None'. */
public final class NoneLiteral extends Literal {

    /** The AST for None, spanning source locations [LEFT..RIGHT]. */
    public NoneLiteral(Location left, Location right) {
        super(left, right);
    }

}
