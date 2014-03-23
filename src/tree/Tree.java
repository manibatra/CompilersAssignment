package tree;

import syms.Scope;
import syms.SymEntry;
import syms.SymbolTable;
/** 
 * class Tree - Abstract syntax tree nodes and support functions.
 * @version $Revision: 17 $  $Date: 2013-05-13 08:25:39 +1000 (Mon, 13 May 2013) $
 * Uses visitor pattern in order to separate the static semantic checks
 * and code generation from tree building. 
 * The accept method for each type of tree node, calls the corresponding
 * visit method of the tree visitor. 
 */
public abstract class Tree {

    /** Tree node representing the main program. */
    public static class ProgramNode {
        private SymbolTable baseSymbolTable;
        private BlockNode mainProc;

        public ProgramNode( SymbolTable baseSyms, BlockNode mainProc ) {
            this.baseSymbolTable = baseSyms;
            this.mainProc = mainProc;
        }
        public void accept( TreeVisitor visitor ) {
            visitor.visitProgramNode( this );
        }
        public Code accept( TreeTransform<Code> visitor ) {
            return visitor.visitProgramNode( this );
        }
        public SymbolTable getBaseSymbolTable() {
            return baseSymbolTable;
        }
        public BlockNode getBlock() {
            return mainProc;
        }
        @Override
        public String toString() {
            return getBlock().toString();
        }
    }

    /** Node representing a Block consisting of declarations and
     * body of a procedure, function, or the main program. */
    public static class BlockNode {
        protected DeclNode.DeclListNode procedures;
        protected StatementNode body;
        protected Scope blockLocals;

        /** Constructor for a block within a procedure */
        public BlockNode(DeclNode.DeclListNode procedures, StatementNode body) {
            this.procedures = procedures;
            this.body = body;
        }
        public void accept( TreeVisitor visitor ) {
            visitor.visitBlockNode( this );
        }
        public Code accept( TreeTransform<Code> visitor ) {
            return visitor.visitBlockNode( this );
        }
        public DeclNode.DeclListNode getProcedures() {
            return procedures;
        }
        public StatementNode getBody() {
            return body;
        }
        public Scope getBlockLocals() {
            return blockLocals;
        }
        public void setBlockLocals( Scope blockLocals ) {
            this.blockLocals = blockLocals;
        }
        @Override
        public String toString() {
            return "BLOCK " + getProcedures() + " BEGIN" + body + " END";
        }
    }

}
