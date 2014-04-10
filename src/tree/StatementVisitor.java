package tree;

/** 
 * interface StatementVisitor - Provides the interface for the visitor pattern 
 * to be applied to an abstract syntax tree node for a statement. 
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 * A class implementing this interface must provide implementations for visit 
 * methods for each of the statement node type. 
 */
public interface StatementVisitor {

    void visitStatementErrorNode( StatementNode.ErrorNode node );

    void visitStatementListNode( StatementNode.ListNode node );

    void visitAssignmentNode( StatementNode.AssignmentNode node);

    void visitWriteNode( StatementNode.WriteNode node);

    void visitCallNode( StatementNode.CallNode node);
    
    void visitIfNode( StatementNode.IfNode node);

    void visitWhileNode( StatementNode.WhileNode node);
    
    void visitSkipNode( StatementNode.SkipNode node);
    
    void visitForNode( StatementNode.ForNode node);
}
