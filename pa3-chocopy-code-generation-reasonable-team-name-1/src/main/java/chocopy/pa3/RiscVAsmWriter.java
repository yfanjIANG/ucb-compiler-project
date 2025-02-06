package chocopy.pa3;

import chocopy.common.codegen.Label;
import java.io.PrintWriter;
import java.io.StringWriter;

/** RISC V assembly-language generation utilities. */
public class RiscVAsmWriter {

    /** Accumulator for assembly code output. */
    protected final StringWriter asmText = new StringWriter();

    /** Allows print, println, and printf of assmebly code. */
    private final PrintWriter out = new PrintWriter(asmText);

    /** The word size in bytes for RISC-V 32-bit. */
    protected static final int WORD_SIZE = 4;

    /**
     * RISC-V physical registers. These correspond to registers in the RISC-V
     * instruction set and thus can be emitted by RiscVAsmWriter.
     **/
    public enum PhysicalRegister implements RiscV.Register {

        A0("a0"), A1("a1"), A2("a2"), A3("a3"), A4("a4"), A5("a5"), A6("a6"),
        A7("a7"),
        T0("t0"), T1("t1"), T2("t2"), T3("t3"), T4("t4"), T5("t5"), T6("t6"),
        S1("s1"), S2("s2"), S3("s3"), S4("s4"), S5("s5"),
        S6("s6"), S7("s7"), S8("s8"), S9("s9"), S10("s10"), S11("s11"),
        FP("fp"), SP("sp"), GP("gp"), RA("ra"), ZERO("zero");

        /** The name of the register used in assembly. */
        protected final String name;

        /** This register's code representation is NAME. */
        PhysicalRegister(String name) { this.name = name; }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @Override
    public String toString() {
        return asmText.toString();
    }

    /**
     * Define @NAME to have the value VALUE. Here, NAME is assumed to be
     * an identifier consisting of letters, digits, underscores, and any of
     * the charcters '$' or '.', and that does not start with a digit. Value
     * may be a numeral or another symbol.
     */
    public void defineSym(String name, String value) {
        if (name.startsWith("@")) {
            emitInsn(String.format(".equiv %s, %s", name, value), null);
        } else {
            emitInsn(String.format(".equiv @%s, %s", name, value), null);
        }
    }

    /**
     * Define @NAME to have the value VALUE, where value is converted to
     * a string. See {@link #defineSym(java.lang.String, java.lang.String)}.
     */
    public void defineSym(String name, int value) {
        defineSym(name, Integer.toString(value));
    }

    /**
     * Returns the word size in bytes.
     *
     * This method is used instead of directly accessing the
     * static field {@link #WORD_SIZE}, so that this class
     * may be extended with alternate word sizes.
     */
    public int getWordSize() { return WORD_SIZE; }

    /**
     * Emit the text STR to the output stream verbatim. STR should have no
     * trailing newline.
     */
    public void emit(String str) { out.println(str); }

    /**
     * Emit instruction or directive INSN along with COMMENT as a one-line
     * comment, if non-null.
     */
    public void emitInsn(String insn, String comment) {
        if (comment != null) {
            emit(String.format("  %-40s # %s", insn, comment));
        } else {
            emitInsn(insn);
        }
    }

    /**
     * Emit instruction or directive INSN without a comment.
     */
    protected void emitInsn(String insn) { emit(String.format("  %s", insn)); }

    /**
     * Emit a local label marker for LABEL with one-line comment COMMENT (null
     * if missing). Invoke only once per unique label.
     */
    public void emitLocalLabel(Label label, String comment) {
        if (comment != null) {
            emit(String.format("%-42s # %s", label + ":", comment));
        } else {
            emit(String.format("%s:", label + ":"));
        }
    }

    /**
     * Emit a global label marker for LABEL. Invoke only once per
     * unique label.
     */
    public void emitGlobalLabel(Label label) {
        emit(String.format("\n.globl %s", label));
        emit(String.format("%s:", label));
    }

    /**
     * Emit a data word containing VALUE as an integer value. COMMENT is
     * a emitted as a one-line comment, if non-null.
     */
    public void emitWordLiteral(int value, String comment) {
        emitInsn(String.format(".word %s", value), comment);
    }

    /**
     * Emit a data word containing the address ADDR, or 0 if LABEL is null.
     * COMMENT is a emitted as a one-line comment, if non-null.
     */
    public void emitWordAddress(Label addr, String comment) {
        if (addr == null) {
            emitWordLiteral(0, comment);
        } else {
            emitInsn(String.format(".word %s", addr), comment);
        }
    }

    /**
     * Emit VALUE as an ASCII null-terminated string constant, with
     * COMMENT as its one-line comment, if non-null.
     */
    public void emitString(String value, String comment) {
        String quoted = value.replace("\\", "\\\\")
                            .replace("\n", "\\n")
                            .replace("\t", "\\t")
                            .replace("\"", "\\\"");
        emitInsn(String.format(".string \"%s\"", quoted), comment);
    }

    /**
     * Mark the start of a data section.
     */
    public void startData() { emit("\n.data"); }

    /**
     * Mark the start of a code/text section.
     */
    public void startCode() { emit("\n.text"); }

    /**
     * Align the next instruction/word in memory to
     * a multiple of 2**POW bytes.
     */
    public void alignNext(int pow) {
        emitInsn(String.format(".align %d", pow));
    }

    /**
     * Emit an ecall instruction, with one-line comment COMMENT,
     * if non-null.
     */
    public void emitEcall(String comment) { emitInsn("ecall", comment); }

    /**
     * Emit a load-address instruction with destination RD and source
     * LABEL. COMMENT is an optional one-line comment (null if missing).
     */
    public void emitLA(PhysicalRegister rd, Label label, String comment) {
        emitInsn(String.format("la %s, %s", rd, label), comment);
    }

    /**
     * Emit a load-immediate pseudo-op to set RD to IMM.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitLI(PhysicalRegister rd, int imm, String comment) {
        emitInsn(String.format("li %s, %d", rd, imm), comment);
    }

    /**
     * Emit a load-upper-immediate instruction to set the upper 20 bits
     * of RD to IMM, where 0 <= IMM < 2**20. COMMENT is an optional
     * one-line comment (null if missing).
     */
    public void emitLUI(PhysicalRegister rd, int immu, String comment) {
        emitInsn(String.format("lui %s, %d", rd, immu), comment);
    }

    /**
     * Emit a move instruction to set RD to the contents of RS1.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitMV(PhysicalRegister rd, PhysicalRegister rs1,
                       String comment) {
        emitInsn(String.format("mv %s, %s", rd, rs1), comment);
    }

    /**
     * Emit a jump-register (computed jump) instruction to the address in
     * RS1. COMMENT is an optional one-line comment (null if missing).
     */
    public void emitJR(PhysicalRegister rs1, String comment) {
        emitInsn(String.format("jr %s", rs1), comment);
    }

    /**
     * Emit a jump (unconditional jump) instruction to LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitJ(Label label, String comment) {
        emitInsn(String.format("j %s", label), comment);
    }

    /**
     * Emit a jump-and-link instruction to LABEL.
     * The address of the next instruction is stored in RA.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitJAL(PhysicalRegister rd, Label label,
                        String comment) {
        emitInsn(String.format("jal %s %s", rd, label), comment);
    }

    /**
     * Emit a jump-and-link instruction to LABEL.
     * The address of the next instruction is stored in RA.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitJAL(Label label, String comment) {
        emitInsn(String.format("jal %s", label), comment);
    }

    /**
     * Emit a computed-jump-and-link instruction to the address in RS1 + imm.
     * The address of next instruction is stored in rd.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitJALR(PhysicalRegister rd, PhysicalRegister rs1,
                         int imm, String comment) {
        emitInsn(String.format("jalr %s %s %d", rd, rs1, imm), comment);
    }

    /**
     * Emit a computed-jump-and-link instruction to the address in RS1.
     * The address of the next instruction is stored in RA.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitJALR(PhysicalRegister rs1, String comment) {
        emitInsn(String.format("jalr %s", rs1), comment);
    }

    /**
     * Emit an add-immediate instruction performing RD = RS1 + IMM.
     * Requires -2048 <= IMM < 2048. COMMENT is an optional one-line
     * comment (null if missing).
     */
    public void emitADDI(PhysicalRegister rd, PhysicalRegister rs1,
                         int imm, String comment) {
        emitInsn(String.format("addi %s, %s, %d", rd, rs1, imm), comment);
    }

    /**
     * Emit an add-immediate instruction performing RD = RS1 + IMM.
     * Here, IMM is a string generally containing a symbolic assembler
     * constant (see defineSym) representing an integer value, or an
     * expression of the form @NAME+NUM or @NAME-NUM.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitADDI(PhysicalRegister rd, PhysicalRegister rs1,
                         String imm, String comment) {
        emitInsn(String.format("addi %s, %s, %s", rd, rs1, imm), comment);
    }

    /**
     * Emit an add instruction performing RD = RS1 + RS2 mod 2**32.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitADD(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("add %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a subtract instruction performing RD = RS1 - RS2 mod 2**32.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSUB(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("sub %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a multiply instruction performing RD = RS1 * RS2 mod 2**32.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitMUL(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("mul %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a signed integer divide instruction performing
     * RD = RS1 / RS2 mod 2**32, rounding the result toward 0.
     * If RS2 == 0, sets RD to -1. If RS1 == -2**31 and RS2 == -1,
     * sets RD to -2**31.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitDIV(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("div %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a remainder instruction: RD = RS1 rem RS2 defined so that
     * (RS1 / RS2) * RS2 + (RS1 rem RS2) == RS1, where / is as for
     * emitDIV. COMMENT is an optional one-line comment (null if missing).
     */
    public void emitREM(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("rem %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit an xor instruction: RD = RS1 ^ RS2. COMMENT is an optional
     * one-line comment (null if missing).
     */
    public void emitXOR(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("xor %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit an xor-immediate instruction: RD = RS1 ^ IMM, where
     * -2048 <= IMM < 2048. COMMENT is an optional
     * one-line comment (null if missing).
     */
    public void emitXORI(PhysicalRegister rd, PhysicalRegister rs1,
                         int imm, String comment) {
        emitInsn(String.format("xori %s, %s, %d", rd, rs1, imm), comment);
    }

    /**
     * Emit a bitwise and instruction: RD = RS1 & RS2.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitAND(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("and %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a bitwise and-immediate instruction: RD = RS1 & IMM, where
     * -2048 <= IMM < 2048.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitANDI(PhysicalRegister rd, PhysicalRegister rs1,
                         int imm, String comment) {
        emitInsn(String.format("andi %s, %s, %d", rd, rs1, imm), comment);
    }

    /**
     * Emit a bitwise or instruction: RD = RS1 | RS2.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitOR(PhysicalRegister rd, PhysicalRegister rs1,
                       PhysicalRegister rs2, String comment) {
        emitInsn(String.format("or %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a bitwise or-immediate instruction: RD = RS1 | IMM, where
     * -2048 <= IMM < 2048.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitORI(PhysicalRegister rd, PhysicalRegister rs1,
                        int imm, String comment) {
        emitInsn(String.format("ori %s, %s, %d", rd, rs1, imm), comment);
    }

    /**
     * Emit a logical left shift instruction: RD = RS1 << (RS2 & 0b11111).
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSLL(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("sll %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a logical left shift instruction: RD = RS1 << (IMM & 0b11111).
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSLLI(PhysicalRegister rd, PhysicalRegister rs1,
                         int imm, String comment) {
        emitInsn(String.format("slli %s, %s, %d", rd, rs1, imm), comment);
    }

    /**
     * Emit a logical right shift instruction: RD = RS1 >>> (RS2 & 0b11111).
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSRL(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("srl %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a logical right shift instruction: RD = RS1 >>> (IMM & 0b11111).
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSRLI(PhysicalRegister rd, PhysicalRegister rs1,
                         int imm, String comment) {
        emitInsn(String.format("srli %s, %s, %d", rd, rs1, imm), comment);
    }

    /**
     * Emit an arithmetic right shift instruction: RD = RS1 >> (RS2 & 0b11111).
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSRA(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("sra %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit an arithmetic right shift instruction: RD = RS1 >> (IMM & 0b11111).
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSRAI(PhysicalRegister rd, PhysicalRegister rs1,
                         int imm, String comment) {
        emitInsn(String.format("srai %s, %s, %d", rd, rs1, imm), comment);
    }

    /**
     * Emit a load-word instruction: RD = MEMORY[RS1 + IMM]:4, where
     * -2048 <= IMM < 2048.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitLW(PhysicalRegister rd, PhysicalRegister rs1,
                       int imm, String comment) {
        emitInsn(String.format("lw %s, %d(%s)", rd, imm, rs1), comment);
    }

    /**
     * Emit a load-word instruction: RD = MEMORY[RS1 + IMM]:4, where
     * -2048 <= IMM < 2048. Here, IMM is symbolic constant expression
     * (see emitADDI). COMMENT is an optional one-line
     * comment (null if missing).
     */
    public void emitLW(PhysicalRegister rd, PhysicalRegister rs1,
                       String imm, String comment) {
        emitInsn(String.format("lw %s, %s(%s)", rd, imm, rs1), comment);
    }

    /**
     * Emit a store-word instruction: MEMORY[RS1 + IMM]:4 = RS2, where
     * -2048 <= IMM < 2048.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSW(PhysicalRegister rs2, PhysicalRegister rs1,
                       int imm, String comment) {
        emitInsn(String.format("sw %s, %d(%s)", rs2, imm, rs1), comment);
    }

    /**
     * Emit a store-word instruction: MEMORY[RS1 + IMM]:4 = RS2, where
     * -2048 <= IMM < 2048. Here, IMM is symbolic constant expression
     * (see emitADDI). COMMENT is an optional one-line
     * comment (null if missing).
     */
    public void emitSW(PhysicalRegister rs2, PhysicalRegister rs1,
                       String imm, String comment) {
        emitInsn(String.format("sw %s, %s(%s)", rs2, imm, rs1), comment);
    }

    /**
     * Emit a load-word instruction for globals: RD = MEMORY[LABEL]:4.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitLW(PhysicalRegister rd, Label label, String comment) {
        emitInsn(String.format("lw %s, %s", rd, label), comment);
    }

    /**
     * Emit a store-word instruction for globals: MEMORY[LABEL]:4 = RS1,
     * using TMP as a temporary register.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSW(PhysicalRegister rs1, Label label,
                       PhysicalRegister tmp, String comment) {
        emitInsn(String.format("sw %s, %s, %s", rs1, label, tmp), comment);
    }

    /**
     * Emit a load-byte instruction: RD = MEMORY[RS1 + IMM]:1, where
     * -2048 <= IMM < 2048. Sign extends the byte loaded.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitLB(PhysicalRegister rd, PhysicalRegister rs1,
                       int imm, String comment) {
        emitInsn(String.format("lb %s, %d(%s)", rd, imm, rs1), comment);
    }

    /**
     * Emit a load-byte-unsigned instruction: RD = MEMORY[RS1 + IMM]:1, where
     * -2048 <= IMM < 2048. Zero-extends the byte loaded.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitLBU(PhysicalRegister rd, PhysicalRegister rs1,
                        int imm, String comment) {
        emitInsn(String.format("lbu %s, %d(%s)", rd, imm, rs1), comment);
    }

    /**
     * Emit a store-byte instruction: MEMORY[RS1 + IMM]:1 = RS2, where
     * -2048 <= IMM < 2048. Assigns the low-order byte of RS2 to memory.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSB(PhysicalRegister rs2, PhysicalRegister rs1,
                       int imm, String comment) {
        emitInsn(String.format("sb %s, %d(%s)", rs2, imm, rs1), comment);
    }

    /**
     * Emit a branch-if-equal instruction: if RS1 == RS2 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBEQ(PhysicalRegister rs1, PhysicalRegister rs2,
                        Label label, String comment) {
        emitInsn(String.format("beq %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emit a branch-if-unequal instruction: if RS1 != RS2 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBNE(PhysicalRegister rs1, PhysicalRegister rs2,
                        Label label, String comment) {
        emitInsn(String.format("bne %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emit a branch-if-greater-or-equal (signed) instruction:
     * if RS1 >= RS2 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBGE(PhysicalRegister rs1, PhysicalRegister rs2,
                        Label label, String comment) {
        emitInsn(String.format("bge %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emit a branch-if-greater-or-equal (unsigned) instruction:
     * if RS1 >= RS2 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBGEU(PhysicalRegister rs1, PhysicalRegister rs2,
                         Label label, String comment) {
        emitInsn(String.format("bgeu %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emit a branch-if-less-than (signed) instruction:
     * if RS1 < RS2 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBLT(PhysicalRegister rs1, PhysicalRegister rs2,
                        Label label, String comment) {
        emitInsn(String.format("blt %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emit a branch-if-less-than (unsigned) instruction:
     * if RS1 < RS2 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBLTU(PhysicalRegister rs1, PhysicalRegister rs2,
                         Label label, String comment) {
        emitInsn(String.format("bltu %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emit a branch-if-zero instruction: if RS1 == 0 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBEQZ(PhysicalRegister rs1, Label label,
                         String comment) {
        emitInsn(String.format("beqz %s, %s", rs1, label), comment);
    }

    /**
     * Emit a branch-if-not-zero instruction: if RS1 != 0 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBNEZ(PhysicalRegister rs1, Label label,
                         String comment) {
        emitInsn(String.format("bnez %s, %s", rs1, label), comment);
    }

    /**
     * Emit a branch-if-less-than-zero instruction: if RS1 < 0 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBLTZ(PhysicalRegister rs1, Label label,
                         String comment) {
        emitInsn(String.format("bltz %s, %s", rs1, label), comment);
    }

    /**
     * Emit a branch-if-greater-than-zero instruction: if RS1 > 0 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBGTZ(PhysicalRegister rs1, Label label,
                         String comment) {
        emitInsn(String.format("bgtz %s, %s", rs1, label), comment);
    }

    /**
     * Emit a branch-if-less-than-equal-to-zero instruction:
     * if RS1 <= 0 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBLEZ(PhysicalRegister rs1, Label label,
                         String comment) {
        emitInsn(String.format("blez %s, %s", rs1, label), comment);
    }

    /**
     * Emit a branch-if-greater-than-equal-to-zero instruction:
     * if RS1 >= 0 goto LABEL.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitBGEZ(PhysicalRegister rs1, Label label,
                         String comment) {
        emitInsn(String.format("bgez %s, %s", rs1, label), comment);
    }

    /**
     * Emit a set-less-than instruction: RD = 1 if RS1 < RS2 else 0.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSLT(PhysicalRegister rd, PhysicalRegister rs1,
                        PhysicalRegister rs2, String comment) {
        emitInsn(String.format("slt %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emit a set-if-zero instruction: RD = 1 if RS1 == 0 else 0.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSEQZ(PhysicalRegister rd, PhysicalRegister rs1,
                         String comment) {
        emitInsn(String.format("seqz %s, %s", rd, rs1), comment);
    }

    /**
     * Emit a set-if-not-zero instruction: RD = 1 if RS1 != 0 else 0.
     * COMMENT is an optional one-line comment (null if missing).
     */
    public void emitSNEZ(PhysicalRegister rd, PhysicalRegister rs1,
                         String comment) {
        emitInsn(String.format("snez %s, %s", rd, rs1), comment);
    }

    private PhysicalRegister expectPhysReg(RiscV.Register reg) {
        if (!(reg instanceof PhysicalRegister physReg)) {
            throw new IllegalArgumentException(
                "Expected physical register; got: " + reg.toString());
        }

        return physReg;
    }

    public void emitRiscVInstr(RiscV.Instr instr) {
        switch (instr) {
        case RiscV.BinaryInstr ins -> {
            var rd = expectPhysReg(ins.rd());
            var rs1 = expectPhysReg(ins.rs1());
            var rs2 = expectPhysReg(ins.rs2());
            emitInsn(String.format("%s %s, %s, %s", ins.op(), rd, rs1, rs2),
                     ins.comment());
        }

        case RiscV.BinaryImmInstr ins -> {
            var rd = expectPhysReg(ins.rd());
            var rs1 = expectPhysReg(ins.rs1());
            emitInsn(
                String.format("%s %s, %s, %d", ins.op(), rd, rs1, ins.imm()),
                ins.comment());
        }

        case RiscV.LoadInstr ins -> {
            var rd = expectPhysReg(ins.rd());
            var rs1 = expectPhysReg(ins.rs1());
            emitInsn(
                String.format("%s %s, %d(%s)", ins.op(), rd, ins.imm(), rs1),
                ins.comment());
        }

        case RiscV.StoreInstr ins -> {
            var rs2 = expectPhysReg(ins.rs2());
            var rs1 = expectPhysReg(ins.rs1());
            emitInsn(
                String.format("%s %s, %d(%s)", ins.op(), rs2, ins.imm(), rs1),
                ins.comment());
        }

        case RiscV.BranchInstr ins -> {
            var rs1 = expectPhysReg(ins.rs1());
            var rs2 = expectPhysReg(ins.rs2());
            var label = ins.label();
            emitInsn(String.format("%s %s, %s, %s", ins.op(), rs1, rs2, label),
                     ins.comment());
        }

        case RiscV.Jal ins -> {
            var rd = expectPhysReg(ins.rd());
            var label = ins.label();
            emitJAL(rd, label, ins.comment());
        }

        case RiscV.Jalr ins -> {
            var rd = expectPhysReg(ins.rd());
            var rs1 = expectPhysReg(ins.rs1());
            emitJALR(rd, rs1, ins.imm(), ins.comment());
        }

        case RiscV.LocalLabel ins -> {
            var label = ins.label();
            emitLocalLabel(label, ins.comment());
        }

        case RiscV.Auipc ins -> {
            var rd = expectPhysReg(ins.rd());
            emitInsn(String.format("auipc %s, %d", rd, ins.immu()),
                     ins.comment());
        }

        case RiscV.Lui ins -> {
            var rd = expectPhysReg(ins.rd());
            emitLUI(rd, ins.immu(), ins.comment());
        }

        case RiscV.Ecall ins -> emitEcall(ins.comment());

        case RiscV.Li ins -> {
            var rd = expectPhysReg(ins.rd());
            emitLI(rd, ins.imm(), ins.comment());
        }

        case RiscV.La ins -> {
            var rd = expectPhysReg(ins.rd());
            emitLA(rd, ins.label(), ins.comment());
        }
        }
    }
}
