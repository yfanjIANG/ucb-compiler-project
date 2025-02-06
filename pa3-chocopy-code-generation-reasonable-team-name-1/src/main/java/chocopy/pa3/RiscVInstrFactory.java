package chocopy.pa3;

import static chocopy.pa3.RiscV.BinaryImmInstr.Op.*;
import static chocopy.pa3.RiscV.BinaryInstr.Op.*;
import static chocopy.pa3.RiscVAsmWriter.PhysicalRegister.*;

import chocopy.common.codegen.Label;
import chocopy.pa3.RiscV.*;

class RiscVInstrFactory {
    /** ----- Binary instructions ----- **/
    BinaryInstr add(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(ADD, rd, rs1, rs2, comment);
    }

    BinaryInstr add(Register rd, Register rs1, Register rs2) {
        return add(rd, rs1, rs2, "");
    }

    BinaryInstr sub(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(SUB, rd, rs1, rs2, comment);
    }

    BinaryInstr sub(Register rd, Register rs1, Register rs2) {
        return sub(rd, rs1, rs2, "");
    }

    BinaryInstr and(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(AND, rd, rs1, rs2, comment);
    }

    BinaryInstr and(Register rd, Register rs1, Register rs2) {
        return and(rd, rs1, rs2, "");
    }

    BinaryInstr or(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(OR, rd, rs1, rs2, comment);
    }

    BinaryInstr or(Register rd, Register rs1, Register rs2) {
        return or(rd, rs1, rs2, "");
    }

    BinaryInstr xor(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(XOR, rd, rs1, rs2, comment);
    }

    BinaryInstr xor(Register rd, Register rs1, Register rs2) {
        return xor(rd, rs1, rs2, "");
    }

    BinaryInstr sll(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(SLL, rd, rs1, rs2, comment);
    }

    BinaryInstr sll(Register rd, Register rs1, Register rs2) {
        return sll(rd, rs1, rs2, "");
    }

    BinaryInstr srl(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(SRL, rd, rs1, rs2, comment);
    }

    BinaryInstr srl(Register rd, Register rs1, Register rs2) {
        return srl(rd, rs1, rs2, "");
    }

    BinaryInstr sra(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(SRA, rd, rs1, rs2, comment);
    }

    BinaryInstr sra(Register rd, Register rs1, Register rs2) {
        return sra(rd, rs1, rs2, "");
    }

    BinaryInstr slt(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(SLT, rd, rs1, rs2, comment);
    }

    BinaryInstr slt(Register rd, Register rs1, Register rs2) {
        return slt(rd, rs1, rs2, "");
    }

    BinaryInstr sltu(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(SLTU, rd, rs1, rs2, comment);
    }

    BinaryInstr sltu(Register rd, Register rs1, Register rs2) {
        return sltu(rd, rs1, rs2, "");
    }

    BinaryInstr mul(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(MUL, rd, rs1, rs2, comment);
    }

    BinaryInstr mul(Register rd, Register rs1, Register rs2) {
        return mul(rd, rs1, rs2, "");
    }

    BinaryInstr div(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(DIV, rd, rs1, rs2, comment);
    }

    BinaryInstr div(Register rd, Register rs1, Register rs2) {
        return div(rd, rs1, rs2, "");
    }

    BinaryInstr rem(Register rd, Register rs1, Register rs2, String comment) {
        return new BinaryInstr(REM, rd, rs1, rs2, comment);
    }

    BinaryInstr rem(Register rd, Register rs1, Register rs2) {
        return rem(rd, rs1, rs2, "");
    }

    /** ----- Binary immediate instructions ----- **/
    BinaryImmInstr addi(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(ADDI, rd, rs1, imm, comment);
    }

    BinaryImmInstr addi(Register rd, Register rs1, int imm) {
        return addi(rd, rs1, imm, "");
    }

    BinaryImmInstr andi(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(ANDI, rd, rs1, imm, comment);
    }

    BinaryImmInstr andi(Register rd, Register rs1, int imm) {
        return andi(rd, rs1, imm, "");
    }

    BinaryImmInstr ori(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(ORI, rd, rs1, imm, comment);
    }

    BinaryImmInstr ori(Register rd, Register rs1, int imm) {
        return ori(rd, rs1, imm, "");
    }

    BinaryImmInstr xori(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(XORI, rd, rs1, imm, comment);
    }

    BinaryImmInstr xori(Register rd, Register rs1, int imm) {
        return xori(rd, rs1, imm, "");
    }

    BinaryImmInstr slli(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(SLLI, rd, rs1, imm, comment);
    }

    BinaryImmInstr slli(Register rd, Register rs1, int imm) {
        return slli(rd, rs1, imm, "");
    }

    BinaryImmInstr srli(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(SRLI, rd, rs1, imm, comment);
    }

    BinaryImmInstr srli(Register rd, Register rs1, int imm) {
        return srli(rd, rs1, imm, "");
    }

    BinaryImmInstr srai(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(SRAI, rd, rs1, imm, comment);
    }

    BinaryImmInstr srai(Register rd, Register rs1, int imm) {
        return srai(rd, rs1, imm, "");
    }

    BinaryImmInstr slti(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(SLTI, rd, rs1, imm, comment);
    }

    BinaryImmInstr slti(Register rd, Register rs1, int imm) {
        return slti(rd, rs1, imm, "");
    }

    BinaryImmInstr sltiu(Register rd, Register rs1, int imm, String comment) {
        return new BinaryImmInstr(SLTIU, rd, rs1, imm, comment);
    }

    BinaryImmInstr sltiu(Register rd, Register rs1, int imm) {
        return sltiu(rd, rs1, imm, "");
    }

    /** ----- Unary pseudo-instructions ----- **/
    Instr mv(Register rd, Register rs, String comment) {
        return addi(rd, rs, 0, comment);
    }

    Instr mv(Register rd, Register rs) { return mv(rd, rs, ""); }

    Instr not(Register rd, Register rs, String comment) {
        return xori(rd, rs, -1, comment);
    }

    Instr not(Register rd, Register rs) { return not(rd, rs, ""); }

    Instr neg(Register rd, Register rs, String comment) {
        return sub(rd, ZERO, rs, comment);
    }

    Instr neg(Register rd, Register rs) { return neg(rd, rs, ""); }

    Instr seqz(Register rd, Register rs, String comment) {
        return sltiu(rd, rs, 1, comment);
    }

    Instr seqz(Register rd, Register rs) { return seqz(rd, rs, ""); }

    Instr snez(Register rd, Register rs, String comment) {
        return sltu(rd, ZERO, rs, comment);
    }

    Instr snez(Register rd, Register rs) { return snez(rd, rs, ""); }

    Instr sltz(Register rd, Register rs, String comment) {
        return slt(rd, rs, ZERO, comment);
    }

    Instr sltz(Register rd, Register rs) { return sltz(rd, rs, ""); }

    Instr sgtz(Register rd, Register rs, String comment) {
        return slt(rd, ZERO, rs, comment);
    }

    Instr sgtz(Register rd, Register rs) { return sgtz(rd, rs, ""); }

    /** ----- Load instructions ----- **/
    LoadInstr lb(Register rd, Register rs1, int imm, String comment) {
        return new LoadInstr(LoadInstr.Op.LB, rd, rs1, imm, comment);
    }

    LoadInstr lb(Register rd, Register rs1, int imm) {
        return lb(rd, rs1, imm, "");
    }

    LoadInstr lbu(Register rd, Register rs1, int imm, String comment) {
        return new LoadInstr(LoadInstr.Op.LBU, rd, rs1, imm, comment);
    }

    LoadInstr lbu(Register rd, Register rs1, int imm) {
        return lbu(rd, rs1, imm, "");
    }

    LoadInstr lh(Register rd, Register rs1, int imm, String comment) {
        return new LoadInstr(LoadInstr.Op.LH, rd, rs1, imm, comment);
    }

    LoadInstr lh(Register rd, Register rs1, int imm) {
        return lh(rd, rs1, imm, "");
    }

    LoadInstr lhu(Register rd, Register rs1, int imm, String comment) {
        return new LoadInstr(LoadInstr.Op.LHU, rd, rs1, imm, comment);
    }

    LoadInstr lhu(Register rd, Register rs1, int imm) {
        return lhu(rd, rs1, imm, "");
    }

    LoadInstr lw(Register rd, Register rs1, int imm, String comment) {
        return new LoadInstr(LoadInstr.Op.LW, rd, rs1, imm, comment);
    }

    LoadInstr lw(Register rd, Register rs1, int imm) {
        return lw(rd, rs1, imm, "");
    }

    /** ----- Store instructions ----- **/
    StoreInstr sb(Register rs2, Register rs1, int imm, String comment) {
        return new StoreInstr(StoreInstr.Op.SB, rs2, rs1, imm, comment);
    }

    StoreInstr sb(Register rs2, Register rs1, int imm) {
        return sb(rs2, rs1, imm, "");
    }

    StoreInstr sh(Register rs2, Register rs1, int imm, String comment) {
        return new StoreInstr(StoreInstr.Op.SH, rs2, rs1, imm, comment);
    }

    StoreInstr sh(Register rs2, Register rs1, int imm) {
        return sh(rs2, rs1, imm, "");
    }

    StoreInstr sw(Register rs2, Register rs1, int imm, String comment) {
        return new StoreInstr(StoreInstr.Op.SW, rs2, rs1, imm, comment);
    }

    StoreInstr sw(Register rs2, Register rs1, int imm) {
        return sw(rs2, rs1, imm, "");
    }

    /** ----- Branch instructions ----- **/
    BranchInstr beq(Register rs1, Register rs2, Label label, String comment) {
        return new BranchInstr(BranchInstr.Op.BEQ, rs1, rs2, label, comment);
    }

    BranchInstr beq(Register rs1, Register rs2, Label label) {
        return beq(rs1, rs2, label, "");
    }

    BranchInstr bge(Register rs1, Register rs2, Label label, String comment) {
        return new BranchInstr(BranchInstr.Op.BGE, rs1, rs2, label, comment);
    }

    BranchInstr bge(Register rs1, Register rs2, Label label) {
        return bge(rs1, rs2, label, "");
    }

    BranchInstr bgeu(Register rs1, Register rs2, Label label, String comment) {
        return new BranchInstr(BranchInstr.Op.BGEU, rs1, rs2, label, comment);
    }

    BranchInstr bgeu(Register rs1, Register rs2, Label label) {
        return bgeu(rs1, rs2, label, "");
    }

    BranchInstr blt(Register rs1, Register rs2, Label label, String comment) {
        return new BranchInstr(BranchInstr.Op.BLT, rs1, rs2, label, comment);
    }

    BranchInstr blt(Register rs1, Register rs2, Label label) {
        return blt(rs1, rs2, label, "");
    }

    BranchInstr bltu(Register rs1, Register rs2, Label label, String comment) {
        return new BranchInstr(BranchInstr.Op.BLTU, rs1, rs2, label, comment);
    }

    BranchInstr bltu(Register rs1, Register rs2, Label label) {
        return bltu(rs1, rs2, label, "");
    }

    BranchInstr bne(Register rs1, Register rs2, Label label, String comment) {
        return new BranchInstr(BranchInstr.Op.BNE, rs1, rs2, label, comment);
    }

    BranchInstr bne(Register rs1, Register rs2, Label label) {
        return bne(rs1, rs2, label, "");
    }

    BranchInstr beqz(Register rs1, Label label, String comment) {
        return new BranchInstr(BranchInstr.Op.BEQ, rs1, ZERO, label, comment);
    }

    BranchInstr beqz(Register rs1, Label label) { return beqz(rs1, label, ""); }

    BranchInstr bnez(Register rs1, Label label, String comment) {
        return new BranchInstr(BranchInstr.Op.BNE, rs1, ZERO, label, comment);
    }

    BranchInstr bnez(Register rs1, Label label) { return bnez(rs1, label, ""); }

    /** ----- Jump instructions ----- **/
    Jal jal(Register rd, Label label, String comment) {
        return new Jal(rd, label, comment);
    }

    Jal jal(Register rd, Label label) { return jal(rd, label, ""); }

    // Pseudo-instruction
    Jal j(Label label, String comment) { return jal(ZERO, label, comment); }

    Jal j(Label label) { return j(label, ""); }

    Jalr jalr(Register rd, Register rs1, int imm, String comment) {
        return new Jalr(rd, rs1, imm, comment);
    }

    Jalr jalr(Register rd, Register rs1, int imm) {
        return jalr(rd, rs1, imm, "");
    }

    // Pseudo-instruction
    Jalr jr(Register rs1, String comment) {
        return jalr(ZERO, rs1, 0, comment);
    }

    Jalr jr(Register rs1) { return jr(rs1, ""); }

    /** ----- Other instructions ----- **/
    LocalLabel label(Label label, String comment) {
        return new LocalLabel(label, comment);
    }

    // Not an instruction
    LocalLabel label(Label label) { return label(label, ""); }

    Auipc auipc(Register rd, int immu, String comment) {
        return new Auipc(rd, immu, comment);
    }

    Auipc auipc(Register rd, int immu) { return auipc(rd, immu, ""); }

    Lui lui(Register rd, int immu, String comment) {
        return new Lui(rd, immu, comment);
    }

    Lui lui(Register rd, int immu) { return lui(rd, immu, ""); }

    Ecall ecall(String comment) { return new Ecall(comment); }

    Ecall ecall() { return ecall(""); }

    // Pseudo-instruction
    Li li(Register rd, int imm, String comment) {
        return new Li(rd, imm, comment);
    }

    Li li(Register rd, int imm) { return li(rd, imm, ""); }

    // Pseudo-instruction
    La la(Register rd, Label label, String comment) {
        return new La(rd, label, comment);
    }

    La la(Register rd, Label label) { return la(rd, label, ""); }

    // Pseudo-instruction
    Instr ret(String comment) { return jalr(ZERO, RA, 0, comment); }

    Instr ret() { return ret(""); }
}
