package chocopy.pa2;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.ClassValueType;
import chocopy.common.analysis.types.FuncType;
import chocopy.common.analysis.types.ListValueType;
import chocopy.common.analysis.types.Type;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.astnodes.*;
import proguard.classfile.attribute.preverification.ObjectType;

import static chocopy.common.analysis.types.Type.INT_TYPE;
import static chocopy.common.analysis.types.Type.BOOL_TYPE;
import static chocopy.common.analysis.types.Type.NONE_TYPE;
import static chocopy.common.analysis.types.Type.STR_TYPE;
import static chocopy.common.analysis.types.Type.EMPTY_TYPE;


import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo.None;

import static chocopy.common.analysis.types.Type.OBJECT_TYPE;

/** Analyzer that performs ChocoPy type checks on all nodes.  Applied after
 *  collecting declarations. */
public class TypeChecker {

    /** The current symbol table (changes depending on the function
     *  being analyzed). */
    private SymbolTable<Type> sym;
    private  SymbolTable<Type> globals;
    
    /** Collector for errors. */
    private Errors errors;
    //a map to store the class symbotable so that we could check sym of superclass and do type check in memberExpr
    private final Map<String, SymbolTable<Type>> classSymTables;
    //a map to store the superclass, if value is object type means that it doesn't have superclass
    private final Map<String, String> superClassTables;


    /** Creates a type checker using GLOBALSYMBOLS for the initial global
     *  symbol table and ERRORS0 to receive semantic errors. */
    public TypeChecker(Map<String, SymbolTable<Type>> globalSymbols, Errors errors0) {
        sym = globalSymbols.get("THIS_IS_GLOBAL_SYMBOL");
        globalSymbols.remove("THIS_IS_GLOBAL_SYMBOL");
        globals = sym;
        errors = errors0;
        classSymTables = globalSymbols;
        superClassTables = new HashMap<>(); 
    }

    /** Mutates the AST-in place; adding type annotations to each AST node. */
    public void analyzeAndAddTypes(Program program) {
        analyzeProgram(program);
    }

    /** Inserts an error message in NODE if there isn't one already.
     *  The message is constructed with MESSAGE and ARGS as for
     *  String.format. */
    private void err(Node node, String message, Object... args) {
        errors.semError(node, message, args);
    }

    private void analyzeProgram(Program program) {
        

        for (Declaration decl : program.declarations) {
            analyzeDeclaration(decl);
        }
        for (Stmt stmt : program.statements) {
            analyzeStmt(stmt);
        }
    }

    private void analyzeDeclaration(Declaration decl) {
        switch (decl) {
            case VarDef varDef -> analyzeVarDef(varDef);
            case FuncDef funcDef -> analyzeFuncDef(funcDef);
            case ClassDef classDef -> analyzeClassDef(classDef);
            case GlobalDecl globalDecl -> analyzeGlobalDecl(globalDecl);
            case NonLocalDecl nonlocalDecl -> analyzeNonlocalDecl(nonlocalDecl);

            /* TODO: Add more cases here. */
            default -> {
                String className = decl.getClass().getCanonicalName();
                throw new UnsupportedOperationException(
                    "analyzeDeclaration not yet implemented for " + className);
            }
        }
    }

    private void analyzeStmt(Stmt stmt) {
        switch (stmt) {
            case ExprStmt exprStmt -> analyzeExprStmt(exprStmt);
            case AssignStmt assignStmt -> analyzeAssignStmt(assignStmt);
            case WhileStmt whileStmt -> analyzeWhileStmt(whileStmt);
            case ForStmt forStmt -> analyzeForStmt(forStmt);
            case IfStmt ifStmt -> analyzeIfStmt(ifStmt);
            case ReturnStmt returnStmt -> err(returnStmt, "Return statement cannot appear at the top level");
            /* TODO: Add more cases here. */
            default -> {
                String className = stmt.getClass().getCanonicalName();
                throw new UnsupportedOperationException(
                    "analyzeStmt not yet implemented for " + className);
            }
        }
    }


    private void analyzeExprStmt(ExprStmt s) {
        analyzeExpr(s.expr);
    }

    private Type analyzeExpr(Expr expr) {
        return switch (expr) {
            case IntegerLiteral intLit -> analyzeIntegerLiteral(intLit);
            case BooleanLiteral boolLit -> analyzeBooleanLiteral(boolLit);
            case NoneLiteral noneLit -> analyzeNoneLiteral(noneLit);
            case StringLiteral strLit -> analyzeStringLiteral(strLit);
            case Identifier id -> analyzeIdentifier(id);
            case BinaryExpr binExpr -> analyzeBinaryExpr(binExpr);
            case UnaryExpr unExpr -> analyzeUnaryExpr(unExpr);
            case ListExpr listExpr -> analyzeListExpr(listExpr);
            case IndexExpr indexExpr -> analyzeIndexExpr(indexExpr);
            case IfExpr ifExpr -> analyzeIfExpr(ifExpr);
            case CallExpr callExpr -> analyzeCallExpr(callExpr);
            case MemberExpr memberExpr -> analyzeMemberExpr(memberExpr);
            case MethodCallExpr methodCallExpr -> analyzeMethodCall(methodCallExpr);
            /* TODO: Add more cases here. */
            default -> {
                
                String className = expr.getClass().getCanonicalName();
                throw new UnsupportedOperationException(
                    "analyzeExpr not yet implemented for " + className);
            }
        };
    }

    private Type analyzeIntegerLiteral(IntegerLiteral i) {
        return i.setInferredType(Type.INT_TYPE);
    }

    private Type analyzeBooleanLiteral(BooleanLiteral i) {
        return i.setInferredType(Type.BOOL_TYPE);
    }

    private Type analyzeNoneLiteral(NoneLiteral i) {
        return i.setInferredType(NONE_TYPE);
    }

    private Type analyzeStringLiteral(StringLiteral i) {
        return i.setInferredType(STR_TYPE);
    }


    private Type analyzeListExpr(ListExpr listExpr) {
        List<Type> elementTypes = new ArrayList<>();
        
        for (Expr element : listExpr.elements) {
            Type elementType = analyzeExpr(element);
            elementTypes.add(elementType);
        }
    
        if (!elementTypes.isEmpty()) {
            Type firstElementType = elementTypes.get(0);
            for (Type type : elementTypes) {
                if (!type.equals(firstElementType)) {
                    return listExpr.setInferredType(new ListValueType(OBJECT_TYPE));
                }
            }
            return listExpr.setInferredType(new ListValueType(firstElementType));
        } 
        else {
            return listExpr.setInferredType(EMPTY_TYPE);
        }
    }
    
    private Type analyzeIndexExpr(IndexExpr indexExpr) {
        Type listtype = analyzeExpr(indexExpr.list); 
        Type indexType = analyzeExpr(indexExpr.index);
        if (!(listtype instanceof ListValueType) && !(listtype.equals(STR_TYPE))) {
            err(indexExpr.list, "Cannot index into type `%s`", listtype);
            return OBJECT_TYPE;
        }
        if (!indexType.equals(INT_TYPE)) {
            err(indexExpr.list, "Index is of non-integer type `%s`", indexType);
        }
        
    
        if (listtype instanceof ListValueType ListType) {
            return indexExpr.setInferredType(ListType.elementType);
        } else {
            if (listtype.equals(STR_TYPE)) {
                return indexExpr.setInferredType(STR_TYPE);
            }
        }
    
        
        return OBJECT_TYPE; 
    }


    private Type analyzeUnaryExpr(UnaryExpr e) {
        Type operandType = analyzeExpr(e.operand);
    
        switch (e.operator) {
            case "-":
                if (INT_TYPE.equals(operandType)) {
                    return e.setInferredType(INT_TYPE);
                } else {
                    err(e, "Cannot apply operator `%s` on type `%s`", e.operator, operandType);
                    return e.setInferredType(INT_TYPE);
                }
            case "not":
                if (BOOL_TYPE.equals(operandType)) {
                    return e.setInferredType(BOOL_TYPE);
                } else {
                    err(e, "Cannot apply operator `%s` on type `%s`", e.operator, operandType);
                    return e.setInferredType(BOOL_TYPE);
                }
            default:
                return e.setInferredType(OBJECT_TYPE);
        }
    }

    private Type analyzeBinaryExpr(BinaryExpr e) {
    Type leftType = analyzeExpr(e.left);
    Type rightType = analyzeExpr(e.right);
    switch (e.operator) {
        case "+":
        if (leftType instanceof ListValueType && rightType instanceof ListValueType) {
            Type elementType = computeLeastUpperBound(((ListValueType) leftType).elementType,((ListValueType) rightType).elementType);
            return e.setInferredType(new ListValueType(elementType));
        } 

        else if (INT_TYPE.equals(leftType) && INT_TYPE.equals(rightType)) {
            return e.setInferredType(INT_TYPE);
        }
        else if (STR_TYPE.equals(leftType) && STR_TYPE.equals(rightType)) {
            return e.setInferredType(STR_TYPE);
        }
        // either side is int type, infer to be int type
        else if (INT_TYPE.equals(leftType) || INT_TYPE.equals(rightType)){
            err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, leftType, rightType);
            return e.setInferredType(INT_TYPE);
        }
        else {
            err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, leftType, rightType);
            return e.setInferredType(OBJECT_TYPE);
        }

        case "-":
        case "*":
        case "//":
        case "%":
            if (INT_TYPE.equals(leftType) && INT_TYPE.equals(rightType)) {
                return e.setInferredType(INT_TYPE);
            } else {
                err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, leftType, rightType);
                return e.setInferredType(OBJECT_TYPE);
            }

        case "and":
        case "or":
            if (BOOL_TYPE.equals(leftType) && BOOL_TYPE.equals(rightType)) {
                return e.setInferredType(BOOL_TYPE);
            } else {
                err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, leftType, rightType);
                return e.setInferredType(OBJECT_TYPE);
            }

        case "<":
        case ">":
        case "<=":
        case ">=":
            if (INT_TYPE.equals(leftType) && INT_TYPE.equals(rightType)) {
                return e.setInferredType(BOOL_TYPE);
            } else {
                err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, leftType, rightType);
                return e.setInferredType(OBJECT_TYPE);
            }

        case "==":
        case "!=":
            if ((INT_TYPE.equals(leftType) && INT_TYPE.equals(rightType)) || (BOOL_TYPE.equals(leftType) && BOOL_TYPE.equals(rightType))
            || (STR_TYPE.equals(leftType) && STR_TYPE.equals(rightType)))
                return e.setInferredType(BOOL_TYPE);
            else {
                err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, leftType, rightType);
                return e.setInferredType(OBJECT_TYPE);
            }

        case "is":
            if (!leftType.isSpecialType() && !rightType.isSpecialType()) {
                return e.setInferredType(BOOL_TYPE);
            } else {
                err(e, "Cannot apply operator `%s` on types `%s` and `%s`",
                    e.operator, leftType, rightType);
                return e.setInferredType(OBJECT_TYPE);
            }

        default:
            return e.setInferredType(OBJECT_TYPE);
        }
    }

    private Type computeLeastUpperBound(Type type1, Type type2) {
        if (type1.equals(type2)) {
            return type1;
        }
        if (type1.equals(NONE_TYPE)) {
            return type2;
        } 
        else if (type2.equals(NONE_TYPE)) {
            return type1;
        }
        return OBJECT_TYPE;
    }


    private Type analyzeIdentifier(Identifier id) {
        String varName = id.name;
        Type varType = sym.get(varName);

        if (varType != null && varType.isValueType()) {
            return id.setInferredType(varType);
        }

        err(id, "Not a variable: %s", varName);
        return id.setInferredType(ValueType.OBJECT_TYPE);
    }

    private void analyzeVarDef(VarDef varDef) {
        Identifier id = varDef.getIdentifier();
        ValueType varType = ValueType.annotationToValueType(varDef.var.type);

        if (!(varType.isListType() && !varType.elementType().equals(Type.EMPTY_TYPE)) &&
        !varType.isSpecialType() && !varType.equals(Type.NONE_TYPE) && !varType.equals(Type.OBJECT_TYPE) &&
        !(varType.className() != null && sym.get(varType.className()) instanceof ClassValueType)) {
        errors.semError(varDef.var.type, "Invalid type annotation; there is no class named: %s", varType.className());
        }

    
        if (varDef.value != null) {
            Type initValueType = analyzeExpr(varDef.value);
            
            if (!(initValueType.equals(NONE_TYPE) && (varType.equals(OBJECT_TYPE) || varType instanceof ClassValueType
            || varType instanceof ListValueType))) {
                if (!initValueType.equals(varType)) {
                    err(varDef.var, "Expected type `%s`; got type `%s`", varType, initValueType);
                }
            }
        }
    

        sym.put(id.name, varType);

        
    }
    
    private void analyzeAssignStmt(AssignStmt assignStmt) {
        Type assignedValueType = analyzeExpr(assignStmt.value); 
        boolean errorOccurred = false;
        
        for (Expr target : assignStmt.targets) {
            Type targetType = analyzeExpr(target); 
            // identifier
            if (target instanceof Identifier id) {
                if (targetType == null) {
                    err(id, "Not a variable: %s", id.name);
                    continue; 
                }
                String varName = id.name;
                if (!sym.declares(varName) && sym.get(varName) != null) {
                    boolean isGlobalDeclared = checkGlobalDeclaration(varName, id);
                    boolean isNonlocalDeclared = !isGlobalDeclared && checkNonlocalDeclaration(varName, id);
    
                    if (!isGlobalDeclared || !isNonlocalDeclared) {
                        err(id, "Cannot assign to variable that is not explicitly declared in this scope: %s", varName);
                        errorOccurred = true;
                        continue; 
                    }
                }
                if (!Typecompatible(targetType, assignedValueType)) {
                    if (!errorOccurred) {
                        err(assignStmt, "Expected type `%s`; got type `%s`", targetType, assignedValueType);
                        errorOccurred = true;
                    }
                } 
                else {
                    id.setInferredType(targetType);
                }
            } 
            // index expr
            else if (target instanceof IndexExpr indexExpr) {
                Type listtype = analyzeExpr(indexExpr.list);
                if (listtype instanceof ListValueType listType) {
                    targetType = listType.elementType;
                } 
                else {
                    err(indexExpr, "`%s` is not a list type", listtype);
                    continue; 
                }

                if (!Typecompatible(targetType, assignedValueType)) {
                    if (!errorOccurred) {
                        err(assignStmt, "Expected type `%s`; got type `%s`", targetType, assignedValueType);
                        errorOccurred = true;
                    }
                }
            } 
            // member expr
            else if (target instanceof MemberExpr){
                if (!Typecompatible(targetType, assignedValueType)) {
                    if (!errorOccurred) {
                        err(assignStmt, "Expected type `%s`; got type `%s`", targetType, assignedValueType);
                        errorOccurred = true;
                    }
                }
            } 
            else {
                err(target, "Not a variable: %s", target);
                continue; 
            }
        }
    }
    
    //helper function to analyze T1>=T2
    private boolean Typecompatible(Type target, Type value) {
        if (target == null || value == null) {
            return false;
        }
    
        if (target.equals(value) || (!target.isSpecialType() && value.equals(NONE_TYPE))) {
            return true;
        }
    
        if (target.equals(OBJECT_TYPE) && (value.equals(NONE_TYPE) || value.isListType() || value.equals(STR_TYPE) || value.equals(INT_TYPE))) {
            return true;
        }
    
        if (target.isListType() && value.equals(EMPTY_TYPE)) {
            return true;
        }
    
        if (target.isListType() && value.isListType()) {
            Type targetElementType = target.elementType();
            Type valueElementType = value.elementType();
            return targetElementType.equals(valueElementType) || valueElementType.equals(NONE_TYPE);
        }
        //which means that target is class type
        if (classSymTables.get(target.toString()) != null) { 
            String superClassName = superClassTables.get(value.toString());
            while (!superClassName.equals("object")) {
                if (superClassName.equals(target.toString())) {
                    return true;
                }
                superClassName = superClassTables.get(superClassName);
            }
            return false;
        }
    
        return false; 
    }
    
    
    
    

    private void analyzeWhileStmt(WhileStmt whileStmt) {
        Type conditionType = analyzeExpr(whileStmt.condition);

        // condition should be bool type
        if (!conditionType.equals(Type.BOOL_TYPE)) {
            err(whileStmt.condition, "Condition expression cannot be of type `%s`", conditionType);
        }

        // check stmt
        for (Stmt bodyStmt : whileStmt.body) {
            analyzeStmt(bodyStmt);
        }
    }
    
    private Type analyzeIfExpr(IfExpr ifExpr) {
        Type conditionType = analyzeExpr(ifExpr.condition);
        if (!conditionType.equals(Type.BOOL_TYPE)) {
            err(ifExpr.condition, "Condition expression cannot be of type `%s`", conditionType);
        }

        Type trueType = analyzeExpr(ifExpr.thenExpr);
        Type falseType = analyzeExpr(ifExpr.elseExpr);

        if (!trueType.equals(falseType)) {
            return Type.OBJECT_TYPE;
        }

        ifExpr.setInferredType(trueType);
        return trueType;
    }
    
    private void analyzeIfStmt(IfStmt ifStmt) {
        Type conditionType = analyzeExpr(ifStmt.condition);
        if (!conditionType.equals(BOOL_TYPE)) {
            err(ifStmt.condition, "Condition expression cannot be of type `%s`", conditionType);
        }
        for (Stmt stmt : ifStmt.thenBody) {
            if (!(stmt instanceof ReturnStmt)){
                analyzeStmt(stmt);
            }
        }
        for (Stmt stmt : ifStmt.elseBody) {
            if (!(stmt instanceof ReturnStmt)){
                analyzeStmt(stmt);
            }
        }
    }

    private void analyzeForStmt(ForStmt node) {
        Type iterableType = node.iterable.setInferredType(
                analyzeExpr(node.iterable)); 
    
        if (iterableType == null) {
            err(node, "Iterable `%s` type inference error.", node.iterable);
        } else if (iterableType.equals(Type.STR_TYPE)) {
            node.identifier.setInferredType(Type.STR_TYPE);
        } else if (iterableType.elementType() == null) {
            err(node, "Cannot iterate over value of type `%s`", iterableType);
        } else {
            node.identifier.setInferredType(
                iterableType.elementType()
            );
        }
    
        for (Stmt stmt : node.body) {
            analyzeStmt(stmt); 
        }
    }


    //recursive analyze a function
    private void analyzeFuncDef(FuncDef funcDef) {
        Identifier id = funcDef.getIdentifier();
        String name = id.name;
        TypeAnnotation rtnType = funcDef.returnType;
        ValueType returnType = ValueType.annotationToValueType(funcDef.returnType);
        //type annotation check
        if (returnType != null && !returnType.isSpecialType() && !returnType.isListType() && !returnType.equals(Type.OBJECT_TYPE) &&
        !(sym.get(returnType.className()) instanceof ClassValueType) && !returnType.equals(Type.NONE_TYPE)) {
        errors.semError(funcDef.returnType, "Invalid type annotation; there is no class named: %s", returnType.className());
        }
        //shadow class name check
        if (globals.get(name) != null && globals.get(name) instanceof ClassValueType) {
                if(name.equals("str")||name.equals("int")||name.equals("bool")||name.equals("object"))
                errors.semError(id, "Cannot shadow class name: " + name);
        }
    
        if (rtnType == null) {
            err(id, "Function `%s` has not been declared or is missing a type annotation.", name);
        }
        //change the scope into the function environment
        SymbolTable<Type> previousScope = sym;
        sym = new SymbolTable<>(previousScope);
        List<ValueType> params = new ArrayList<>();
        for (TypedVar param : funcDef.params) {
            Identifier paramId = param.identifier;
            String paramName = paramId.name;
            Type paramType = ValueType.annotationToValueType(param.type);
            params.add((ValueType) paramType);
            //shadow class name check
            if (globals.get(param.identifier.name) != null && globals.get(param.identifier.name) instanceof ClassValueType) {
                if(param.identifier.name.equals("str")||param.identifier.name.equals("int")
                    ||param.identifier.name.equals("bool")|param.identifier.name.equals("object")
                    ||classSymTables.get(param.identifier.name) != null){
                        errors.semError(param.identifier, "Cannot shadow class name: " + param.identifier.name);

                    }
                
            }
            //type annotation check
            if (!(paramType.isListType() && !paramType.elementType().equals(Type.EMPTY_TYPE)) &&
                !paramType.isSpecialType() &&
                !(paramType.className() != null && previousScope.get(paramType.className()) instanceof ClassValueType)) {
                errors.semError(param.type, "Invalid type annotation; there is no class named: %s", paramType.className());
            }
            //duplicate parameters definition check
            if (sym.declares(paramName)) {
                err(paramId, "Duplicate declaration of identifier in same scope: %s", paramName);
            } else {
                sym.put(paramName, paramType);
            }
        }
        //check the declaration in the function body
        FuncType funcType = new FuncType(params, returnType);
        List<Declaration> nested_funcdef = new ArrayList<>();
        for (Declaration declaration : funcDef.declarations) {
            // nested function in this function body, process later
            if(declaration instanceof FuncDef){
                nested_funcdef.add(declaration);
            } 
            // vardef in the function body
            else if(declaration instanceof VarDef){
                //shadow class name check
                if (globals.get(declaration.getIdentifier().name) != null && globals.get(declaration.getIdentifier().name) instanceof ClassValueType) {
                    if(declaration.getIdentifier().name.equals("str")||declaration.getIdentifier().name.equals("int")
                    ||declaration.getIdentifier().name.equals("bool")|declaration.getIdentifier().name.equals("object")
                    ||classSymTables.get(declaration.getIdentifier().name) != null)
                    {
                        errors.semError(declaration.getIdentifier(), "Cannot shadow class name: " + declaration.getIdentifier().name);
    
                    }
    
                }
                // duplicate declaration check
                if (sym.declares(declaration.getIdentifier().name)) {
                    err(declaration.getIdentifier(), "Duplicate declaration of identifier in same scope: %s", declaration.getIdentifier().name);}

                analyzeDeclaration(declaration);  
            }           
            // other declaration like global and nonlocal
            else{
                analyzeDeclaration(declaration);  
            }
            
        }
        //analyze the neested function
        for (Declaration declaration : nested_funcdef){
            Identifier func_decl = declaration.getIdentifier();
            String func_decl_name = func_decl.name;
            if (sym.declares(func_decl_name)) {
                err(func_decl, "Duplicate declaration of identifier in same scope: %s", func_decl_name);}
            analyzeDeclaration(declaration);
        }
        boolean hasReturn = false; 
        for (Stmt stmt : funcDef.statements) {
            if (stmt instanceof ReturnStmt returnStmt) {
                hasReturn = true;
                analyzeReturnStmt(returnStmt, ValueType.annotationToValueType(rtnType));
            } else {
                analyzeStmt(stmt);
            }
        }
    
        if (!hasReturn && !returnType.equals(Type.NONE_TYPE) && !returnType.equals(Type.OBJECT_TYPE) ) {
            err(id, "All paths in this function/method must have a return statement: %s", name);
        }
    
    
        sym = previousScope;
        sym.put(name, funcType);


    }
    

    
    
    private void analyzeClassDef(ClassDef classDef) {
        Identifier classNameId = classDef.getIdentifier();
        String className = classNameId.name;
    
        // get the classtype
        Type classType = sym.get(className);
        if (!(classType instanceof ClassValueType)) {
            errors.semError(classNameId, "Class definition error: %s is not a valid class type", className);
            return;
        }
        ClassValueType classValueType = (ClassValueType) classType;
    
        SymbolTable<Type> parentSymTable = null;
    
        // create class symbol table to inherit the parent symbol table
        String superClassName = classDef.superClass.name;
        SymbolTable<Type> parentScope = sym;
        SymbolTable<Type> classSym = sym;
        sym = new SymbolTable<>(globals);
        if (!(superClassName.equals("object"))) {
            parentSymTable = classSymTables.get(superClassName);
            sym = new SymbolTable<>(parentSymTable);
        }
        superClassTables.put(className, superClassName);
        
        
    
        // check classdef declarations 
        for (Declaration decl : classDef.declarations) {
            classSymTables.put(className, sym);
            if (decl instanceof VarDef varDef) {
                Identifier attrId = varDef.getIdentifier();
                String attrName = attrId.name;

                // conflict with the parent
                Type inheritedType = sym.get(attrName);
                boolean inThisScope = sym.declares(attrName);
                if (inheritedType != null && !inThisScope) {
                    errors.semError(attrId, "Cannot re-define attribute: %s", attrName);
                } else if (inThisScope) {
                    errors.semError(attrId, "Duplicate declaration of identifier in same scope: %s", attrName);
                } else {
                    analyzeVarDef(varDef); 
                }

            }
            //a little bit different from funcdef 
            else if (decl instanceof FuncDef funcDef) {
                Identifier id = funcDef.getIdentifier();
                String name = id.name;
                TypeAnnotation rtnType = funcDef.returnType;
                ValueType returnType = ValueType.annotationToValueType(funcDef.returnType);
                if (name.equals("__init__")) {
                    if (funcDef.params.size() != 1) {
                        err(funcDef.getIdentifier(), "Method overridden with different type signature: %s", name);
                    } else {
                        if (!funcDef.params.get(0).identifier.name.equals("self") )
                            err(funcDef.getIdentifier(), "Method overridden with different type signature: %s", name);
                    }
                }
                if (returnType != null && !returnType.isSpecialType() && !returnType.isListType() && !returnType.equals(Type.OBJECT_TYPE) &&
                    !(sym.get(returnType.className()) instanceof ClassValueType) && !returnType.equals(Type.NONE_TYPE)) {
                    errors.semError(funcDef.returnType, "Invalid type annotation; there is no class named: %s", returnType.className());
                    }
                    
                if (globals.get(name) != null && globals.get(name) instanceof ClassValueType) {
                        if(name.equals("str")||name.equals("int")||name.equals("bool")||name.equals("object"))
                        errors.semError(id, "Cannot shadow class name: " + name);
                }

                if (rtnType == null) {
                    err(id, "Function `%s` has not been declared or is missing a type annotation.", name);
                }
                // record the previousscope and prepare for the scope change
                SymbolTable<Type> previousScope = sym;
                sym = new SymbolTable<>(previousScope);
                List<ValueType> params = new ArrayList<>();

                if (funcDef.params.size() == 0){
                    err(funcDef.name, "First parameter of the following method must be of the enclosing class: %s", funcDef.getIdentifier().name);
                }

                for (int i = 0; i < funcDef.params.size(); i++) {
                    TypedVar param = funcDef.params.get(i);
                    Identifier paramId = param.identifier;
                    String paramName = paramId.name;
                    Type paramType = ValueType.annotationToValueType(param.type);
                    params.add((ValueType) paramType);
                    if (i == 0) {
                        if (!paramName.equals("self") || !paramType.equals(classValueType)) {
                            err(funcDef.name, "First parameter of the following method must be of the enclosing class: %s",
                            funcDef.getIdentifier().name);
                        }
                    }
                    if (globals.get(param.identifier.name) != null && globals.get(param.identifier.name) instanceof ClassValueType) {
                        if(param.identifier.name.equals("str")||param.identifier.name.equals("int")
                            ||param.identifier.name.equals("bool")|param.identifier.name.equals("object")
                            ||classSymTables.get(param.identifier.name) != null){
                                errors.semError(param.identifier, "Cannot shadow class name: " + param.identifier.name);
        
                            }
                    }
        
                    if (!(paramType.isListType() && !paramType.elementType().equals(Type.EMPTY_TYPE))
                            && !paramType.isSpecialType() &&
                            !(paramType.className() != null
                                    && (parentScope.get(paramType.className()) instanceof ClassValueType))) {
                        errors.semError(param.type, "Invalid type annotation; there is no class named: %s",
                                paramType.className());
                    }
                    if (sym.declares(paramName)) {
                        err(paramId, "Duplicate declaration of identifier in same scope: %s", paramName);
                    } else {
                        sym.put(paramName, paramType);
                    }

                }
                FuncType funcType = new FuncType(params, returnType);
                List<Declaration> nested_funcdef = new ArrayList<>();
                for (Declaration declaration : funcDef.declarations) {
                    if(declaration instanceof FuncDef){
                        nested_funcdef.add(declaration);
                    } 
                    else if(declaration instanceof VarDef){
                        if (globals.get(declaration.getIdentifier().name) != null && globals.get(declaration.getIdentifier().name) instanceof ClassValueType) {
                            if(declaration.getIdentifier().name.equals("str")||declaration.getIdentifier().name.equals("int")
                            ||declaration.getIdentifier().name.equals("bool")|declaration.getIdentifier().name.equals("object")
                            ||classSymTables.get(declaration.getIdentifier().name) != null)
                            {
                                errors.semError(declaration.getIdentifier(), "Cannot shadow class name: " + declaration.getIdentifier().name);
                            }
                        }
                        if (sym.declares(declaration.getIdentifier().name)) {
                            err(declaration.getIdentifier(), "Duplicate declaration of identifier in same scope: %s", declaration.getIdentifier().name);}

                        analyzeDeclaration(declaration);  
                    }
                    else{
                        analyzeDeclaration(declaration);  
                    }
            
                }
                for (Declaration declaration : nested_funcdef){
                    Identifier func_decl = declaration.getIdentifier();
                    String func_decl_name = func_decl.name;
                    if (sym.declares(func_decl_name)) {
                        err(func_decl, "Duplicate declaration of identifier in same scope: %s", func_decl_name);}
                    analyzeDeclaration(declaration);
                }
                boolean hasReturn = false;  
                for (Stmt stmt : funcDef.statements) {
                    if (stmt instanceof ReturnStmt returnStmt) {
                        hasReturn = true;
                        analyzeReturnStmt(returnStmt, ValueType.annotationToValueType(rtnType));
                    } 
                    else {
                        analyzeStmt(stmt);
                    }
                }
                if (!hasReturn && !returnType.equals(Type.NONE_TYPE) && !returnType.equals(Type.OBJECT_TYPE) ) {
                    err(id, "All paths in this function/method must have a return statement: %s", name);
                }
            
                sym = previousScope;


            

                
                Type inheritedType = sym.get(name);
                if (inheritedType != null && !(inheritedType instanceof FuncType)) {
                    errors.semError(funcDef.getIdentifier(), "Cannot re-define attribute: %s", name);
                } else if (sym.declares(name)) {
                    errors.semError(decl.getIdentifier(), "Duplicate declaration of identifier in same scope: %s", name);
                } else {
                    //check inherited correctness
                    FuncType superFuncType = (FuncType) inheritedType;
                    if (inheritedType != null) {
                        if (superFuncType.parameters.size() != funcType.parameters.size() || !superFuncType.returnType.equals(funcType.returnType)) {
                            err(funcDef.getIdentifier(), "Method overridden with different type signature: %s", name);
                        } 
                        else {
                            for (int i = 1; i < funcType.parameters.size(); i++) {
                                Type superFuncParaType = superFuncType.parameters.get(i);
                                Type funcParaType = funcType.parameters.get(i);
                                if (!superFuncParaType.equals(funcParaType)) {
                                    err(funcDef.getIdentifier(), "Method overridden with different type signature: %s", name);
                                }
                            }
                            sym.put(name, funcType);
                        }
                    } 
                    else {
                        sym.put(name, funcType); 
                    }
                    
                    
                }
                
            } else {
                errors.semError(decl.getIdentifier(), "Invalid declaration in class: %s", decl.getIdentifier().name);
            }
        }
        
        classSymTables.put(className, sym); 
        sym = classSym; //recover
    }
    


    private void analyzeReturnStmt(ReturnStmt returnStmt, Type expectReturnType) {
        if (returnStmt.value != null) {
            Type exprType = analyzeExpr(returnStmt.value);

            if (!Typecompatible(expectReturnType, exprType)) {
                errors.semError(returnStmt, "Expected type `%s`; got type `%s`", expectReturnType, exprType);
            }
            returnStmt.value.setInferredType(exprType);
            
        } else {
            // return value == null
            if (!Typecompatible(expectReturnType, ValueType.NONE_TYPE)) {
                errors.semError(returnStmt, "Expected type `%s`; got `None`", expectReturnType);
            }
        }
    }
    
    private Type analyzeCallExpr(CallExpr callExpr) {
        String functionName = callExpr.function.name;
    
        Type funcOrClassType = sym.get(functionName);
        if (funcOrClassType == null) {
            err(callExpr.function, "Not a function or class: %s", functionName);
            return ValueType.OBJECT_TYPE; 
        }
        // It is a class
        if (classSymTables.get(funcOrClassType.toString()) != null) {
            ClassValueType classType = (ClassValueType) funcOrClassType;
    
            if (!callExpr.args.isEmpty()) {
                errors.semError(callExpr, "Expected 0 arguments; got %s", callExpr.args.size());
                return classType;
            }
    
            callExpr.setInferredType(classType);
            return classType;
        }
        // It is a function
        if (!(funcOrClassType instanceof FuncType)) {
            err(callExpr.function, "Not a function or class: %s", functionName);
            return ValueType.OBJECT_TYPE; 
        }
    
        FuncType functionType = (FuncType) funcOrClassType;
        List<ValueType> paramTypes = functionType.parameters;
        Type returnType = functionType.returnType;
    
        if (callExpr.args.size() != paramTypes.size()) {
            errors.semError(callExpr, "Expected %d arguments; got %d", paramTypes.size(), callExpr.args.size());
            return ValueType.OBJECT_TYPE;
        }
        // parameter check
        for (int i = 0; i < callExpr.args.size(); i++) {
            Expr argExpr = callExpr.args.get(i);
            Type argType = analyzeExpr(argExpr); 
            Type expectedType = paramTypes.get(i);
    
            if (!Typecompatible(expectedType, argType)) {
                errors.semError(callExpr, "Expected type `%s`; got type `%s` in parameter %d",
                         expectedType, argType, i);
                break;
            }
        }
    
        callExpr.function.setInferredType(new FuncType(functionType.parameters, functionType.returnType));
        callExpr.setInferredType(returnType);
        return returnType;
    }
    

    private Type analyzeGlobalDecl(GlobalDecl globalDecl) {
        Identifier id = globalDecl.getIdentifier();
        String name = id.name;
    
        if (sym.declares(name)) {
            errors.semError(id, "Duplicate declaration of identifier in same scope: %s", name);
            return null;
        }
        // check the global
        if (!globals.declares(name) || !globals.get(name).isValueType()) {
            errors.semError(id, "Not a global variable: %s", name);
            return null;
        }
        // should not be special
        if(name.equals("str")||name.equals("int")||name.equals("bool")||name.equals("object")){
            errors.semError(id, "Not a global variable: %s", name);
            return null;
        }

        Type globalVarType = globals.get(name);
        sym.put(name,globalVarType);
        return globalVarType;
    }
    


    private Type analyzeNonlocalDecl(NonLocalDecl nonlocalDecl) {
        Identifier id = nonlocalDecl.getIdentifier();
        String name = id.name;
    
        if (sym.declares(name)) {
            errors.semError(id, "Duplicate declaration of identifier in same scope: %s", name);
            return null;
        }
    
        SymbolTable<Type> previousScope = sym;
        if (sym.getParent() == null || !sym.getParent().declares(name) ||sym.getParent().getParent() == null) {
            errors.semError(id, "Not a nonlocal variable: %s", name);
            return null;
        }
    
        sym = sym.getParent();
        Type nonlocalVarType = sym.get(name);
        sym = previousScope;
        sym.put(name,nonlocalVarType);

        return nonlocalVarType;
    }
    
    private boolean checkGlobalDeclaration(String varName, Identifier id) {

    
        boolean isGlobal = globals.declares(varName) && globals.get(varName).isValueType();
        
        if (!isGlobal) {
            errors.semError(id, "Not a global variable: %s", varName);
        }
        if(varName.equals("str")||varName.equals("int")||varName.equals("bool")||varName.equals("object")){
            errors.semError(id, "Not a global variable: %s", varName);
            isGlobal = false;
        }
        
        return isGlobal;
    }
    
    private boolean checkNonlocalDeclaration(String varName, Identifier id) {
        SymbolTable<Type> previousScope = sym;
        boolean isNonlocal = false;

        if (sym.getParent() != null) {
            sym = sym.getParent();
            if (sym.declares(varName) && sym.get(varName).isValueType()) {
                isNonlocal = true;
            } else {
                errors.semError(id, "Not a nonlocal variable: %s", varName);
                sym = previousScope;
            }
        }

        sym = previousScope;
        return isNonlocal;
    }

    private Type analyzeMemberExpr(MemberExpr memberExpr) {
        Type objectType = analyzeExpr(memberExpr.object);
        // no such attr
        if (classSymTables.get(objectType.toString()) == null) {
            err(memberExpr.object, "There is no attribute named `%s` in class `%s`", memberExpr.member.name.toString(),
                    objectType);
            return ValueType.OBJECT_TYPE; 
        }

        ClassValueType classType = (ClassValueType) objectType;
        SymbolTable<Type> classSymTable = classSymTables.get(classType.toString());

        // no such attr
        Type memberType = classSymTable.get(memberExpr.member.name);
        if (memberType == null) {
            err(memberExpr.object, "There is no attribute named `%s` in class `%s`", memberExpr.member.name.toString(),
                    objectType);
            return ValueType.OBJECT_TYPE;
        }

        memberExpr.setInferredType(memberType);
        return memberType;
    }
    

    // like the callexpr
    private Type analyzeMethodCall(MethodCallExpr methodCallExpr) {
        Type receiverType = analyzeExpr(methodCallExpr.method.object);
        if (classSymTables.get(receiverType.toString()) == null) {
            err(methodCallExpr.method.member, "There is no method named `%s` in class `%s`",methodCallExpr.method.member.name, receiverType);
            return ValueType.OBJECT_TYPE;
        }
    
        ClassValueType classType = (ClassValueType) receiverType;
        SymbolTable<Type> classSym = classSymTables.get(receiverType.toString());
        // no such method
        if (classSym.get(methodCallExpr.method.member.name) == null) {
            err(methodCallExpr.method, "There is no method named `%s` in class `%s`", methodCallExpr.method.member.name, classType);
            return ValueType.OBJECT_TYPE;
        }
    
        FuncType methodType = (FuncType) classSym.get(methodCallExpr.method.member.name);

        int expectedParamCount = methodType.parameters.size() - 1; // 'self' parameter
        int actualArgCount = methodCallExpr.args.size();
    
        if (actualArgCount != expectedParamCount) {
            errors.semError(methodCallExpr, "Expected %d arguments; got %d", expectedParamCount, actualArgCount);
            return ValueType.OBJECT_TYPE;
        }
    
        List<ValueType> params = new ArrayList<>();
        params.add((ValueType) methodType.parameters.get(0));
        for (int i = 0; i < actualArgCount; i++) {
            Expr argExpr = methodCallExpr.args.get(i);
            Type argType = analyzeExpr(argExpr);
            Type expectedType = methodType.parameters.get(i + 1); //  'self' argument
            params.add((ValueType) argType);
            
            if (!Typecompatible(expectedType, argType)) {
                errors.semError(methodCallExpr, "Expected type `%s`; got type `%s` in parameter %d",
                                expectedType, argType, i+1);
            }
        }


        methodCallExpr.setInferredType(((FuncType) methodType).returnType);
        methodCallExpr.method.setInferredType(new FuncType(params,methodType.returnType));
        return methodType.returnType;
    }
    
    
    
}