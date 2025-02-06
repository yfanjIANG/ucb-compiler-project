package chocopy.common.astnodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Base of all AST nodes representing definitions or declarations.
 */
public sealed abstract class Declaration extends Node permits
    ClassDef, FuncDef, GlobalDecl, NonLocalDecl, VarDef {

    /** A definition or declaration spanning source locations [LEFT..RIGHT]. */
    public Declaration(Location left, Location right) {
        super(left, right);
    }

    /** Return the identifier defined by this Declaration. */
    @JsonIgnore
    public abstract Identifier getIdentifier();
}
