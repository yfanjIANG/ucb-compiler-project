package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** String constants. */
public final class StringLiteral extends Literal {

    /** Contents of the literal, not including quotation marks. */
    public final String value;

    /** The AST for a string literal containing VALUE, spanning source
     *  locations [LEFT..RIGHT]. */
    public StringLiteral(Location left, Location right, String value) {
        super(left, right);
        this.value = value;
    }

}
