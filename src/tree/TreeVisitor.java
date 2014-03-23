package tree;


/** 
 * interface TreeVisitor - Visitor pattern for declarations and procvedures.
 * @version $Revision: 15 $  $Date: 2013-05-08 13:58:21 +1000 (Wed, 08 May 2013) $
 * Provides the interface for the visitor pattern to be applied to an
 * abstract syntax tree. A class implementing this interface (such as the
 * static checker) must provide implementations for visit methods for
 * each of the tree node type. 
 * For example, the visit methods provided by the static checker tree
 * visitor implement the type checks for each type of tree node. 
 */
public interface TreeVisitor {

    void visitProgramNode(Tree.ProgramNode node);

    void visitBlockNode(Tree.BlockNode node);
    
    void visitDeclListNode(DeclNode.DeclListNode node);

    void visitProcedureNode(DeclNode.ProcedureNode node);
}
