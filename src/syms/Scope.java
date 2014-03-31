package syms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import machine.StackMachine;
import syms.SymEntry;

/** A Scope represents a static scope for a procedure, main program or 
 * the predefined scope. 
 * @version $Revision: 17 $  $Date: 2013-05-13 08:25:39 +1000 (Mon, 13 May 2013) $
 * It provides operations to add and look up identifiers. 
 * Searching for an identifier in a scope starts at the current scope, 
 * but then if it is not found, the search proceeds to the next outer 
 * (parent) scope, and so on. 
 */
public class Scope {
    /** Parent Scope */
    private Scope parent;
    /** Static level of this scope */
    private int level;
    /** Symbol table entries */
    private Map<String, SymEntry> entries;
    /** space allocated for local variables within this scope */
    private int variableSpace;
    /** true if this is an extension of its parent scope */
    private boolean extension;

    /** This constructs a single scope within a symbol table
     * that is linked to the parent scope, which may be null to
     * indicate that there is no parent. 
     * @param parent scope
     * @param level of nesting of scope 
     */
    public Scope( Scope parent, int level ) {
        this.parent = parent;
        this.level = level;
        /* Initially empty */
        this.entries = new HashMap<String, SymEntry>();
        variableSpace = 0;
        extension = false;
    }
    /** Scope constructor for use in extending the current scope 
     * @param parent scope
     * @param level of nesting of scope
     * @param extension - true if an extension
     */
    public Scope( Scope parent, int level, boolean extension ) {
        this( parent, level );
        this.extension = extension;
    }
    public Scope getParent() {
        return parent;
    }
    public int getLevel() {
        return level;
    }
    /** @return the set of entries in this scope */
    public Collection<SymEntry> getEntries() {
        return entries.values();
    }
    /** Lookup id starting in the current scope and 
     * thence in the parent scope and so on.
     * @param id Identifier to search for.
     * @return symbol table entry for the id, or null if not found.
     */
    public SymEntry lookup( String id ) {
        if( entries.containsKey( id ) ) {
            return entries.get( id );
        }
        if( parent != null ) {
            return parent.lookup( id );
        }
        return null;
    }
    /** Add an entry to the scope unless an entry for the same name exists.
     * @param entry to be added
     * @return the entry added or null is it already exited in this scope. 
     */
    public SymEntry addEntry( SymEntry entry ) {
        if( entries.containsKey( entry.getIdent() ) ) {
            System.out.println("In here");
        	return null;
            
        } else {
            entries.put( entry.getIdent(), entry );
            return entry;
        }
    }
    /** @return the amount of space allocated to local variables
     * within the current scope. */
    public int getVariableSpace() {
        if( extension ) {
            return parent.getVariableSpace();
        } else {
            return variableSpace;
        }
    }
    /** Allocate space for a local variable.
     * @param size is the amount of space required for the variable.
     * @return address (offset) of allocated space */
    public int allocVariableSpace( int size ) {
        if( extension ) {
            return parent.allocVariableSpace (size );
        } else {
            int base = variableSpace;
            variableSpace += size;
            return StackMachine.LOCALS_BASE + base;
        }
    }

    // TODO The formatting produced here could be better
    @Override
    public String toString() {
        return "\nScope " + "\n" + entries +
            (parent == null ? "" : parent);
    }
}
