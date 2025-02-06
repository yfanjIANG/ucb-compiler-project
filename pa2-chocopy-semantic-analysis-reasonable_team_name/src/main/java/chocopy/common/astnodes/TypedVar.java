package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** An identifier with attached type annotation. */
public final class TypedVar extends Node {

    /** The typed identifier. */
    public final Identifier identifier;
    /** The declared type. */
    public final TypeAnnotation type;

    /** The AST for
     *       IDENTIFIER : TYPE.
     *  spanning source locations [LEFT..RIGHT].
     */
    public TypedVar(Location left, Location right,
                    Identifier identifier, TypeAnnotation type) {
        super(left, right);
        this.identifier = identifier;
        this.type = type;
    }

}
