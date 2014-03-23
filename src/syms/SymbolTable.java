package syms;

import machine.StackMachine;
import syms.Type;
import syms.Type.ReferenceType;
import source.Position;
import tree.ConstExp;

/** A SymbolTable represents a sequence of scopes, one for each nested static
 * level, i.e., procedure, main program or the predefined scope. 
 * @version $Revision: 17 $  $Date: 2013-05-13 08:25:39 +1000 (Mon, 13 May 2013) $
 * It provides operations to add identifiers, look up identifiers, add a new 
 * scope and exit a scope. Searching for an identifier in an SymbolTable 
 * starts at the current scope, but then if it is not found, the search 
 * proceeds to the next outer (parent) scope, and so on.
 */
public class SymbolTable {
    /** Current scope */ 
    private Scope currentScope;

    /** Construct a symbol table and build the predefined scope
     * as its initial scope.
     */
    public SymbolTable() {
        currentScope = new Scope( null, 0 );
        // Add predefined symbols to predefined scope
        addType( "int", Position.NO_POSITION, Type.INTEGER_TYPE );
        addType( "boolean", Position.NO_POSITION, Type.BOOLEAN_TYPE );
        addConstant("false", Position.NO_POSITION, Type.BOOLEAN_TYPE, 
                StackMachine.FALSE_VALUE );
        addConstant("true", Position.NO_POSITION, Type.BOOLEAN_TYPE, 
                StackMachine.TRUE_VALUE );
        addOperator("+", Position.NO_POSITION, Type.ARITHMETIC_BINARY );
        addOperator("-", Position.NO_POSITION, Type.ARITHMETIC_BINARY );
        addOperator("*", Position.NO_POSITION, Type.ARITHMETIC_BINARY );
        addOperator("/", Position.NO_POSITION, Type.ARITHMETIC_BINARY );
        addOperator("=", Position.NO_POSITION, Type.INT_RELATIONAL_TYPE );
        addOperator("=", Position.NO_POSITION, Type.BOOL_RELATIONAL_TYPE );
        addOperator("!=", Position.NO_POSITION, Type.INT_RELATIONAL_TYPE );
        addOperator("!=", Position.NO_POSITION, Type.BOOL_RELATIONAL_TYPE );
        addOperator(">", Position.NO_POSITION, Type.INT_RELATIONAL_TYPE);
        addOperator("<", Position.NO_POSITION, Type.INT_RELATIONAL_TYPE);
        addOperator(">=", Position.NO_POSITION, Type.INT_RELATIONAL_TYPE);
        addOperator("<=", Position.NO_POSITION, Type.INT_RELATIONAL_TYPE);
    }
    /** Enter a new scope */
    public Scope newScope() {
        currentScope = new Scope( currentScope, currentScope.getLevel()+1 );
        return currentScope;
    }
    /** Re-enter a scope on a traversal */
    public Scope reenterScope( Scope newScope ) {
        currentScope = newScope;
        return currentScope;
    }
    /** Exit scope */
    public void leaveScope() {
        currentScope = currentScope.getParent();
    }
    /** Extend the current scope at the same level.
     * Assumes the variables have been allocated for the parent scope. */
    public Scope extendCurrentScope() {
        currentScope = new Scope( currentScope, currentScope.getLevel(), true );
        return currentScope;
    }
    /** Leave extended scope at same level */
    public void leaveExtendedScope() {
        currentScope = currentScope.getParent();
    }
    /** @return the level of the current scope: predefined scope is at level 0,
     *         main program scope is at level 1, and so on. 
     */
    public int getLevel() {
        return currentScope.getLevel();
    }
    public Scope getCurrentScope() {
        return currentScope;
    }
    /** Resolve references to type identifiers and allocate space 
     * for variables and check for circularly defined types and constants. */
    public void resolveCurrentScope() {
        for( SymEntry entry : currentScope.getEntries() ) {
//          System.out.println( "Symtab resolving " + entry.ident );
            entry.resolve();
        }
    }
    /** Look up an entry in all scopes starting from the current scope.
     * @param name of entry to be looked up
     * @return entry if one is found, else return null
     */
    public SymEntry lookup( String name ) {
        return currentScope.lookup( name );
    }
    /** Add an entry to the current scope
     * @return a reference to the entry unless an entry with the same 
     * name already exists in the current scope, in which case return null.
     */
    public SymEntry addEntry( SymEntry entry ) {
        return currentScope.addEntry( entry );
    }
    /** Add a CONSTANT entry to the current scope - known value
     * @return a reference to the new entry unless an entry with the same name
     *         already exists in the current scope, in which case return null.
     */
    public SymEntry.ConstantEntry addConstant(String name, Position posn,
            Type type, int val) {
        SymEntry.ConstantEntry entry = 
            new SymEntry.ConstantEntry(name, posn, currentScope, type, val);
        return (SymEntry.ConstantEntry) currentScope.addEntry(entry);
    }
    /** Add a CONSTANT entry to the current scope - tree to be evaluated later
     * @return a reference to the new entry unless an entry with the same name
     *         already exists in the current scope, in which case return null.
     */
    public SymEntry.ConstantEntry addConstant(String name, Position posn,
            ConstExp val) {
        SymEntry.ConstantEntry entry =
            new SymEntry.ConstantEntry(name, posn, currentScope, 
                Type.ERROR_TYPE, val);
        return (SymEntry.ConstantEntry) currentScope.addEntry(entry);
    }
    /** Lookup a CONSTANT entry in all scopes starting from the current scope.
     * @param name of the CONSTANT to be looked up
     * @return entry for name if one is found and it is a CONSTANT entry 
     *         otherwise return null; note that a non-CONSTANT entry may mask
     *         a CONSTANT entry of the same name in an outer scope.
     */
    public SymEntry.ConstantEntry lookupConstant(String name) {
        SymEntry entry = currentScope.lookup(name);
        if (entry == null || !(entry instanceof SymEntry.ConstantEntry)) {
            return null;
        }
        return (SymEntry.ConstantEntry) entry;
    }
    /** Add a TYPE entry to the current scope 
     * @return a reference to the new entry unless an entry with the same name
     *         already exists in the current scope, in which case return null.
     */
    public SymEntry.TypeEntry addType(String name, Position posn, Type type) {
        SymEntry.TypeEntry entry =
                new SymEntry.TypeEntry(name, posn, currentScope, type);
        return (SymEntry.TypeEntry) currentScope.addEntry(entry);
    }
    /** Lookup a TYPE entry in all scopes starting from the current scope. 
     * @param name of the TYPE to be looked up
     * @return entry for name if one is found and it is a TYPE entry 
     *         otherwise return null; note that a non-TYPE entry may mask
     *         a TYPE entry of the same name in an outer scope.
     */
    public SymEntry.TypeEntry lookupType(String name) {
        SymEntry entry = currentScope.lookup(name);
        if (entry == null || !(entry instanceof SymEntry.TypeEntry)) {
            return null;
        }
        return (SymEntry.TypeEntry) entry;
    }
    /** Add a VARIABLE entry to the current scope.
     * @return a reference to the new entry unless an entry with the same name
     *         already exists in the current scope, in which case return null.
     */
    public SymEntry.VarEntry addVariable(String name, Position posn, 
            ReferenceType type){
        SymEntry.VarEntry entry =
                new SymEntry.VarEntry(name, posn, currentScope, type );
        return (SymEntry.VarEntry) currentScope.addEntry(entry);
    }
    /** Lookup a VARIABLE entry in all scopes starting from the current scope.
     * @param name of the VARIABLE to be looked up
     * @return entry for name if one is found and it is a VARIABLE entry 
     *         otherwise return null; note that a non-VARIABLE entry may mask
     *         a VARIABLE entry of the same name in an outer scope.
     */
    public SymEntry.VarEntry lookupVariable(String name) {
        SymEntry entry = currentScope.lookup(name);
        if (entry == null || !(entry instanceof SymEntry.VarEntry)) {
            return null;
        }
        return (SymEntry.VarEntry) entry;
    }
    /** Add a PROCEDURE entry to the current scope
     * @return a reference to the new entry unless an entry with the same name
     *         already exists in the current scope, in which case return null.
     */
    public SymEntry.ProcedureEntry addProcedure(String name, Position posn) {
        SymEntry.ProcedureEntry entry =
                new SymEntry.ProcedureEntry(name, posn, currentScope );
        return (SymEntry.ProcedureEntry) currentScope.addEntry(entry);
    }
    public SymEntry.ProcedureEntry addProcedure(String name, Position posn,
            Type.ProcedureType type ) {
        SymEntry.ProcedureEntry entry =
                new SymEntry.ProcedureEntry(name, posn, currentScope, type );
        return (SymEntry.ProcedureEntry) currentScope.addEntry(entry);
    }
    /** Lookup a PROCEDURE entry in all scopes starting from the current scope.
     * @param name of the PROCEDURE to be looked up
     * @return entry for name if one is found and it is a PROCEDURE entry 
     *         otherwise return null; note that a non-PROCEDURE entry may mask
     *         a PROCEDURE entry of the same name in an outer scope.
     */
    public SymEntry.ProcedureEntry lookupProcedure(String name) {
        SymEntry entry = currentScope.lookup(name);
        if (entry == null || !(entry instanceof SymEntry.ProcedureEntry)) {
            return null;
        }
        return (SymEntry.ProcedureEntry) entry;
    }
    /** Add an OPERATOR entry to the current scope
     * @return a reference to the new entry unless an entry with the same name
     *         already exists in the current scope, in which case return null.
     */
    public SymEntry.OperatorEntry addOperator(String name, Position posn,
            Type type ) {
        SymEntry.OperatorEntry entry = lookupOperator( name );
        if( entry == null ) {
            /** Create a new entry for the operator */
            entry = new SymEntry.OperatorEntry(name, posn, currentScope, type );
            return (SymEntry.OperatorEntry) currentScope.addEntry(entry);
        } else if( entry.getLevel() == currentScope.getLevel() ) {
            /** Already defined at the current level - extend intersection type */
            entry.extendType( type );
            return entry;
        } else {
            /** Defined at an outer level create new entry with old
             * intersection type and extend with new type.
             */
            entry = new SymEntry.OperatorEntry(name, posn, currentScope, 
                    entry.getType() );
            entry.extendType( type );
            return (SymEntry.OperatorEntry) currentScope.addEntry(entry);
        }
    }
    /** Lookup an OPERATOR entry in all scopes starting from the current scope.
     * @param name of the OPERATOR to be looked up
     * @return entry for name if one is found and it is an OPERATOR entry 
     *         otherwise return null; note that a non-OPERATOR entry may mask
     *         a OPERATOR entry of the same name in an outer scope.
     */
    public SymEntry.OperatorEntry lookupOperator(String name) {
        SymEntry entry = currentScope.lookup(name);
        if (entry == null || !(entry instanceof SymEntry.OperatorEntry)) {
            return null;
        }
        return (SymEntry.OperatorEntry) entry;
    }
    // TODO The formatting produced here could be better
    @Override
    public String toString() {
        return "\nSymTable " + " Level=" + currentScope.getLevel() + 
                "\n" + currentScope;
    }
}
