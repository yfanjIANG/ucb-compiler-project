package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** Type denotation for a list type. */
public final class ListType extends TypeAnnotation {

    /** The element of list element. */
    public final TypeAnnotation elementType;

    /** The AST for the type annotation
     *       [ ELEMENTTYPE ].
     *  spanning source locations [LEFT..RIGHT].
     */
    public ListType(Location left, Location right, TypeAnnotation elementType) {
        super(left, right);
        this.elementType = elementType;
    }

}
