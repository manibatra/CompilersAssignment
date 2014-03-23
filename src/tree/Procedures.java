package tree;

import java.util.LinkedList;
import java.util.List;

import source.ErrorHandler;
import source.Severity;
import syms.Scope;

/** 
 * class Procedures - code for each procedure and start and finish
 * addresses. Handles a stack trace back for the stack machine
 * in the event of a runtime error.
 * @version $Revision: 17 $  $Date: 2013-05-13 08:25:39 +1000 (Mon, 13 May 2013) $
 */
public class Procedures {
    
    public class ProcedureStart {
        String procName;
        Scope locals;
        int start, finish;
        
        ProcedureStart( String procName, Scope locals, int start ) {
            super();
            this.procName = procName;
            this.locals = locals;
            this.start = start;
        }
        public Scope getLocals() {
            return locals;
        }
        @Override
        public String toString() {
            return procName + " " + start + " : " + finish;
        }
    }
    private List<ProcedureStart> procStarts;
    
    public Procedures() {
        procStarts = new LinkedList<ProcedureStart>();
    }
    public void addProcedureStart( String procName, Scope locals, int start ) {
        if( ! procStarts.isEmpty() ) {
            /* check last finish location is current location */
            ProcedureStart previous = procStarts.get( procStarts.size() - 1 );
            assert previous.finish == start;
        }
        procStarts.add( new ProcedureStart( procName, locals, start ) );
    }
    public void addProcedureFinish( int finish ) {
        assert ! procStarts.isEmpty() &&
            procStarts.get( procStarts.size() - 1 ).start <= finish;
        procStarts.get( procStarts.size() -1 ).finish = finish;
    }
    public ProcedureStart getProcedure( int pc ) {
        if( pc < procStarts.get(0).start ||
            procStarts.get(procStarts.size()-1).finish <= pc ) {
            // Must be in main program setup or finalization code
            return null;
        }
        for( ProcedureStart ps : procStarts ) {
            if( pc < ps.finish ) {
                return ps;
            }
        }
        // Can't get here
        ErrorHandler.getErrorHandler().errorMessage(
                "getProcedure failed assertion 2", Severity.FATAL );
        return null;
    }
    @Override
    public String toString() {
        String s = "";
        for( ProcedureStart start : procStarts ) {
            s += start.toString() + "\n";
        }
        return s;
    }
}
