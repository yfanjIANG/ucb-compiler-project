package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Base of all AST nodes representing statements.
 *
 * There is nothing in this class, but there will be some AST
 * node types that have fields that are *any statement* or a
 * list of statements. For those cases, having a field of this type will
 * encompass all types of statements such as expression statements,
 * if statements, while statements, etc.
 *
 */
public sealed abstract class Stmt extends Node permits
    AssignStmt, ExprStmt, ForStmt, IfStmt, ReturnStmt, WhileStmt {
    /** A statement spanning source locations [LEFT..RIGHT]. */
    public Stmt(Location left, Location right) {
        super(left, right);
    }
}
