package chocopy.pa3;

import chocopy.common.astnodes.*;

/**
 * A base class to help write visitors over the AST.
 *
 * By default, all visit implementations throw an error. This is so that when
 * you override this class, it is easy to tell which visit methods you still
 * might need to implement -- attempting to call any unimplemented visit method
 * will throw an exception at runtime.
 *
 * This class includes functions that dispatch on the dynamic type of each
 * Node, Expression, Stmt, etc. However, recursion on child AST nodes must be
 * implemented manually in each visit function. This is because each visitor may
 * not want to recursively traverse all children, or may want to traverse
 * children in a specific order.
 *
 * This class returns void on every method. If you need to return a value, you
 * can make a copy of this class and change return types as necessary. Make sure
 * to make the new class non-abstract.
 */
public abstract class AstVisitor {
    public void visit(AssignStmt node) {
        throw new UnsupportedOperationException("visit not implemented for AssignStmt");
    }

    public void visit(BinaryExpr node) {
        throw new UnsupportedOperationException("visit not implemented for BinaryExpr");
    }

    public void visit(BooleanLiteral node) {
        throw new UnsupportedOperationException("visit not implemented for BooleanLiteral");
    }

    public void visit(CallExpr node) {
        throw new UnsupportedOperationException("visit not implemented for CallExpr");
    }

    public void visit(ClassDef node) {
        throw new UnsupportedOperationException("visit not implemented for ClassDef");
    }

    public void visit(ClassType node) {
        throw new UnsupportedOperationException("visit not implemented for ClassType");
    }

    public void visit(CompilerError node) {
        throw new UnsupportedOperationException("visit not implemented for CompilerError");
    }

    public void visit(Errors node) {
        throw new UnsupportedOperationException("visit not implemented for Errors");
    }

    public void visit(ExprStmt node) {
        throw new UnsupportedOperationException("visit not implemented for ExprStmt");
    }

    public void visit(ForStmt node) {
        throw new UnsupportedOperationException("visit not implemented for ForStmt");
    }

    public void visit(FuncDef node) {
        //throw new UnsupportedOperationException("visit not implemented for FuncDef");
        return;
    }

    public void visit(GlobalDecl node) {
        throw new UnsupportedOperationException("visit not implemented for GlobalDecl");
    }

    public void visit(Identifier node) {
        throw new UnsupportedOperationException("visit not implemented for Identifier");
    }

    public void visit(IfExpr node) {
        throw new UnsupportedOperationException("visit not implemented for IfExpr");
    }

    public void visit(IfStmt node) {
        throw new UnsupportedOperationException("visit not implemented for IfStmt");
    }

    public void visit(IndexExpr node) {
        throw new UnsupportedOperationException("visit not implemented for IndexExpr");
    }

    public void visit(IntegerLiteral node) {
        throw new UnsupportedOperationException("visit not implemented for IntegerLiteral");
    }

    public void visit(ListExpr node) {
        throw new UnsupportedOperationException("visit not implemented for ListExpr");
    }

    public void visit(ListType node) {
        throw new UnsupportedOperationException("visit not implemented for ListType");
    }

    public void visit(MemberExpr node) {
        throw new UnsupportedOperationException("visit not implemented for MemberExpr");
    }

    public void visit(MethodCallExpr node) {
        throw new UnsupportedOperationException("visit not implemented for MethodCallExpr");
    }

    public void visit(NoneLiteral node) {
        throw new UnsupportedOperationException("visit not implemented for NoneLiteral");
    }

    public void visit(NonLocalDecl node) {
        throw new UnsupportedOperationException("visit not implemented for NonLocalDecl");
    }

    public void visit(Program node) {
        throw new UnsupportedOperationException("visit not implemented for Program");
    }

    public void visit(ReturnStmt node) {
        throw new UnsupportedOperationException("visit not implemented for ReturnStmt");
    }

    public void visit(StringLiteral node) {
        throw new UnsupportedOperationException("visit not implemented for StringLiteral");
    }

    public void visit(TypedVar node) {
        throw new UnsupportedOperationException("visit not implemented for TypedVar");
    }

    public void visit(UnaryExpr node) {
        throw new UnsupportedOperationException("visit not implemented for UnaryExpr");
    }

    public void visit(VarDef node) {
        throw new UnsupportedOperationException("visit not implemented for VarDef");
    }

    public void visit(WhileStmt node) {
        throw new UnsupportedOperationException("visit not implemented for WhileStmt");
    }

    public void dispatchNode(Node node) {
        switch (node) {
            case CompilerError compilerError -> visit(compilerError);
            case Declaration declaration -> dispatchDeclaration(declaration);
            case Errors errors -> visit(errors);
            case Expr expr -> dispatchExpr(expr);
            case Program program -> visit(program);
            case Stmt stmt -> dispatchStmt(stmt);
            case TypeAnnotation typeAnnotation -> dispatchTypeAnnotation(typeAnnotation);
            case TypedVar typedVar -> visit(typedVar);
            case StudentNode studentNode -> {
                String className = studentNode.getClass().getCanonicalName();
                throw new RuntimeException(
                        "dispatchNode not implemented for " + className);
            }
        };
    }

    public void dispatchDeclaration(Declaration node) {
        switch (node) {
            case ClassDef classDef -> visit(classDef);
            case FuncDef funcDef -> visit(funcDef);
            case GlobalDecl globalDecl -> visit(globalDecl);
            case NonLocalDecl nonLocalDecl -> visit(nonLocalDecl);
            case VarDef varDef -> visit(varDef);
            case StudentDecl studentDecl -> {
                String className = studentDecl.getClass().getCanonicalName();
                throw new RuntimeException(
                        "dispatchDeclaration not implemented for " + className);
            }
        };
    }

    public void dispatchStmt(Stmt node) {
        switch (node) {
            case AssignStmt assignStmt -> visit(assignStmt);
            case ExprStmt exprStmt -> visit(exprStmt);
            case ForStmt forStmt -> visit(forStmt);
            case IfStmt ifStmt -> visit(ifStmt);
            case ReturnStmt returnStmt -> visit(returnStmt);
            case WhileStmt whileStmt -> visit(whileStmt);
            case StudentStmt studentStmt -> {
                String className = studentStmt.getClass().getCanonicalName();
                throw new RuntimeException(
                        "dispatchStmt not implemented for " + className);
            }
        };
    }

    public void dispatchExpr(Expr node) {
        switch (node) {
            case BinaryExpr binExpr -> visit(binExpr);
            case CallExpr callExpr -> visit(callExpr);
            case Identifier id -> visit(id);
            case IfExpr ifExpr -> visit(ifExpr);
            case IndexExpr indexExpr -> visit(indexExpr);
            case ListExpr listExpr -> visit(listExpr);
            case Literal literal -> dispatchLiteral(literal);
            case MemberExpr memberExpr -> visit(memberExpr);
            case MethodCallExpr methodCallExpr -> visit(methodCallExpr);
            case UnaryExpr unaryExpr -> visit(unaryExpr);
            case StudentExpr studentExpr -> {
                String className = studentExpr.getClass().getCanonicalName();
                throw new RuntimeException(
                        "dispatchExpr not implemented for " + className);
            }
        };
    }

    public void dispatchLiteral(Literal node) {
        switch (node) {
            case BooleanLiteral boolLiteral -> visit(boolLiteral);
            case IntegerLiteral intLiteral -> visit(intLiteral);
            case NoneLiteral noneLiteral -> visit(noneLiteral);
            case StringLiteral strLiteral -> visit(strLiteral);
            case StudentLiteral studentLiteral -> {
                String className = studentLiteral.getClass().getCanonicalName();
                throw new RuntimeException(
                        "dispatchLiteral not implemented for " + className);
            }
        };
    }

    public void dispatchTypeAnnotation(TypeAnnotation node) {
        switch (node) {
            case ClassType classType -> visit(classType);
            case ListType listType -> visit(listType);
            case StudentType studentType -> {
                String className = studentType.getClass().getCanonicalName();
                throw new RuntimeException(
                        "dispatchTypeAnnotation not implemented for " + className);
            }
        };
    }
}
