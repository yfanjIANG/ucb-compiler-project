package chocopy.pa3;

import chocopy.common.astnodes.Program;

/** Interface to code generator. */
public class StudentCodeGen {

    /**
     * Perform code generation from PROGRAM, assumed to be well-typed,
     * to RISC-V, returning the assembly code.  DEBUG iff --debug was on the
     * command line.
     */
    public static String process(Program program, boolean debug) {
        /* Emit code into a ByteOutputStream, and convert to a string.
         * If you need instructions not provided by RiscVAsmWriter, simply
         * use an extension of it. */
        try {
            RiscVAsmWriter asmWriter = new RiscVAsmWriter();
            CodeGenBase cgen = new CodeGenImpl(asmWriter);
            cgen.generate(program);

            return asmWriter.toString();
        } catch (IllegalStateException | IllegalArgumentException |
                UnsupportedOperationException e) {
            System.err.println("Error performing code generation. "
                               + "Re-run with --debug to see stack trace.");
            if (debug) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
