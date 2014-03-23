package tree;

/** class CodePlusProcedures packages together an instance of
 * Code and an instance of Procedures.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $ 
 */
public class CodePlusProcedures {
    /** Instruction sequence for program */
    private Code instructionList;
    /** Start addresses of procedures */
    private Procedures procStarts;
    
    public CodePlusProcedures( Code instructionList, Procedures procStarts ) {
        this.instructionList = instructionList;
        this.procStarts = procStarts;
    }
    
    public Code getInstructionList() {
        return instructionList;
    }
    public Procedures getProcStarts() {
        return procStarts;
    }
}
