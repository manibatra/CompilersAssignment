package tree;

/** 
 * interface TreeVisitor - Visitor pattern for declarations and procedures.
 * @version $Revision: 15 $  $Date: 2013-05-08 13:58:21 +1000 (Wed, 08 May 2013) $
 * Provides the interface for the visitor pattern to be applied to an
 * abstract syntax tree. A class implementing this interface (such as the
 * code generator) must provide implementations for visit methods for
 * each of the tree node type. 
 * For example, the visit methods provided by the code generator tree
 * visitor implement the code generation for each type of tree node. 
 */
public interface TreeTransform<ResultType> {

    ResultType visitProgramNode(Tree.ProgramNode node);
    
    ResultType visitBlockNode(Tree.BlockNode node);

    ResultType visitDeclListNode(DeclNode.DeclListNode node);

    ResultType visitProcedureNode(DeclNode.ProcedureNode node);
}
