package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/** A declaration of a variable (i.e., with type annotation). */
public final class VarDef extends Declaration {
    /** The variable and its assigned type. */
    public final TypedVar var;
    /** The initial value assigned. */
    public final Literal value;

    /** The AST for
     *      VAR = VALUE
     *  where VAR has a type annotation, and spanning source
     *  locations [LEFT..RIGHT]. */
    public VarDef(Location left, Location right, TypedVar var, Literal value) {
        super(left, right);
        this.var = var;
        this.value = value;
    }

    /** The identifier defined by this declaration. */
    @Override
    public Identifier getIdentifier() {
        return this.var.identifier;
    }
}
