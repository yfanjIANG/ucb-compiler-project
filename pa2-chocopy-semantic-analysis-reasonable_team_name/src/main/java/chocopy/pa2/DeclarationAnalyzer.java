package chocopy.pa2;

import static chocopy.common.analysis.types.Type.INT_TYPE;
import static chocopy.common.analysis.types.Type.OBJECT_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.ClassValueType;
import chocopy.common.analysis.types.FuncType;
import chocopy.common.analysis.types.Type;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.astnodes.ClassDef;
import chocopy.common.astnodes.ClassType;
import chocopy.common.astnodes.Declaration;
import chocopy.common.astnodes.Errors;
import chocopy.common.astnodes.FuncDef;
import chocopy.common.astnodes.Identifier;
import chocopy.common.astnodes.Program;
import chocopy.common.astnodes.TypedVar;
import chocopy.common.astnodes.VarDef;
import chocopy.common.astnodes.GlobalDecl;

/**
 * Analyzes declarations to create a top-level symbol table.
 *
 * Students should modify this class.
 */
public class DeclarationAnalyzer {

    /** Current symbol table.  Changes with new declarative region. */
    private SymbolTable<Type> sym = new SymbolTable<>();
    /** Global symbol table. */
    private final SymbolTable<Type> globals = sym;
    /** Receiver for semantic error messages. */
    private final Errors errors;
    private final Map<String, SymbolTable<Type>> classSymTables = new HashMap<>(); 

    /** A new declaration analyzer sending errors to ERRORS0. */
    public DeclarationAnalyzer(Errors errors0) {
        errors = errors0;
    }

    private record IdentifierAndType(Identifier id, Type type) {}


    
    public Map<String, SymbolTable<Type>> analyzeProgram(Program program) {
        List<ValueType> params = new ArrayList<ValueType>();
        params.add(ValueType.OBJECT_TYPE);
        sym = new SymbolTable<>(); 
        globals.put("print", new FuncType(params, ValueType.NONE_TYPE));
        globals.put("str", new FuncType(params, ValueType.NONE_TYPE));
        globals.put("len", new FuncType(params, ValueType.INT_TYPE));
        globals.put("input", new FuncType(params, ValueType.STR_TYPE));
        globals.put("int", new FuncType(params, ValueType.NONE_TYPE));
        globals.put("bool", new FuncType(params, ValueType.NONE_TYPE));
        globals.put("object", new FuncType(params, ValueType.NONE_TYPE));
        
        globals.put("bool", new ClassValueType("object"));
        globals.put("int", new ClassValueType("object"));
        globals.put("str", new ClassValueType("object"));
        globals.put("object", new ClassValueType("object"));


        for (Declaration decl : program.declarations) {

            analyzeDeclaration(decl);
        }
        classSymTables.put("THIS_IS_GLOBAL_SYMBOL", globals);

        return classSymTables;
    }

    public IdentifierAndType analyzeDeclaration(Declaration decl) {
        return switch (decl) {
            case VarDef varDef -> analyzeVarDef(varDef);
            case FuncDef funcDef -> analyzeFuncDef(funcDef);
            case ClassDef classDef -> analyzeClassDef(classDef);

            /* TODO: Add more cases here. */
            default -> {
                String className = decl.getClass().getCanonicalName();
                throw new UnsupportedOperationException(
                    "analyzeDeclaration not yet implemented for " + className);
            }
        };
    }
    //analyze if the vardef is duplicately defined
    public IdentifierAndType analyzeVarDef(VarDef varDef) {
        Identifier id = varDef.getIdentifier();
        String name = id.name;
        Type varType = ValueType.annotationToValueType(varDef.var.type);

        if (globals.declares(name)) {
            errors.semError(id, "Duplicate declaration of identifier in same scope: %s", name);
        } else {
            globals.put(name, varType);  
        }
    
        return new IdentifierAndType(id, varType);
    }
    //analyze if the funcdef is duplicately defined but do not step into the function to analyze the inner functiondef
    public IdentifierAndType analyzeFuncDef(FuncDef funcDef) {

        
        Identifier id = funcDef.getIdentifier();
        String name = id.name;
        ValueType returnType = ValueType.annotationToValueType(funcDef.returnType);


        if (globals.declares(name)) {
            errors.semError(id, "Duplicate declaration of identifier in same scope: %s", name);
        }
        

        List<ValueType> params = new ArrayList<>();
    
        for (TypedVar param : funcDef.params) {

            Type paramType = ValueType.annotationToValueType(param.type);
            params.add((ValueType) paramType);
            
        }

        FuncType funcType = new FuncType(params, returnType);
        globals.put(name, funcType);


        return new IdentifierAndType(id, returnType);
    }
    

    //analyze if the classdef is duplicately defined but do not step into the class to analyze the inner functiondef
    public IdentifierAndType analyzeClassDef(ClassDef classDef) {
        Identifier id = classDef.getIdentifier();
        String name = id.name;
        Type classType = new ClassValueType(name);

        // check duplicate declaration in global
        if (globals.declares(name)) {
            errors.semError(id, "Duplicate declaration of identifier in same scope: %s", name);
            return null; 
        } else {
            globals.put(name, classType); // add to the global symbol table
        }

        if (classDef.superClass != null) {
            String superClassName = classDef.superClass.name;

            // Superclass defined?
            if (superClassName.equals("int") || superClassName.equals("str") || superClassName.equals("bool")) {
                errors.semError(classDef.superClass, "Cannot extend special class: %s", superClassName);
            }else if(!globals.declares(superClassName) && !superClassName.equals("object")) {
                errors.semError(classDef.superClass, "Super-class not defined: %s", superClassName);
            } else {

                Type superClassType = globals.get(superClassName);
                if (!(superClassType instanceof ClassValueType) || 
                    superClassType.equals(ValueType.INT_TYPE) || 
                    superClassType.equals(ValueType.STR_TYPE) || 
                    superClassType.equals(ValueType.BOOL_TYPE)) {

                        if (!superClassName.equals("object")) {
                            errors.semError(classDef.superClass, "Super-class must be a class: %s", superClassName);

                        }
                }

            }
        }

       

        classSymTables.put(name, new SymbolTable<>());

        return new IdentifierAndType(id, classType);
    }


    
    
}