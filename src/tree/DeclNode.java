package tree;

import java.util.LinkedList;
import java.util.List;

import syms.Scope;
import syms.SymEntry;
import syms.Type;

/**
 * class DeclNode - Handles Declarations lists and procedures.
 * @version $Revision: 15 $  $Date: 2013-05-08 13:58:21 +1000 (Wed, 08 May 2013) $ 
 * DeclNode is an abstract class. 
 * The classes defined within DeclNode extend it.
 */
public abstract class DeclNode {
    
    /** Constructor */
    protected DeclNode() {
        super();
    }
    /** Simple visitor pattern implemented in subclasses */
    public abstract void accept( TreeVisitor visitor );
    /** Code generation visitor pattern implemented in subclasses */
    public abstract Code accept( TreeTransform<Code> visitor );

    /** Tree node representing a list of (procedure) declarations */
    public static class DeclListNode extends DeclNode {
        List<DeclNode> declarations;
        
        public DeclListNode() {
            declarations = new LinkedList<DeclNode>();
        }
        public List<DeclNode> getDeclarations() {
            return declarations;
        }
        public void addDeclaration( DeclNode declaration ) {
            declarations.add( declaration );
        }
        @Override
        public void accept(TreeVisitor visitor) {
            visitor.visitDeclListNode( this );
        }
        @Override
        public Code accept( TreeTransform<Code> visitor) {
            return visitor.visitDeclListNode( this );
        }
    }

    /** Tree node representing a single procedure. */
    public static class ProcedureNode extends DeclNode {
        private SymEntry.ProcedureEntry procEntry;
        private Tree.BlockNode block;

        public ProcedureNode( SymEntry.ProcedureEntry entry, 
                Tree.BlockNode block ) {
            this.procEntry = entry;
            this.block = block;
        }
        @Override
        public void accept( TreeVisitor visitor ) {
            visitor.visitProcedureNode( this );
        }
        @Override
        public Code accept( TreeTransform<Code> visitor ) {
            return visitor.visitProcedureNode( this );
        }
        public SymEntry.ProcedureEntry getProcEntry() {
            return procEntry;
        }
        public Tree.BlockNode getBlock() {
            return block;
        }
        @Override
        public String toString( ) {
            return "PROCEDURE " + procEntry.getIdent() + " = " + 
                block.toString();
        }
    }
}
