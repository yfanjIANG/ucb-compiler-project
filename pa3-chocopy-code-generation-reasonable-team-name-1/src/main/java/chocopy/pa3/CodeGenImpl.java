package chocopy.pa3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chocopy.pa3.RiscV.Register;
import chocopy.pa3.RiscVAsmWriter.PhysicalRegister;
import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.ClassValueType;
import chocopy.common.analysis.types.ListValueType;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.astnodes.*;
import chocopy.common.codegen.ClassInfo;
import chocopy.common.codegen.FuncInfo;
import chocopy.common.codegen.GlobalVarInfo;
import chocopy.common.codegen.Label;
import chocopy.common.codegen.StackVarInfo;
import chocopy.common.codegen.SymbolInfo;

import static chocopy.pa3.RiscVAsmWriter.PhysicalRegister.*;



/**
 * This is where the main implementation of PA3 will live.
 *
 * A large part of the functionality has already been implemented
 * in the base class, CodeGenBase. Make sure to read through that
 * class, since you will want to use many of its fields
 * and utility methods in this class when emitting code.
 *
 * Also read the PDF spec for details on what the base class does and
 * what APIs it exposes for its sub-class (this one). Of particular
 * importance is knowing what all the SymbolInfo classes contain.
 */
public class CodeGenImpl extends CodeGenBase {

    /** A code generator emitting instructions to asmWriter. */
    public CodeGenImpl(RiscVAsmWriter asmWriter) {
        super(asmWriter);
    }
    
    private boolean string_exist = false;
    /** Operation on None. */
    private final Label errorNone = new Label("error.None");
    /** Division by zero. */
    private final Label errorDiv = new Label("error.Div");
    /** Index out of bounds. */
    private final Label errorOob = new Label("error.OOB");

    private final Label label_int = new Label("makeint");

    private final Label label_bool = new Label("makebool");
    
    private final Label label_conslist = new Label("conslist");

    private final Label label_streql = new Label("streql");
    
    private final Label label_strneql = new Label("strneql");

    private final Label label_allChars = new Label("allChars");

    private final Label label_initchars = new Label("initchars");
    
    private final Label label_concat = new Label("concat");
    
    private final Label label_noconv = new Label("noconv");

    private final Label label_strcat = new Label("strcat");









    /**
     * Emits the top level of the program.
     *
     * This method is invoked exactly once, and is surrounded
     * by some boilerplate code that: (1) initializes the heap
     * before the top-level begins and (2) exits after the top-level
     * ends.
     *
     * You only need to generate code for statements.
     *
     * @param statements top level statements
     */
    protected void emitTopLevel(List<Stmt> statements) {
        
        StmtsToRiscV stmtsToRiscV = new StmtsToRiscV(null);
        String mainlabel = "@..main.size";
        asmWriter.emitADDI(SP, SP, "-"+mainlabel, "Saved FP and saved RA (unused at top level).");
        asmWriter.emitSW(ZERO, SP, mainlabel+"-4", "Top saved FP is 0.");
        asmWriter.emitSW(ZERO, SP, mainlabel+"-8", "Top saved RA is 0.");
        asmWriter.emitADDI(FP, SP, mainlabel, "Set FP to previous SP.");
        asmWriter.emitJAL(label_initchars, "Initialize one-character strings.");
        int stmtNum = 0;
        for (Stmt stmt : statements) {
            stmtsToRiscV.dispatchStmt(stmt);
            stmtNum++;
        }
        asmWriter.defineSym(mainlabel, stmtNum * 16 * asmWriter.getWordSize());
        asmWriter.emitLI(A0, EXIT_ECALL, "Code for ecall: exit");
        asmWriter.emitEcall(null);
    }

    /**
     * Emits the code for a function described by FUNCINFO.
     *
     * This method is invoked once per function and method definition.
     * At the code generation stage, nested functions are emitted as
     * separate functions of their own. So if function `bar` is nested within
     * function `foo`, you only emit `foo`'s code for `foo` and only emit
     * `bar`'s code for `bar`.
     */
    protected void emitUserDefinedFunction(FuncInfo funcInfo) {
        asmWriter.emitGlobalLabel(funcInfo.getCodeLabel());
        StmtsToRiscV stmtsToRiscV = new StmtsToRiscV(funcInfo);
        stmtsToRiscV.equivEmitted = 0;
        stmtsToRiscV.localNum = 3;
        String sizelabel = "@" + funcInfo.getFuncName() + ".size";
        asmWriter.emitADDI(SP, SP, "-" + sizelabel, "Reserve space for stack frame.");
        asmWriter.emitSW(RA, SP, sizelabel + "-4", "return address");
        asmWriter.emitSW(FP, SP, sizelabel + "-8", "control link");
        
        asmWriter.emitADDI(FP, SP, sizelabel, "New FP is at old SP.");

        for (StackVarInfo localVar : funcInfo.getLocals()) {
            stmtsToRiscV.stack_slot++;
            Literal value = localVar.getInitialValue();
            stmtsToRiscV.dispatchExpr(value);
            asmWriter.emitSW(A0, FP, -(stmtsToRiscV.stack_slot - 1) * asmWriter.getWordSize(), "local variable " + localVar.getVarName());
        }
        for (Stmt stmt : funcInfo.getStatements()) {
            stmtsToRiscV.dispatchStmt(stmt);
        }

        asmWriter.emitMV(A0, ZERO, "Returning None implicitly");
        asmWriter.emitLocalLabel(stmtsToRiscV.epilogue, "Epilogue");

        // FIXME: {... reset fp etc. ...}

        asmWriter.defineSym(sizelabel, (2 + 1 + stmtsToRiscV.localNum + stmtsToRiscV.equivEmitted + 1 ) * asmWriter.getWordSize());
        
        asmWriter.emitLW(RA, FP, -4, "Get return address");
        asmWriter.emitLW(FP, FP, -8, "Use control link to restore caller's fp");
        asmWriter.emitADDI(SP, SP, sizelabel, "Restore stack pointer");


        asmWriter.emitJR(RA, "Return to caller");
    }

    
    

    /** An analyzer that encapsulates code generation for statements. */
    private class StmtsToRiscV extends AstVisitor {
        /*
         * The symbol table has all the info you need to determine
         * what a given identifier 'x' in the current scope is. You can
         * use it as follows:
         *   SymbolInfo x = sym.get("x");
         *
         * A SymbolInfo can be one the following:
         * - ClassInfo: a descriptor for classes
         * - FuncInfo: a descriptor for functions/methods
         * - AttrInfo: a descriptor for attributes
         * - GlobalVarInfo: a descriptor for global variables
         * - StackVarInfo: a descriptor for variables allocated on the stack,
         *      such as locals and parameters
         *
         * Since the input program is assumed to be semantically
         * valid and well-typed at this stage, you can always assume that
         * the symbol table contains valid information. For example, in
         * an expression `foo()` you KNOW that sym.get("foo") will either be
         * a FuncInfo or ClassInfo, but not any of the other infos
         * and never null.
         *
         * The symbol table in funcInfo has already been populated in
         * the base class: CodeGenBase. You do not need to add anything to
         * the symbol table. Simply query it with an identifier name to
         * get a descriptor for a function, class, variable, etc.
         *
         * The symbol table also maps nonlocal and global vars, so you
         * only need to lookup one symbol table and it will fetch the
         * appropriate info for the var that is currently in scope.
         */

        /** Symbol table for my statements. */
        private SymbolTable<SymbolInfo> sym;

        protected final Map<Label, PhysicalRegister> regMap = globalVar;

        /** Label of code that exits from procedure. */
        protected Label epilogue;

        protected int equivEmitted = 0;
        protected int localNum;
        private int stack_slot;

        /** The descriptor for the current function, or null at the top
         *  level. */
        private FuncInfo funcInfo;
        private final String size_label;
        /** An analyzer for the function described by FUNCINFO0, which is null
         *  for the top level. */
        StmtsToRiscV(FuncInfo funcInfo0) {
            funcInfo = funcInfo0;
            if (funcInfo == null) {
                sym = globalSymbols;
                size_label = "@..main.size";
                stack_slot = 3;
            } else {
                sym = funcInfo.getSymbolTable();
                size_label = "@" + funcInfo0.getFuncName() + ".size";
                stack_slot = 3;
            }
            equivEmitted = 0;
            epilogue = generateLocalLabel();
        }

        public void visit(ExprStmt stmt) {
            dispatchExpr(stmt.expr);
        }
        @Override
        public void visit(AssignStmt assignStmt) {
            dispatchExpr(assignStmt.value);

            for (Expr target : assignStmt.targets) {
                if(target instanceof Identifier){
                    
                    String varName = ((Identifier)target).name;
                    SymbolInfo symbolInfo = sym.get(varName);
                    if (symbolInfo instanceof StackVarInfo) {
                        SymbolTable<SymbolInfo> current_symbolTable = sym;
                        FuncInfo current_functInfo = funcInfo;
                        if (!current_symbolTable.declares(varName)) {
                            current_symbolTable = current_symbolTable.getParent();
                            asmWriter.emitLW(T0, FP, current_functInfo.getParams().size() * asmWriter.getWordSize(), "Load static link");
                            current_functInfo = current_functInfo.getParentFuncInfo();
                            while (!current_symbolTable.declares(varName)) {
                                current_symbolTable = current_symbolTable.getParent();
                                asmWriter.emitLW(T0, T0, current_functInfo.getParams().size() * asmWriter.getWordSize(),
                                        "Load static link");
                                current_functInfo = current_functInfo.getParentFuncInfo();
                            }
                            int index = current_functInfo.getVarIndex(varName);
                            asmWriter.emitSW(A0, T0, (current_functInfo.getParams().size() - 1 - index) * asmWriter.getWordSize(), "Store local var: " + varName);
                        }
                        else{
                            int index = current_functInfo.getVarIndex(varName);
                            asmWriter.emitSW(A0, FP, (current_functInfo.getParams().size() - 1 - index) * asmWriter.getWordSize(), "Store local var: " + varName);
                        }
                        
                    }

                    
                    if (symbolInfo instanceof GlobalVarInfo) {
                        if (globalVar.get(((GlobalVarInfo) symbolInfo).getLabel()) != null) {
                            PhysicalRegister reg = globalVar.get(((GlobalVarInfo) symbolInfo).getLabel());
                            asmWriter.emitMV(reg, A0, "Store global var: " + varName);
                        } else {
                            asmWriter.emitSW(A0, ((GlobalVarInfo) symbolInfo).getLabel(), T0, "Store global var: " + varName);
                        }
                    }
                }
                else if (target instanceof IndexExpr){
                    IndexExpr indexexpr = (IndexExpr)target;
                    Label checkOOBLabel = generateLocalLabel();
                    Label noErrorLabel = generateLocalLabel();
                    asmWriter.emitSW(A0, FP, -stack_slot * asmWriter.getWordSize(), "Push on stack slot " + stack_slot);
                    stack_slot++;
                    dispatchExpr(indexexpr.list);
                    asmWriter.emitSW(A0, FP, -stack_slot * asmWriter.getWordSize(), "Push on stack slot " + stack_slot);
                    stack_slot++;
                    dispatchExpr(indexexpr.index);
                    stack_slot--;
                    asmWriter.emitLW(T0, FP, -stack_slot * asmWriter.getWordSize(), "Pop stack slot " + stack_slot);
                    stack_slot--;
                    asmWriter.emitLW(T1,FP,-stack_slot * asmWriter.getWordSize(),"Pop stack slot " + stack_slot);


        
                    asmWriter.emitBNEZ(T0, checkOOBLabel, "Ensure not None");
                    asmWriter.emitJ(errorNone, "Go to error handler");
        
                    asmWriter.emitLocalLabel(checkOOBLabel, "Not none ");
                    asmWriter.emitLW(T2, T0, "@.__len__", "Load attribute: __len__");
                    asmWriter.emitBLTU(A0, T2, noErrorLabel, "Ensure 0 <= index < len");
                    asmWriter.emitJ(errorOob, "Go to error handler");
        
                    asmWriter.emitLocalLabel(noErrorLabel, "Index within bounds");
                    asmWriter.emitADDI(A0, A0, 4, "Compute list element offset in words");
                    asmWriter.emitLI(T2, 4, "Word size in bytes");
                    asmWriter.emitMUL(A0, A0, T2, "Compute list element offset in bytes");
                    asmWriter.emitADD(A0, T0, A0, "Pointer to list element");
                    asmWriter.emitSW(T1, A0, 0, "Set list element");
                }
                else{
                    MemberExpr memberexpr = (MemberExpr) target;
                    asmWriter.emitSW(A0, FP, -(stack_slot) * asmWriter.getWordSize(), "Push on stack slot 3");
                    stack_slot++;
                    dispatchExpr(memberexpr.object);
                    stack_slot--;
                    asmWriter.emitMV(A1, A0,  "Move object");
                    asmWriter.emitLW(A0, FP, -(stack_slot) * asmWriter.getWordSize(), "Pop stack slot 3");
                    Label label = generateLocalLabel();
                    asmWriter.emitBNEZ(A1, label, "Ensure not None");
                    asmWriter.emitJ(errorNone, "Go to error handler");
                    asmWriter.emitLocalLabel(label, "Not None");
                    ClassInfo classInfo = (ClassInfo) globalSymbols.get(memberexpr.object.getInferredType().className());
                    String attrname = memberexpr.member.name;
                    asmWriter.emitSW(A0, A1, getAttrOffset(classInfo,attrname), "Set attribute");

                }
                
            }
        }

        @Override
        public void visit(ReturnStmt stmt) {
            // FIXME: Here, we emit an instruction that does nothing. Clearly,
            // this is wrong, and you'll have to fix it.
            // This is here just to demonstrate how to emit a
            // RISC-V instruction.
            if (stmt.value == null) {
                asmWriter.emitMV(A0, ZERO, "Returning None implicitly");
            } else {
                dispatchExpr(stmt.value);
            }
            asmWriter.emitJ(epilogue, "Go to return");
        }

        
        

        @Override
        public void visit(UnaryExpr unaryExpr) {
            dispatchExpr(unaryExpr.operand);
            switch (unaryExpr.operator) {
                case "-":
                    asmWriter.emitSUB(A0, ZERO, A0, "Get negative number.");
                    break;
                case "not":
                    asmWriter.emitXORI(A0, A0, 1, "Flip the expr value");
                    break;
                default:
                    break;
            }
        }
        private boolean is_special_int(Expr expr, int value) {
            return expr instanceof Literal && (((IntegerLiteral) expr).value) == value;
        }
        private boolean isPowerOfTwo(Expr expr) {
            if (expr instanceof Literal ) {
                int value = ((IntegerLiteral) expr).value;
                return value > 0 && (value & (value - 1)) == 0;
            }
            return false;
        }

        @Override
        public void visit(BinaryExpr binaryExpr) {
            if (binaryExpr.operator.equals("+")) {
                if (is_special_int(binaryExpr.right, 0)) {
                    dispatchExpr(binaryExpr.left);
                    return; 
                }
                if (is_special_int(binaryExpr.left, 0)) {
                    dispatchExpr(binaryExpr.right);
                    return;
                }
            } else if (binaryExpr.operator.equals("*")) {
                if (is_special_int(binaryExpr.right, 1)) {
                    dispatchExpr(binaryExpr.left);
                    return;
                }
                if (is_special_int(binaryExpr.left, 1)) {
                    dispatchExpr(binaryExpr.right);
                    return;
                }
                if (is_special_int(binaryExpr.left, 0) || is_special_int(binaryExpr.right, 0)) {
                    asmWriter.emitLI(A0, 0, " x * 0 to 0");
                    return;
                }
                
            } else if (binaryExpr.operator.equals("**")) {
                if (is_special_int(binaryExpr.right, 2)) {
                    dispatchExpr(binaryExpr.left);
                    asmWriter.emitSW(A0, FP, -stack_slot * asmWriter.getWordSize(), "Save x for x ** 2");
                    stack_slot++;
                    dispatchExpr(binaryExpr.left); 
                    stack_slot--;
                    asmWriter.emitLW(T0, FP, -stack_slot * asmWriter.getWordSize(), "Load saved x");
                    asmWriter.emitMUL(A0, T0, A0, " x ** 2 to x * x");
                    return;
                }
            }
            if (!binaryExpr.operator.equals("and") && !binaryExpr.operator.equals("or")
            && !binaryExpr.left.getInferredType().equals(ClassValueType.STR_TYPE) && !binaryExpr.right.getInferredType().equals(ClassValueType.STR_TYPE)
            && !binaryExpr.left.getInferredType().isListType() && !binaryExpr.right.getInferredType().isListType()
            ) {
                dispatchExpr(binaryExpr.left);
                asmWriter.emitSW(A0, FP, -stack_slot * asmWriter.getWordSize(), "Save left expression value.");
                stack_slot++;
                dispatchExpr(binaryExpr.right);
                stack_slot--;
                asmWriter.emitLW(T0, FP, -stack_slot * asmWriter.getWordSize(), "Load left expression value.");
            }


            switch (binaryExpr.operator) {
                case "+":
                    if((binaryExpr.left.getInferredType().isListType() && binaryExpr.right.getInferredType().isListType()) ||
                    (binaryExpr.left.getInferredType().equals(ClassValueType.STR_TYPE) && binaryExpr.right.getInferredType().equals(ClassValueType.STR_TYPE))
                    ){
                        if(binaryExpr.left.getInferredType().isListType() && binaryExpr.right.getInferredType().isListType()){
                            asmWriter.emitLA(T0, label_noconv, "Identity conversion");
                            asmWriter.emitSW(T0, FP, -stack_slot * asmWriter.getWordSize(), "Push argument 3 from last.");
                            stack_slot++;
                            asmWriter.emitLA(T0, label_noconv, "Identity conversion");
                            asmWriter.emitSW(T0, FP, -stack_slot * asmWriter.getWordSize(), "Push argument 2 from last.");
                            stack_slot++;
                        }
                           
                
                        dispatchExpr(binaryExpr.left);
                        asmWriter.emitSW(A0, FP, -stack_slot * asmWriter.getWordSize(), "Push argument 1 from last.");
                        stack_slot++;
                        dispatchExpr(binaryExpr.right);
                        asmWriter.emitSW(A0, FP, -stack_slot * asmWriter.getWordSize(), "Push argument 0 from last.");
                        asmWriter.emitADDI(SP, FP, -stack_slot * asmWriter.getWordSize(), "Set SP to last argument.");
                        stack_slot++;
                        if(binaryExpr.left.getInferredType().isListType() && binaryExpr.right.getInferredType().isListType()){
                            asmWriter.emitJAL(label_concat, "Call runtime concatenation routine.");
                        }
                        else{
                            asmWriter.emitJAL(label_strcat, "Call runtime concatenation routine.");
                        }

                        asmWriter.emitADDI(SP, FP, "-" + size_label, "Set SP to stack frame top");
                        if(binaryExpr.left.getInferredType().isListType() && binaryExpr.right.getInferredType().isListType()){
                            stack_slot-=4;
                        }
                        else{
                            stack_slot-=2;
                        }
                    }

                    else{
                        asmWriter.emitADD(A0, T0, A0, "Add");
                    }
                    break;
                case "-":
                    asmWriter.emitSUB(A0, T0, A0, "Sub");
                    break;
                case "*":
                    asmWriter.emitMUL(A0, T0, A0, "Multiply");
                    break;
                case "==":
                    if (binaryExpr.left.getInferredType().equals(ClassValueType.BOOL_TYPE) && binaryExpr.right.getInferredType().equals(ClassValueType.BOOL_TYPE)){
                        asmWriter.emitXOR(A0, T0, A0, "Operator ==");
                        asmWriter.emitSEQZ(A0, A0, "Set result to 1 if equal)");
                        
                    }
                    else if (binaryExpr.left.getInferredType().equals(ClassValueType.INT_TYPE) && binaryExpr.right.getInferredType().equals(ClassValueType.INT_TYPE)){
                        asmWriter.emitXOR(A0, T0, A0, "Operator =="); 
                        asmWriter.emitSEQZ(A0, A0, "Operator == (..contd)");
                    }
                    else {
                        dispatchExpr(binaryExpr.left);
                        asmWriter.emitSW(A0, FP,-stack_slot * asmWriter.getWordSize(), "Push argument 1 from last.");
                        stack_slot++;
                        dispatchExpr(binaryExpr.right);
                        asmWriter.emitSW(A0, FP,-stack_slot * asmWriter.getWordSize(), "Push argument 0 from last.");
                        asmWriter.emitADDI(SP, FP, -stack_slot * asmWriter.getWordSize(), "Set SP to last argument111.");
                        stack_slot--;
                        asmWriter.emitJAL(label_streql, null);
                        asmWriter.emitADDI(SP, FP, "-" + size_label, "Set SP to stack frame top.");

                    }
                    break;
                case "!=":
                    if (binaryExpr.left.getInferredType().equals(ClassValueType.BOOL_TYPE) && binaryExpr.right.getInferredType().equals(ClassValueType.BOOL_TYPE)){
                        asmWriter.emitXOR(A0, T0, A0, "Operator !="); 
                        asmWriter.emitSNEZ(A0, A0, "Set result to 1 if not equal");

                    }
                    else if (binaryExpr.left.getInferredType().equals(ClassValueType.INT_TYPE) && binaryExpr.right.getInferredType().equals(ClassValueType.INT_TYPE)){
                        asmWriter.emitXOR(A0, T0, A0, "Operator !="); 
                        asmWriter.emitSNEZ(A0, A0, "Operator != (..contd)");
                    }
                    else {
                        dispatchExpr(binaryExpr.left);
                        asmWriter.emitSW(A0, FP,-stack_slot * asmWriter.getWordSize(), "Push argument  1  from last.");
                        stack_slot++;
                        dispatchExpr(binaryExpr.right);
                        asmWriter.emitSW(A0, FP,-stack_slot * asmWriter.getWordSize(), "Push argument  0  from last.");
                        asmWriter.emitADDI(SP, FP, -stack_slot * asmWriter.getWordSize(), "Set SP to last argument222.");
                        stack_slot--;
                        asmWriter.emitJAL(label_strneql, null);
                        asmWriter.emitADDI(SP, FP, "-" + size_label, "Set SP to stack frame top.");
                    }
                    break;
                case "<":
                    asmWriter.emitSLT(A0, T0, A0, "Operator <"); 
                    break;
                case "<=":
                    asmWriter.emitSLT(A0, A0, T0, "Operator <="); 
                    asmWriter.emitSEQZ(A0, A0, "Operator <= (..contd)");
                    break;
                case ">":
                    asmWriter.emitSLT(A0, A0, T0, "Operator >"); 
                    break;
                case ">=":
                    asmWriter.emitSLT(A0, T0, A0, "Operator >="); 
                    asmWriter.emitSEQZ(A0, A0, "Operator >= (..contd)");
                    break;
                case "//":
                    Label notZeroLabel = generateLocalLabel();
 
                    asmWriter.emitBNEZ(A0, notZeroLabel, "Ensure non-zero divisor");
                    asmWriter.emitJ(errorDiv, "Go to error handler");
                
                    asmWriter.emitLocalLabel(notZeroLabel, "Divisor is non-zero");
                    asmWriter.emitDIV(A0, T0, A0, "Operater //");
                    
                    break;
                
                case "%":
                    Label notZeroLabel_for_rem = generateLocalLabel();
                    asmWriter.emitBNEZ(A0, notZeroLabel_for_rem, "Ensure non-zero divisor");
                    asmWriter.emitJ(errorDiv, "Go to error handler");
                    //Divisor is non-zero
                    asmWriter.emitLocalLabel(notZeroLabel_for_rem, "Divisor is non-zero");
                    asmWriter.emitREM(A0, T0, A0, "Operater %");

                    
                    break;
                case "and": 
                    Label falseLabel = generateLocalLabel();
                    dispatchExpr(binaryExpr.left);
                    // short circuit if left is false
                    asmWriter.emitBEQZ(A0, falseLabel, "Short-circuit 'and' if left is false");
                    // else caculate right
                    dispatchExpr(binaryExpr.right);
                    asmWriter.emitAND(A0,A0,T0,"and operation");
                    asmWriter.emitLocalLabel(falseLabel, null);
                    break;
                    
                case "or": 
                    Label trueLabel = generateLocalLabel();
                    dispatchExpr(binaryExpr.left);
                    // short circuit if left is true
                    asmWriter.emitBNEZ(A0, trueLabel, "Short-circuit 'or' if left is true");
                    // else caculate right
                    dispatchExpr(binaryExpr.right);
                    asmWriter.emitAND(A0, A0, T0, "or operation");
                    asmWriter.emitLocalLabel(trueLabel, null);
                    break;

                case "is":
                    asmWriter.emitXOR(A0, T0, A0, "Compare references");
                    asmWriter.emitSEQZ(A0, A0, "Operator is");
                    break;
            
                default :
                    throw new UnsupportedOperationException(
                        "visit(BinaryExpr) not implemented for " + binaryExpr.operator);
            }
        }

        @Override
        public void visit(MemberExpr memberExpr) 
        {
            ClassInfo classInfo = (ClassInfo) globalSymbols.get(memberExpr.object.getInferredType().className());
            Label label = generateLocalLabel();

            dispatchExpr(memberExpr.object);
            asmWriter.emitBNEZ(A0, label, "Ensure not None");
            asmWriter.emitJ(errorNone, "Go to error handler");
            asmWriter.emitLocalLabel(label, "Not None");
            asmWriter.emitLI(T5, getAttrOffset(classInfo, memberExpr.member.name), "Store attribute address");
            asmWriter.emitLW(A0, A0, getAttrOffset(classInfo, memberExpr.member.name),"Get value of attribute: "+ classInfo.getClassName()+"."+memberExpr.member.name);
        }

        boolean flag =false;

        @Override
        public void visit(CallExpr callExpr) {
            String callName = callExpr.function.name;
            SymbolInfo calleeFunctionInfo = sym.get(callName);
            if(calleeFunctionInfo instanceof ClassInfo){
                ClassInfo Class = (ClassInfo) calleeFunctionInfo;
                if (Class.getClassName() == "int" || Class.getClassName() == "bool") {
                    asmWriter.emitMV(A0, ZERO, "Special cases: int and bool unboxed.");
                } else {
                    
                    asmWriter.emitLA(A0, Class.getPrototypeLabel(), "Load pointer to prototype of: " + Class.getClassName());
                    asmWriter.emitJAL(objectAllocLabel, "Allocate new object in A0");
                    asmWriter.emitSW(A0, FP, -(stack_slot) * asmWriter.getWordSize(), "Push on stack slot " + stack_slot);
                    stack_slot++;
                    asmWriter.emitSW(A0, FP, -(stack_slot) * asmWriter.getWordSize(), "Push argument 0 from last.");
                    asmWriter.emitADDI(SP, FP, -(stack_slot) * asmWriter.getWordSize(), "Set SP to last argument.");
                    asmWriter.emitLW(A1, A0, getDispatchTableOffset(), "Load address of object's dispatch table");
                    asmWriter.emitLW(A1, A1, getMethodOffset(Class, "__init__"), "Load address of method: " + Class.getClassName() + "__init__");
                    asmWriter.emitJALR(A1, "Invoke method: " + Class.getClassName()+ "__init__");
                    asmWriter.emitADDI(SP, FP, "-"+size_label, "Set SP to stack frame top.");
                    stack_slot--;
                    asmWriter.emitLW(A0, FP, -(stack_slot) * asmWriter.getWordSize(), "Pop stack slot " + stack_slot);
                }
            }
            else{
                FuncInfo function = (FuncInfo)calleeFunctionInfo;
                if (funcInfo != null && !callName.equals(function.getFuncName())) {
                    int jump = funcInfo.getDepth() - function.getDepth() + 1;
                    asmWriter.emitMV(T0, FP, "Get static link to " + funcInfo.getFuncName());
    
                    FuncInfo curFuncInfo = funcInfo;
                    for (int i = 0; i < jump; i++) {
                        if (curFuncInfo == null) break;
                        int offset = curFuncInfo.getParams().size() * asmWriter.getWordSize();
                        asmWriter.emitLW(T0, T0, offset, "Get static link to " + curFuncInfo.getFuncName());
                        curFuncInfo = curFuncInfo.getParentFuncInfo();
                    }
                    asmWriter.emitSW(T0, FP, -(stack_slot) * asmWriter.getWordSize(), "Push Static link");
                }

    
                stack_slot++;

                int argSize = callExpr.args.size();
                for (int i = 0 ; i < callExpr.args.size() ; i++) {

                    Expr e = callExpr.args.get(i);
                    String param_name = function.getParams().get(i);
                    StackVarInfo param_info = (StackVarInfo) function.getSymbolTable().get(param_name);
                    int ori_stack_slot = stack_slot;
                    if(e instanceof CallExpr){
                        flag = true;
                    }
                    dispatchExpr(e);
                    if (flag) {
                        stack_slot = ori_stack_slot;
                    }
                    if (e.getInferredType().equals(ClassValueType.INT_TYPE) && param_info.getVarType().equals(ClassValueType.OBJECT_TYPE)) {
                        asmWriter.emitJAL(label_int, "Box integer");
                    }
                    if (e.getInferredType().equals(ClassValueType.BOOL_TYPE) && param_info.getVarType().equals(ClassValueType.OBJECT_TYPE)) {
                        asmWriter.emitJAL(label_bool, "Box boolean");
                    }
                    asmWriter.emitSW(A0, FP, -(stack_slot) * asmWriter.getWordSize(),
                            "Push argument " + i + " from last.");
                    stack_slot++;
                    
                }
                stack_slot--;


                asmWriter.emitADDI(SP, FP, -(stack_slot) * asmWriter.getWordSize(), "Set SP to last argument333.");
                
                if (funcInfo != null) {
                    equivEmitted = argSize;
                }
                asmWriter.emitJAL(function.getCodeLabel(), "Invoke function: " + callName);
                asmWriter.emitADDI(SP, FP, "-" + size_label, "Set SP to stack frame top.");
                stack_slot-=argSize;
            }
            
        }
        @Override
        public void visit(MethodCallExpr methodCallExpr) 
        {
            dispatchExpr(methodCallExpr.method.object);
            int methodargSize = methodCallExpr.args.size();
            
            Label label = generateLocalLabel();
            asmWriter.emitBNEZ(A0, label, "Ensure not None");
            asmWriter.emitJ(errorNone, "Go to error handler");
            asmWriter.emitLocalLabel(label, "Not None");
            stack_slot = stack_slot + methodargSize + 1;
            asmWriter.emitSW(A0, FP, (methodargSize - stack_slot) * asmWriter.getWordSize(),"Push argument %d from last." + methodargSize);

            for (int i = 0; i < methodargSize; i++) {
                dispatchExpr(methodCallExpr.args.get(i));
                asmWriter.emitSW(A0, FP, (methodargSize - i - 1 - stack_slot) * asmWriter.getWordSize(), null);
            }
            asmWriter.emitLW(A0, FP, (methodargSize- stack_slot) * asmWriter.getWordSize(),"Peek stack slot " + (stack_slot - (methodargSize + 1)));
            ClassInfo classInfo = null;
            if(methodCallExpr.method.object instanceof CallExpr)
                classInfo = (ClassInfo)globalSymbols.get(((CallExpr)methodCallExpr.method.object).getInferredType().className());
            else 
                classInfo = (ClassInfo)globalSymbols.get(methodCallExpr.method.object.getInferredType().className());
            asmWriter.emitLW(A1, A0, getDispatchTableOffset(), "Load address of object's dispatch table");
            asmWriter.emitLW(A1, A1, getMethodOffset(classInfo, methodCallExpr.method.member.name), 
                String.format("Load address of method: %s.%s", classInfo.getClassName(), methodCallExpr.method.member.name));
            asmWriter.emitADDI(SP, FP, -stack_slot * asmWriter.getWordSize(), "Set SP to last argument.");
            asmWriter.emitJALR(A1, "Invoke method: " + classInfo.getClassName() + "." + methodCallExpr.method.member.name);
            asmWriter.emitADDI(SP, FP, "-" + size_label, "Set SP to stack frame top");
            stack_slot -= methodargSize+1;
    
        }
        // FIXME: Example of statement.
        
        @Override
        public void visit(ListExpr listexpr) {
            int listSize = listexpr.elements.size();
            for (int i = 0; i < listSize; i++) {
                dispatchExpr(listexpr.elements.get(i));
                
                asmWriter.emitSW(A0, FP, -(stack_slot) * asmWriter.getWordSize(), "Load global var: ");
                stack_slot++;
            }
            

            asmWriter.emitLI(A0, listSize, "Load list length");
            asmWriter.emitSW(A0, FP, -(stack_slot) * asmWriter.getWordSize(), "Load global var: ");
            asmWriter.emitADDI(SP, FP, -(stack_slot) * asmWriter.getWordSize(), "Set SP to last argument444.");

            asmWriter.emitJAL(label_conslist, "Move values to new list object");

            asmWriter.emitADDI(SP, FP, "-" + size_label, "Set SP to stack frame top.");
            stack_slot-=listSize;
        }

        @Override
        public void visit(IndexExpr indexexpr) {

                Label checkOOBLabel = generateLocalLabel();
                Label noErrorLabel = generateLocalLabel();
                if(indexexpr.list.getInferredType().isListType()){
                    dispatchExpr(indexexpr.list);
                    asmWriter.emitSW(A0,FP,-stack_slot * asmWriter.getWordSize(),"Push on stack slot 5");
                    stack_slot++;
                    dispatchExpr(indexexpr.index);
                    stack_slot--;
                    asmWriter.emitLW(A1,FP,-stack_slot * asmWriter.getWordSize(),"Pop on stack slot 5");


        
                    // ensure list pointer is not None
                    asmWriter.emitBNEZ(A1, checkOOBLabel, "Ensure list is not None");
                    asmWriter.emitJ(errorNone, "Throw operation on None error.");
        
                    // ensure index is within bound
                    asmWriter.emitLocalLabel(checkOOBLabel, "List is not none. Now check index bound");
                    asmWriter.emitLW(T0, A1, "@.__len__", "Load attribute: __len__");
                    asmWriter.emitBLTU(A0, T0, noErrorLabel, "Ensure 0 <= index < len");
                    asmWriter.emitJ(errorOob, "Throw index out of bound error");
        
                    // list item selection
                    asmWriter.emitLocalLabel(noErrorLabel, "Index within bound, so compute list selection");
                    asmWriter.emitADDI(A0, A0, 4, "Word size in bytes");
                    asmWriter.emitLI(T0, 4, "Word size in bytes");
                    asmWriter.emitMUL(A0, A0, T0, "Compute list element offset in bytes");
                    asmWriter.emitADD(A0, A1, A0, "Pointer to list element");
                    // read value of list element
                    asmWriter.emitLW(A0, A0, 0, "Get list element");
                }
                else{
                    dispatchExpr(indexexpr.list);
                    asmWriter.emitSW(A0,FP,-stack_slot * asmWriter.getWordSize(),"Push on stack slot 5");
                    stack_slot++;
                    dispatchExpr(indexexpr.index);
                    stack_slot--;
                    asmWriter.emitLW(A1,FP,-stack_slot * asmWriter.getWordSize(),"Pop on stack slot 5");
                    asmWriter.emitLW(T0, A1, "@.__len__", "Load attribute: __len__");
                    asmWriter.emitBLTU(A0, T0, noErrorLabel, "Ensure 0 <= index < len");
                    asmWriter.emitJ(errorOob, "Throw index out of bound error");
                    asmWriter.emitLocalLabel(noErrorLabel, "Index within bound, so compute list selection");
                    stack_slot++;
                    asmWriter.emitSW(A0,FP,-stack_slot * asmWriter.getWordSize(),"Push on stack slot 4");
                    asmWriter.emitLW(T0,FP,-stack_slot * asmWriter.getWordSize(),"Pop on stack slot 4");
                    stack_slot--;
                    asmWriter.emitLW(A1,FP,-stack_slot * asmWriter.getWordSize(),"Pop on stack slot 2");
                    asmWriter.emitADDI(T0, T0, 16, "Word size in bytes");
                    asmWriter.emitADD(T0, A1, T0, "Get pointer to char");
                    asmWriter.emitLBU(T0, T0, 0, "Load character");
                    asmWriter.emitLI(T1, 20, "Word size in bytes");
                    asmWriter.emitMUL(T0, T0, T1, "Multiply by size of string object");
                    asmWriter.emitLA(A0,label_allChars,"Index into single-char table");
                    string_exist=true;
                    asmWriter.emitADD(A0, A0, T0, "Get pointer to char");


                }
                

        }

        // FIXME: More, of course.
        @Override
        public void visit(BooleanLiteral booleanLiteral) {
            asmWriter.emitLI(A0, booleanLiteral.value ? 1 : 0, "Load boolean literal " + booleanLiteral.value);
        }
        @Override
        public void visit(IntegerLiteral integerLiteral) {
            asmWriter.emitLI(A0, integerLiteral.value, "Load integer literal " + integerLiteral.value);
        }
        @Override
        public void visit(StringLiteral stringLiteral) {
            Label strLabel = constants.getStrConstant(stringLiteral.value);
            asmWriter.emitLA(A0, strLabel, "Load string label");
        }

        @Override
        public void visit(NoneLiteral noneLiteral) {
            asmWriter.emitMV(A0,ZERO,"Load None");
        }

        @Override
        public void visit(IfExpr ifExpr) {
            Label else_label = generateLocalLabel();
            Label end_label = generateLocalLabel();

            dispatchExpr(ifExpr.condition);
            asmWriter.emitBEQZ(A0, else_label, "Branch on false");

            dispatchExpr(ifExpr.thenExpr);
            asmWriter.emitJ(end_label, "Jump to end of loop");

            asmWriter.emitLocalLabel(else_label, "Else part");
            dispatchExpr(ifExpr.elseExpr);

            asmWriter.emitLocalLabel(end_label, "End of if-else expression");

        }




        @Override
        public void visit(IfStmt ifStmt) {
            Label else_label = generateLocalLabel();
            dispatchExpr(ifStmt.condition);
            asmWriter.emitBEQZ(A0, else_label, "Jump to end of loop");
            for (Stmt stmt : ifStmt.thenBody) {
                dispatchStmt(stmt);
            }
            Label end_label = generateLocalLabel();
            asmWriter.emitJ(end_label, null);
            asmWriter.emitLocalLabel(else_label, "Else body");
            for (Stmt stmt : ifStmt.elseBody) {
                dispatchStmt(stmt);
            }
            asmWriter.emitLocalLabel(end_label, null);
        }

        @Override
        public void visit(Identifier identifier) {
            String varName = identifier.name;
            SymbolInfo symbolInfo = sym.get(varName);
            
            if (symbolInfo instanceof StackVarInfo) {
                SymbolTable<SymbolInfo> current_symbolTable = sym;
                FuncInfo current_functInfo = funcInfo;
                if (!current_symbolTable.declares(varName)) {
                    current_symbolTable = current_symbolTable.getParent();
                    asmWriter.emitLW(T0, FP, current_functInfo.getParams().size() * asmWriter.getWordSize(), "Load static link");
                    current_functInfo = current_functInfo.getParentFuncInfo();
                    while (!current_symbolTable.declares(varName)) {
                        current_symbolTable = current_symbolTable.getParent();
                        asmWriter.emitLW(T0, T0, current_functInfo.getParams().size() * asmWriter.getWordSize(),
                                "Load static link");
                        current_functInfo = current_functInfo.getParentFuncInfo();
                    }
                    int index = current_functInfo.getVarIndex(varName);
                    asmWriter.emitLW(A0, T0, (current_functInfo.getParams().size() - index - 1) * asmWriter.getWordSize(), "Load local var: " + varName);
                }
                else{
                    int index = current_functInfo.getVarIndex(varName);
                    asmWriter.emitLW(A0, FP, (current_functInfo.getParams().size() - index - 1) * asmWriter.getWordSize(), "Load local var: " + varName);
                }

               
            } else {
                if (globalVar.get(((GlobalVarInfo) symbolInfo).getLabel()) != null) {
                    PhysicalRegister reg = globalVar.get(((GlobalVarInfo) symbolInfo).getLabel());
                    asmWriter.emitMV(A0, reg, "Load global var: " + varName);
                }
                else {
                    asmWriter.emitLW(A0, ((GlobalVarInfo) symbolInfo).getLabel(), "Load global var: " + varName);
                }
            }
        }


        @Override
        public void visit(WhileStmt whileStmt) {
            Label conditionLabelforWhile = generateLocalLabel();
            Label bodyForWhile = generateLocalLabel();
            asmWriter.emitJ(conditionLabelforWhile, "Jump to loop test");
            asmWriter.emitLocalLabel(bodyForWhile, "Top of while loop");
            for (Stmt stmt : whileStmt.body) {
                dispatchStmt(stmt);
            }
            asmWriter.emitLocalLabel(conditionLabelforWhile, "Test loop condition");
            dispatchExpr(whileStmt.condition);
            asmWriter.emitBNEZ(A0, bodyForWhile, "jump to body");
        }



        public void visit(ForStmt forStmt) {
            Label notNoneLabel = generateLocalLabel();
            Label loopHeaderLabel = generateLocalLabel();
            Label exitLabel = generateLocalLabel();

            dispatchExpr(forStmt.iterable);
            if(forStmt.iterable.getInferredType().isListType()){
                asmWriter.emitBNEZ(A0, notNoneLabel, "check if it is not None");
                asmWriter.emitJ(errorNone, "go to error handler");
                asmWriter.emitLocalLabel(notNoneLabel, "not None");
            }

            asmWriter.emitSW(A0, FP, -stack_slot * asmWriter.getWordSize(), "push on stack slot" + stack_slot);
            asmWriter.emitMV(T1, ZERO, "initialize for loop index");
            asmWriter.emitSW(T1, FP, -(stack_slot + 1) * asmWriter.getWordSize(), "push on stack" + stack_slot + 1);
            stack_slot++;
            stack_slot++;

            asmWriter.emitLocalLabel(loopHeaderLabel, "loop header");
            asmWriter.emitLW(T1, FP, -(stack_slot - 1) * asmWriter.getWordSize(), "pop stack" + stack_slot);
            asmWriter.emitLW(T0, FP, -(stack_slot - 2) * asmWriter.getWordSize(), "pop stack" + stack_slot);
            asmWriter.emitLW(T2, T0, "@.__len__", "Get attribute __len__");
            asmWriter.emitBGEU(T1, T2, exitLabel, "exit loop");
            if(forStmt.iterable.getInferredType().isListType()){
                asmWriter.emitADDI(T1, T1, 1, "increment index");
                asmWriter.emitSW(T1, FP, -(stack_slot - 1) * asmWriter.getWordSize(), "Push on stack");
                asmWriter.emitADDI(T1, T1, 3, "Compute list element offset in words");
                asmWriter.emitLI(T2, 4, "Word size in bytes");
                asmWriter.emitMUL(T1, T1, T2, "Compute list element offset in bytes");
                asmWriter.emitADD(T1, T0, T1, "Pointer to list element");
                asmWriter.emitLW(T0, T1, 0, "Get list element");
            }
            else{
                asmWriter.emitLW(T0, FP, -(stack_slot - 1) * asmWriter.getWordSize(), "pop stack");
                asmWriter.emitLW(A1, FP, -(stack_slot - 2) * asmWriter.getWordSize(), "peek stack");
                asmWriter.emitADDI(T1, T0, 1, "increment index");
                asmWriter.emitSW(T1, FP, -(stack_slot - 1) * asmWriter.getWordSize(), "Push on stack");
                asmWriter.emitADDI(T0, T0, 16, "Convert index to offset to char in bytes");
                asmWriter.emitADD(T0, A1, T0, "Pointer to list element");
                asmWriter.emitLBU(T0, T0, 0, "Load character");
                asmWriter.emitLI(T1, 20, "Load character");
                asmWriter.emitMUL(T0, T0, T1, "Multiply by size of string object");
                asmWriter.emitLA(A0, label_allChars, "Index into single-char table");
                string_exist=true;
                asmWriter.emitADD(T0, A0, T0, "Pointer to list element");

            }
            
            
            String varName = (forStmt.identifier).name;
            SymbolInfo symbolInfo = sym.get(varName);
            if (symbolInfo instanceof StackVarInfo) {
                SymbolTable<SymbolInfo> current_symbolTable = sym;
                FuncInfo current_functInfo = funcInfo;
                if (!current_symbolTable.declares(varName)) {
                    current_symbolTable = current_symbolTable.getParent();
                    asmWriter.emitLW(T2, FP, current_functInfo.getParams().size() * asmWriter.getWordSize(), "Load static link");
                    current_functInfo = current_functInfo.getParentFuncInfo();
                    while (!current_symbolTable.declares(varName)) {
                        current_symbolTable = current_symbolTable.getParent();
                        asmWriter.emitLW(T2, T2, current_functInfo.getParams().size() * asmWriter.getWordSize(), "Load static link");
                        current_functInfo = current_functInfo.getParentFuncInfo();
                    }
                    int index = current_functInfo.getVarIndex(varName);
                    asmWriter.emitSW(T0, T2, (current_functInfo.getParams().size() - 1 - index) * asmWriter.getWordSize(), "Store local var: " + varName);
                }
                else{
                    int index = current_functInfo.getVarIndex(varName);
                    asmWriter.emitSW(T0, FP, (current_functInfo.getParams().size() - 1 - index) * asmWriter.getWordSize(), "Store local var: " + varName);
                }
               
            }

            
            if (symbolInfo instanceof GlobalVarInfo) {
                if (globalVar.get(((GlobalVarInfo) symbolInfo).getLabel()) != null) {
                    PhysicalRegister reg = globalVar.get(((GlobalVarInfo) symbolInfo).getLabel());
                    asmWriter.emitMV(reg, T0, "Store global var: " + varName);
                }
                else {
                    asmWriter.emitSW(T0, ((GlobalVarInfo) symbolInfo).getLabel(), T2, "Store global var: " + varName);
                }
                
            }

            for (Stmt stmt : forStmt.body) {
                dispatchStmt(stmt);
            }

            asmWriter.emitJ(loopHeaderLabel, "Loop back to header");

            asmWriter.emitLocalLabel(exitLabel, "end of loop");
            stack_slot -= 2;

        }

        

    }

   
    /**
     * Emits custom code in the CODE segment.
     *
     * This method is called after emitting the top level and the
     * function bodies for each function.
     *
     * You can use this method to emit anything you want outside of the
     * top level or functions, e.g. custom routines that you may want to
     * call from within your code to do common tasks. This is not strictly
     * needed. You might not modify this at all and still complete
     * the assignment.
     *
     * To start you off, here is an implementation of three routines that
     * will be commonly needed from within the code you will generate
     * for statements.
     *
     * The routines are error handlers for operations on None, index out
     * of bounds, and division by zero. They never return to their caller.
     * Just jump to one of these routines to throw an error and
     * exit the program. For example, to throw an OOB error:
     *   asmWriter.emitJ(errorOob, "Go to out-of-bounds error and abort");
     *
     */
    protected void emitCustomCode() {
        emitErrorFunc(errorNone, "Operation on None");
        emitErrorFunc(errorDiv, "Division by zero");
        emitErrorFunc(errorOob, "Index out of bounds");
        emitMakeBool();
        emitMakeInt();
        emitConsList();
        emitStreql();
        emitStrneql();
        if(string_exist){
            emitInitChars();
        }
        else{
            emitInitChars_2();
        }
        emitConcat();
        emitNoconv();
        emitStrcat();
        
    }

    /** Emit an error routine labeled ERRLABEL that aborts with message MSG. */
    private void emitErrorFunc(Label errLabel, String msg) {
        asmWriter.emitGlobalLabel(errLabel);
        if(errLabel==errorNone){
            asmWriter.emitLI(A0, ERROR_NONE, "Exit code for: " + msg);
        }
        else if(errLabel==errorDiv){
            asmWriter.emitLI(A0, ERROR_DIV_ZERO, "Exit code for: " + msg);  
        }
        else{
            asmWriter.emitLI(A0, ERROR_OOB, "Exit code for: " + msg);
        }

        asmWriter.emitLA(A1, constants.getStrConstant(msg),
                       "Load error message as str");
        asmWriter.emitADDI(A1, A1, getAttrOffset(strClass, "__str__"),
                         "Load address of attribute __str__");
        asmWriter.emitJ(abortLabel, "Abort");
    }
    private void emitMakeBool() {
        Label falseConstantLabel = constants.getBoolConstant(false);
        asmWriter.emitGlobalLabel(label_bool);
        asmWriter.emitSLLI(A0, A0, 4, null);
        asmWriter.emitLA(T0, falseConstantLabel, null);
        asmWriter.emitADD(A0, A0, T0, null);
        asmWriter.emitJR(RA, null);
    }

    private void emitMakeInt() {
        asmWriter.emitGlobalLabel(label_int);
        asmWriter.emitADDI(SP, SP, -8, null);
        asmWriter.emitSW(RA, SP, 4, null);
        asmWriter.emitSW(A0, SP, 0, null);
        asmWriter.emitMV(T5, RA, null);
        asmWriter.emitMV(T4, A0, null);
        ClassInfo intClass = (ClassInfo) globalSymbols.get("int");
        asmWriter.emitLA(A0, intClass.getPrototypeLabel(), null);
        asmWriter.emitJAL(new Label("alloc"), null);
        asmWriter.emitLW(T0, SP, 0, null);
        asmWriter.emitSW(T0, A0, getAttrOffset(intClass, "__int__"), null);
        asmWriter.emitMV(RA, T5, null);
        asmWriter.emitADDI(SP, SP, 8, null);
        asmWriter.emitJR(RA, null);
    }

    private void emitConsList() {
        asmWriter.emitGlobalLabel(label_conslist); 
    
        asmWriter.emitADDI(SP, SP, -8, null);
        asmWriter.emitSW(RA, SP, 4, null);
        asmWriter.emitSW(FP, SP, 0, null);
        asmWriter.emitADDI(FP, SP, 8, null);
    
        asmWriter.emitLW(A1, FP, 0, null); 
    
        ClassInfo listClass = (ClassInfo) globalSymbols.get(".list");
        asmWriter.emitLA(A0, listClass.getPrototypeLabel(), null);
    
        Label doneLabel = generateLocalLabel();
        asmWriter.emitBEQZ(A1, doneLabel, null);
    
        asmWriter.emitADDI(A1, A1, 4, null); 
        asmWriter.emitJAL(new Label("alloc2"), null);
    
        asmWriter.emitLW(T0, FP, 0, null); 
        asmWriter.emitSW(T0, A0, "@.__len__", null);
    
        asmWriter.emitSLLI(T1, T0, 2, null); 
        asmWriter.emitADD(T1, T1, FP, null); 
        asmWriter.emitADDI(T2, A0, "@.__elts__", null); 
    
        Label loopLabel = generateLocalLabel();
        asmWriter.emitLocalLabel(loopLabel,null);
        asmWriter.emitLW(T3, T1, 0, null); 
        asmWriter.emitSW(T3, T2, 0, null); 
        asmWriter.emitADDI(T1, T1, -4, null); 
        asmWriter.emitADDI(T2, T2, 4, null);  
        asmWriter.emitADDI(T0, T0, -1, null); 
        asmWriter.emitBNEZ(T0, loopLabel, null); 
    
        asmWriter.emitLocalLabel(doneLabel,null);
        asmWriter.emitLW(RA, FP, -4, null); 
        asmWriter.emitLW(FP, FP, -8, null); 
        asmWriter.emitADDI(SP, SP, 8, null); 
        asmWriter.emitJR(RA, null); 
    }
    
    private void emitStreql() {
        asmWriter.emitGlobalLabel(label_streql);
        asmWriter.emitADDI(SP, SP, -8, null);
        asmWriter.emitSW(RA, SP, 4, null);
        asmWriter.emitSW(FP, SP, 0, null);
        asmWriter.emitADDI(FP, SP, 8, null);
    
        asmWriter.emitLW(A1, FP, 4, null); 
        asmWriter.emitLW(A2, FP, 0, null); 
    
        asmWriter.emitLW(T0, A1, "@.__len__", null); 
        asmWriter.emitLW(T1, A2, "@.__len__", null); 
        Label notEqualLabel = generateLocalLabel();
        asmWriter.emitBNE(T0, T1, notEqualLabel, null); 
    
        Label loopLabel = generateLocalLabel();
        Label doneLabel = generateLocalLabel();
        asmWriter.emitLocalLabel(loopLabel, null);
        asmWriter.emitBEQZ(T0, doneLabel, null); 
    
        asmWriter.emitLBU(T2, A1, 16, null); 
        asmWriter.emitLBU(T3, A2, 16, null); 
        asmWriter.emitBNE(T2, T3, notEqualLabel, null); 
    
        asmWriter.emitADDI(A1, A1, 1, null); 
        asmWriter.emitADDI(A2, A2, 1, null); 
        asmWriter.emitADDI(T0, T0, -1, null); 
        asmWriter.emitJ(loopLabel, null); 
    
        Label endLabel = generateLocalLabel();
        asmWriter.emitLocalLabel(doneLabel, null);
        asmWriter.emitLI(A0, 1, null); 
        asmWriter.emitJ(endLabel, null); 
    
        asmWriter.emitLocalLabel(notEqualLabel, null);
        asmWriter.emitXOR(A0, A0, A0, null); 
    
        asmWriter.emitLocalLabel(endLabel, null);
        asmWriter.emitLW(RA, FP, -4, null); 
        asmWriter.emitLW(FP, FP, -8, null); 
        asmWriter.emitADDI(SP, SP, 8, null); 
        asmWriter.emitJR(RA, null); 
    }

    private void emitStrneql() {
        asmWriter.emitGlobalLabel(label_strneql);
    
        asmWriter.emitADDI(SP, SP, -8, null);
        asmWriter.emitSW(RA, SP, 4, null);
        asmWriter.emitSW(FP, SP, 0, null);
        asmWriter.emitADDI(FP, SP, 8, null);
    
        asmWriter.emitLW(A1, FP, 4, null); 
        asmWriter.emitLW(A2, FP, 0, null); 
    
        asmWriter.emitLW(T0, A1, "@.__len__", null); 
        asmWriter.emitLW(T1, A2, "@.__len__", null); 
        Label notEqualLabel = generateLocalLabel();
        asmWriter.emitBNE(T0, T1, notEqualLabel, null); 
    
        Label loopLabel = generateLocalLabel();
        Label endLabel = generateLocalLabel();
        asmWriter.emitLocalLabel(loopLabel, null);
    
        asmWriter.emitLBU(T2, A1, 16, null); 
        asmWriter.emitLBU(T3, A2, 16, null); 
        asmWriter.emitBNE(T2, T3, notEqualLabel, null); 
    
        asmWriter.emitADDI(A1, A1, 1, null); 
        asmWriter.emitADDI(A2, A2, 1, null); 
        asmWriter.emitADDI(T0, T0, -1, null); 
        asmWriter.emitBGTZ(T0, loopLabel, null); 
        asmWriter.emitXOR(A0, A0, A0, null); 

        asmWriter.emitJ(endLabel, null); 
    
        asmWriter.emitLocalLabel(notEqualLabel, null);
        asmWriter.emitLI(A0, 1, null); 
    
        asmWriter.emitLocalLabel(endLabel, null);
    
        asmWriter.emitLW(RA, FP, -4, null); 
        asmWriter.emitLW(FP, FP, -8, null); 
        asmWriter.emitADDI(SP, SP, 8, null); 
        asmWriter.emitJR(RA, null); 
    }

    private void emitInitChars() {
        asmWriter.emitGlobalLabel(label_initchars);

        asmWriter.emitLA(A0, new Label("$str$prototype"), null);
        asmWriter.emitLW(T0, A0, 0, null);  
        asmWriter.emitLW(T1, A0, 4, null); 
        asmWriter.emitLW(T2, A0, 8, null); 
        asmWriter.emitLI(T3, 1, null);    
        asmWriter.emitLA(A0, label_allChars, null); 
        asmWriter.emitLI(T4, 256, null);                 
        asmWriter.emitMV(T5, ZERO, null);                
    
        Label loopLabel = generateLocalLabel();

        // Loop start
        asmWriter.emitLocalLabel(loopLabel, null);
    
        asmWriter.emitSW(T0, A0, 0, null);  // Store first word
        asmWriter.emitSW(T1, A0, 4, null); // Store second word
        asmWriter.emitSW(T2, A0, 8, null); // Store third word
        asmWriter.emitSW(T3, A0, 12, null); 
        asmWriter.emitSW(T5, A0, 16, null); 
    
        //next object
        asmWriter.emitADDI(A0, A0, 20, null); 
        asmWriter.emitADDI(T5, T5, 1, null); 
    
        asmWriter.emitBNE(T4, T5, loopLabel, null); 
        asmWriter.emitJR(RA, null); 

        asmWriter.emitInsn(".data");
        asmWriter.emitInsn(".align 2");
        asmWriter.emitInsn(".globl allChars");
        asmWriter.emitLocalLabel(label_allChars, "all chars label");
        asmWriter.emitInsn(".space 5120");
        asmWriter.emitInsn(".text");

    }


    private void emitConcat() {
        asmWriter.emitGlobalLabel(label_concat);
    
        // Prologue
        asmWriter.emitADDI(SP, SP, -32, null);
        asmWriter.emitSW(RA, SP, 28, null);
        asmWriter.emitSW(FP, SP, 24, null);
        asmWriter.emitADDI(FP, SP, 32, null);
        asmWriter.emitSW(S1, FP, -12, null);
        asmWriter.emitSW(S2, FP, -16, null);
        asmWriter.emitSW(S3, FP, -20, null);
        asmWriter.emitSW(S4, FP, -24, null);
        asmWriter.emitSW(S5, FP, -28, null);
    
        // Check if any of the input lists is null
        asmWriter.emitLW(T0, FP, 4, null);
        asmWriter.emitLW(T1, FP, 0, null);
        Label concatNoneLabel = generateLocalLabel();
        asmWriter.emitBEQZ(T0, concatNoneLabel, null);
        asmWriter.emitBEQZ(T1, concatNoneLabel, null);
    
        // Compute combined length
        asmWriter.emitLW(T0, T0, "@.__len__", null);
        asmWriter.emitLW(T1, T1, "@.__len__", null);
        asmWriter.emitADD(S5, T0, T1, null);
        asmWriter.emitADDI(A1, S5, 4, null);
        asmWriter.emitLA(A0, new Label("$.list$prototype"), null);
        asmWriter.emitJAL(new Label("alloc2"), null);
        asmWriter.emitSW(S5, A0, "@.__len__", null);
        asmWriter.emitMV(S5, A0, null);
    
        // Prepare pointers
        asmWriter.emitADDI(S3, S5, "@.__elts__", null);
        asmWriter.emitLW(S1, FP, 4, null);
        asmWriter.emitLW(S2, S1, "@.__len__", null);
        asmWriter.emitADDI(S1, S1, "@.__elts__", null);
        asmWriter.emitLW(S4, FP, 12, null);
    
        Label concatLoop1 = generateLocalLabel();
        Label concatLoop2 = generateLocalLabel();
        Label concatLoop3 = generateLocalLabel();
        Label concatEnd = generateLocalLabel();
        asmWriter.emitLocalLabel(concatLoop1, null);
        asmWriter.emitBEQZ(S2, concatLoop2, null);
        asmWriter.emitLW(A0, S1, 0, null);
        asmWriter.emitJALR(RA, S4, 0, null);
        asmWriter.emitSW(A0, S3, 0, null);
        asmWriter.emitADDI(S2, S2, -1, null);
        asmWriter.emitADDI(S1, S1, 4, null);
        asmWriter.emitADDI(S3, S3, 4, null);
        asmWriter.emitJ(concatLoop1, null);
    
        asmWriter.emitLocalLabel(concatLoop2, null);
        asmWriter.emitLW(S1, FP, 0, null);
        asmWriter.emitLW(S2, S1, "@.__len__", null);
        asmWriter.emitADDI(S1, S1, "@.__elts__", null);
        asmWriter.emitLW(S4, FP, 8, null);
        asmWriter.emitLocalLabel(concatLoop3, null);
        asmWriter.emitBEQZ(S2, concatEnd, null);
        asmWriter.emitLW(A0, S1, 0, null);
        asmWriter.emitJALR(RA, S4, 0, null);
        asmWriter.emitSW(A0, S3, 0, null);
        asmWriter.emitADDI(S2, S2, -1, null);
        asmWriter.emitADDI(S1, S1, 4, null);
        asmWriter.emitADDI(S3, S3, 4, null);
        asmWriter.emitJ(concatLoop3, null);
    
        asmWriter.emitLocalLabel(concatEnd, null);
        asmWriter.emitMV(A0, S5, null);
    
        asmWriter.emitLW(S1, FP, -12, null);
        asmWriter.emitLW(S2, FP, -16, null);
        asmWriter.emitLW(S3, FP, -20, null);
        asmWriter.emitLW(S4, FP, -24, null);
        asmWriter.emitLW(S5, FP, -28, null);
        asmWriter.emitLW(RA, FP, -4, null);
        asmWriter.emitLW(FP, FP, -8, null);
        asmWriter.emitADDI(SP, SP, 32, null);
        asmWriter.emitJR(RA, null);
    
        asmWriter.emitLocalLabel(concatNoneLabel, null);
        asmWriter.emitJ(new Label("error.None"), null);
    }
    
    private void emitInitChars_2() {
        asmWriter.emitGlobalLabel(label_initchars);
        asmWriter.emitJR(RA,null);
    }
    private void emitNoconv() {
        asmWriter.emitGlobalLabel(label_noconv);
        asmWriter.emitJR(RA,null);
    }

    private void emitStrcat() {
        asmWriter.emitGlobalLabel(label_strcat);
    
        asmWriter.emitADDI(SP, SP, -12, null);  
        asmWriter.emitSW(RA, SP, 8, null);      
        asmWriter.emitSW(FP, SP, 4, null);      
        asmWriter.emitADDI(FP, SP, 12, null);   
    
        asmWriter.emitLW(T0, FP, 4, null);      
        asmWriter.emitLW(T1, FP, 0, null);      
    
        asmWriter.emitLW(T0, T0, "@.__len__", null);  
        Label strcat_4 = generateLocalLabel();
        asmWriter.emitBEQZ(T0, strcat_4, null);       
    
        asmWriter.emitLW(T1, T1, "@.__len__", null);  
        Label strcat_5 = generateLocalLabel();
        asmWriter.emitBEQZ(T1, strcat_5, null);       
    
        asmWriter.emitADD(T1, T0, T1, null);          
        asmWriter.emitSW(T1, FP, -12, null);          
        asmWriter.emitADDI(T1, T1, 4, null);         
        asmWriter.emitSRLI(T1, T1, 2, null);          
        asmWriter.emitADDI(A1, T1, 4, null);  
        asmWriter.emitLA(A0, new Label("$str$prototype"), null);
        asmWriter.emitJAL(new Label("alloc2"), null);                     
    
        asmWriter.emitLW(T0, FP, -12, null);         
        asmWriter.emitSW(T0, A0, "@.__len__", null); 
        asmWriter.emitADDI(T2, A0, "@.__str__", null);        
    
        asmWriter.emitLW(T0, FP, 4, null);           
        asmWriter.emitLW(T1, T0, "@.__len__", null);
        asmWriter.emitADDI(T0, T0, "@.__str__", null);  
        Label strcat_1 = generateLocalLabel();
        Label strcat_2 = generateLocalLabel();
        asmWriter.emitLocalLabel(strcat_1, null);
        asmWriter.emitBEQZ(T1, strcat_2, null);      
        asmWriter.emitLBU(T3, T0, 0, null);          
        asmWriter.emitSB(T3, T2, 0, null);           
        asmWriter.emitADDI(T1, T1, -1, null);        
        asmWriter.emitADDI(T0, T0, 1, null);         
        asmWriter.emitADDI(T2, T2, 1, null);         
        asmWriter.emitJ(strcat_1, null);            
        asmWriter.emitLocalLabel(strcat_2, null);
    
        asmWriter.emitLW(T0, FP, 0, null);           
        asmWriter.emitLW(T1, T0, "@.__len__", null);         
        asmWriter.emitADDI(T0, T0, "@.__str__", null);        
        Label strcat_3 = generateLocalLabel();
        Label strcat_6 = generateLocalLabel();
        asmWriter.emitLocalLabel(strcat_3, null);
        asmWriter.emitBEQZ(T1, strcat_6, null);      
        asmWriter.emitLBU(T3, T0, 0, null);          
        asmWriter.emitSB(T3, T2, 0, null);           
        asmWriter.emitADDI(T1, T1, -1, null);        
        asmWriter.emitADDI(T0, T0, 1, null);        
        asmWriter.emitADDI(T2, T2, 1, null);         
        asmWriter.emitJ(strcat_3, null);            

    
        Label strcat_7 = generateLocalLabel();
        asmWriter.emitLocalLabel(strcat_4, null);
        asmWriter.emitLW(A0, FP, 0, null);
        asmWriter.emitJ(strcat_7, null);
    
        asmWriter.emitLocalLabel(strcat_5, null);
        asmWriter.emitLW(A0, FP, 4, null);          
        asmWriter.emitJ(strcat_7, null);
        asmWriter.emitLocalLabel(strcat_6, null);
        asmWriter.emitSB(ZERO, T2, 0, null);
        asmWriter.emitLocalLabel(strcat_7, null);
        asmWriter.emitLW(RA, FP, -4, null);          
        asmWriter.emitLW(FP, FP, -8, null);          
        asmWriter.emitADDI(SP, SP, 12, null);       
        asmWriter.emitJR(RA, null);                  
    }
    
}