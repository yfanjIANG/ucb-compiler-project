package chocopy.pa3;

import chocopy.common.codegen.Label;
import chocopy.pa3.RiscVAsmWriter.PhysicalRegister;

class RiscV {
    sealed interface Register permits PhysicalRegister, VirtualRegister {}

    /**
     * Virtual registers.
     *
     * There are an unlimited number of these virtual registers; this is useful
     * if you want to do more sophisticated optimizations as you do not have to
     * worry about constraints on the number of registers.
     *
     * However, it can be challenging to translate code that assumes an
     * unlimited number of virtual registers to code that only uses limited
     * physical registers (to "lower" such code requires lifetime analysis +
     * register allocation).
     *
     * Unless you feel confident with your ability to lower such code, we
     * strongly suggest that you only generate code for physical registers from
     * the ChocoPy AST. For TA support, we will be prioritizing students who
     * only use physical registers, and not all TAs will be able to help you
     * with lowering.
     */
    public record VirtualRegister(String ident) implements Register {
        @Override
        public String toString() {
            return "%" + ident;
        }
    }

    /**
     * These are the subset of instructions that are necessary to implement
     * function logic. For example, there is no instruction for a string
     * constant here, as those should be emitted ahead of time.
     *
     * Instructions are immutable. You may add to or modify these instructions.
     *
     * These instructions are not necessary a superset or a subset of the
     * instructions supported in RiscVAsmWriter. You are allowed to modify both
     * files as necessary to add support for new instructions.
     */
    public sealed interface Instr permits BinaryInstr, BinaryImmInstr,
        LoadInstr, StoreInstr, BranchInstr, Jal, Jalr, LocalLabel, Auipc, Lui,
        Ecall, Li, La {
        public String comment();
    }

    public record BinaryInstr(Op op, Register rd, Register rs1, Register rs2,
                              String comment) implements Instr {
        public enum Op {
            ADD("add"),
            SUB("sub"),
            AND("and"),
            OR("or"),
            XOR("xor"),
            SLL("sll"),
            SRL("srl"),
            SRA("sra"),
            SLT("slt"),
            SLTU("sltu"),
            MUL("mul"),
            DIV("div"),
            REM("rem");

            protected final String op;

            Op(String op) { this.op = op; }

            public String toString() { return op; }
        }

        public String toString() {
            return String.format("%s %s, %s, %s  # %s", op, rd, rs1, rs2,
                                 comment);
        }
    }

    public record BinaryImmInstr(Op op, Register rd, Register rs1, int imm,
                                 String comment) implements Instr {
        public BinaryImmInstr {
            if (op == Op.SLLI || op == Op.SRLI || op == Op.SRAI) {
                assert 0 <= imm && imm < 32;
            } else {
                assert - 2048 <= imm && imm < 2048;
            }
        }

        public enum Op {
            ADDI("addi"),
            ANDI("andi"),
            ORI("ori"),
            XORI("xori"),
            SLLI("slli"),
            SRLI("srli"),
            SRAI("srai"),
            SLTI("slti"),
            SLTIU("sltiu");

            protected final String op;

            Op(String op) { this.op = op; }

            public String toString() { return op; }
        }

        public String toString() {
            return String.format("%s %s, %s, %d  # %s", op, rd, rs1, imm,
                                 comment);
        }
    }

    public record LoadInstr(Op op, Register rd, Register rs1, int imm,
                            String comment) implements Instr {
        public LoadInstr { assert - 2048 <= imm && imm < 2048; }

        public enum Op {
            LB("lb"),
            LBU("lbu"),
            LH("lh"),
            LHU("lhu"),
            LW("lw");

            protected final String op;

            Op(String op) { this.op = op; }

            public String toString() { return op; }
        }

        public String toString() {
            return String.format("%s %s, %d(%s)  # %s", op, rd, imm, rs1,
                                 comment);
        }
    }

    public record StoreInstr(Op op, Register rs2, Register rs1, int imm,
                             String comment) implements Instr {
        public StoreInstr { assert - 2048 <= imm && imm < 2048; }

        public enum Op {
            SB("sb"),
            SH("sh"),
            SW("sw");

            protected final String op;

            Op(String op) { this.op = op; }

            public String toString() { return op; }
        }

        public String toString() {
            return String.format("%s %s, %d(%s)  # %s", op, rs2, imm, rs1,
                                 comment);
        }
    }

    /*
     * These directly correspond to RISC-V instructions. However, if you want to
     * do optimizations based on the control flow graph, it may be easier to use
     * use a different branch instruction that takes one register argument and
     * two labels (`br rs1 label1 label2`) which branches to label1 if rs1 is
     * true; otherwise, branches to label2 if rs1 is false.
     *
     * If you choose to use that instruction, you will have to figure out how to
     * lower it to RISC-V.
     */
    public record BranchInstr(Op op, Register rs1, Register rs2, Label label,
                              String comment) implements Instr {
        public enum Op {
            BEQ("beq"),
            BGE("bge"),
            BGEU("bgeu"),
            BLT("blt"),
            BLTU("bltu"),
            BNE("bne");

            protected final String op;

            Op(String op) { this.op = op; }

            public String toString() { return op; }
        }

        public String toString() {
            return String.format("%s %s, %s, %s  # %s", op, rs1, rs2, label,
                                 comment);
        }
    }

    public record Jal(Register rd, Label label, String comment)
        implements Instr {}

    public record Jalr(Register rd, Register rs1, int imm, String comment)
        implements Instr {

        public Jalr { assert - 2048 <= imm && imm < 2048; }
    }

    /*
     * This is not a real RISC-V instruction, but it is useful to model it as an
     * instruction to make it easier to build a control-flow graph.
     */
    public record LocalLabel(Label label, String comment) implements Instr {}

    public record Auipc(Register rd, int immu, String comment)
        implements Instr {}

    public record Lui(Register rd, int immu, String comment) implements Instr {}

    public record Ecall(String comment) implements Instr {}

    /*
     * This a pseudo-instruction, but it is tricky to implement the logic for
     * loading large constants in terms of lui and addi, so we include it here.
     */
    public record Li(Register rd, int imm, String comment) implements Instr {}

    /*
     * This is a pseudo-instruction, but it is tricky to implement the logic for
     * addresses in terms of auipc and addi, so we include it here.
     */
    public record La(Register rd, Label label, String comment)
        implements Instr {}
}
