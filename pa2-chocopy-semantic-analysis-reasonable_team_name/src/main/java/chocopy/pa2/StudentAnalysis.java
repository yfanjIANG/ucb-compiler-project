package chocopy.pa2;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.Type;
import chocopy.common.astnodes.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/** Top-level class for performing semantic analysis. */
public class StudentAnalysis {

    /** Perform semantic analysis on PROGRAM, adding error messages and
     *  type annotations. Provide debugging output iff DEBUG. Returns modified
     *  tree. */
    public static Program process(Program program, boolean debug) {
        if (program.hasErrors()) {
            return program;
        }

        try {
            DeclarationAnalyzer declarationAnalyzer =
                new DeclarationAnalyzer(program.errors);
                Map<String, SymbolTable<Type>> globalSym =
                declarationAnalyzer.analyzeProgram(program);

            
            TypeChecker typeChecker =
                new TypeChecker(globalSym, program.errors);
            typeChecker.analyzeAndAddTypes(program);
            
        } catch (UnsupportedOperationException e) {
            System.out.println("Typechecking failed with error: " + e.getMessage());
        }

        return program;
    }
}
