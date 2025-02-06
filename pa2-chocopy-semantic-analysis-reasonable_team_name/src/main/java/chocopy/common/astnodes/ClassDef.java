package chocopy.common.astnodes;

import java.util.List;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** A class definition. */
public final class ClassDef extends Declaration {

    /** Name of the declared class. */
    public final Identifier name;
    /** Name of the parent class. */
    public final Identifier superClass;
    /** Body of the class. */
    public final List<Declaration> declarations;

    /** An AST for class
     *    NAME(SUPERCLASS):
     *       DECLARATIONS.
     *  spanning source locations [LEFT..RIGHT].
     */
    public ClassDef(Location left, Location right,
                    Identifier name, Identifier superClass,
                    List<Declaration> declarations) {
        super(left, right);
        this.name = name;
        this.superClass = superClass;
        this.declarations = declarations;
    }

    @Override
    public Identifier getIdentifier() {
        return this.name;
    }
}
