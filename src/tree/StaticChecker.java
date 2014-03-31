package tree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import machine.StackMachine;
import source.Errors;
import source.Position;
import source.Severity;
import syms.SymEntry;
import syms.SymbolTable;
import syms.Type;
import syms.Type.IncompatibleTypes;
import syms.Type.ReferenceType;
import tree.DeclNode.DeclListNode;
import tree.ExpNode.IdentifierNode;
import tree.ExpNode.VariableNode;
import tree.StatementNode.ForNode;
import tree.StatementNode.SkipNode;
import tree.Tree.*;

/** class StaticSemantics - Performs the static semantic checks on
 * the abstract syntax tree using a visitor pattern to traverse the tree.
 * @version $Revision: 17 $  $Date: 2013-05-13 08:25:39 +1000 (Mon, 13 May 2013) $
 * See the notes on the static semantics of PL0 to understand the PL0
 * type system in detail.
 */
public class StaticChecker implements TreeVisitor, StatementVisitor, 
ExpTransform<ExpNode> {

	/** The static checker maintains a symbol table reference.
	 * Its current scope is that for the procedure 
	 * currently being processed.
	 */
	private SymbolTable symtab;
	/** Errors are reported through the error handler. */
	private Errors errors;

	/** Construct a static checker for PL0.
	 * @param errors is the error message handler.
	 */
	public StaticChecker( Errors errors ) {
		super();
		this.errors = errors;
	}
	/** The tree traversal starts with a call to visitProgramNode.
	 * Then its descendants are visited using visit methods for each
	 * node type, which are called using the visitor pattern "accept"
	 * (or "transform" for expression nodes) method of the abstract
	 * syntax tree node.
	 */
	public void visitProgramNode(ProgramNode node) {
		// Set up the symbol table to be that for the main program.
		symtab = node.getBaseSymbolTable();
		// Set the current symbol table scope to that for the procedure.
		symtab.reenterScope( node.getBlock().getBlockLocals() );
		// resolve all references to identifiers with the declarations
		symtab.resolveCurrentScope();
		// Check the main program block.
		node.getBlock().accept( this );
		// Restore the symbol table to the parent scope (not really necessary)
		symtab.leaveScope();
	}
	public void visitBlockNode(BlockNode node) {
		// Check the procedures, if any.
		node.getProcedures().accept( this );
		// Check the body of the block.
		node.getBody().accept( this );
	}
	public void visitDeclListNode(DeclListNode node) {
		for( DeclNode declaration : node.getDeclarations() ) {
			declaration.accept( this );
		}
	}
	/** Procedure, function or main program node */
	public void visitProcedureNode(DeclNode.ProcedureNode node) {
		SymEntry.ProcedureEntry procEntry = node.getProcEntry();
		// Set the current symbol table scope to that for the procedure.
		symtab.reenterScope( procEntry.getLocalScope() );
		// resolve all references to identifiers with the declarations
		symtab.resolveCurrentScope();
		// Check the block of the procedure.
		BlockNode block = node.getBlock();
		block.accept( this );
		// Restore the symbol table to the parent scope.
		symtab.leaveScope();
	}
	/*************************************************
	 *  Statement node static checker visit methods
	 *************************************************/
	public void visitStatementErrorNode(StatementNode.ErrorNode node) {
		// Nothing to check - already invalid.
	}

	//    public void visitAssignmentNode(StatementNode.AssignmentNode node) {
	//        // Check the left side left value.
	//        ExpNode left = node.getVariable().transform( this );
	//        node.setVariable( left );
	//        // Check the right side expression.
	//        ExpNode exp = node.getExp().transform( this );
	//        node.setExp( exp );
	//        // Validate that it is a true left value and not a constant.
	//        Type lvalType = left.getType();
	//        if( ! (lvalType instanceof Type.ReferenceType) ) {
	//            error( "variable (i.e., L-Value) expected", left.getPosition() );
	//        } else {
	//            /* Validate that the right expression is assignment
	//             * compatible with the left value. This may require that the 
	//             * right side expression is coerced to the dereferenced
	//             * type of the left side LValue. */
	//            Type baseType = ((Type.ReferenceType)lvalType).getBaseType();
	//            node.setExp( baseType.coerceExp( exp ) );
	//        }
	//    }

	
	
	
	public void visitAssignmentNode(StatementNode.AssignmentNode node) {
		// Check the left side left value.
		List<ExpNode> left = new ArrayList<ExpNode>();

		HashSet<String> ids = new HashSet<String>();

		boolean duplicates = false;

		for(ExpNode var : node.getVariable()){
			//have to yet check for the case when two identifiers are the same 
						left.add(var.transform( this ));
						
			if(!(ids.add(((IdentifierNode)var).getId()))){

				duplicates = true;

			}

		}

		node.setVariable( left );
		// Check the right side expression.

		List<ExpNode> exp = new ArrayList<ExpNode>();
		for( ExpNode e : node.getExp()){

			exp.add(e.transform( this ));

		}

		node.setExp( exp );
		// Validate that it is a true left value and not a constant.

		List<Type> lvalType = new ArrayList<Type>();
		for( ExpNode var : left) {

			lvalType.add(var.getType());
			//System.out.println(var.getType());
		}

		if(left.size() < exp.size()){

			error("too few l-values", left.get(0).getPosition());

		} else if(left.size() > exp.size()) {

			error("too few expressions", left.get(0).getPosition());

		} else if(duplicates){

			error("there is one or more duplicate variables present", left.get(0).getPosition());

		} else {	
			List<ExpNode> finalExp = new ArrayList<ExpNode>();
			for(int i = 0; i < left.size(); i++) {

				if( ! (lvalType.get(i) instanceof Type.ReferenceType) ) {

					error( "variable (i.e. , L-Value) expected", left.get(i).getPosition() );
				} else  {
					//yet to fix coercion
					if(symtab.lookupVariable(((ExpNode.VariableNode)left.get(i)).getId()).isControlVar()){
						
						error("value cannot be assigned to a control variable", left.get(i).getPosition());
						
					} else {


					Type baseType = ((Type.ReferenceType)lvalType.get(i)).getBaseType();
					finalExp.add( baseType.coerceExp( exp.get(i)));

					}

				}

			}

			node.setExp(finalExp);


		} 

	}

	public void visitWriteNode(StatementNode.WriteNode node) {
		// Check the expression being written.
		ExpNode exp = node.getExp().transform( this );
		// coerce expression to be of type integer,
		// or complain if not possible.
		node.setExp( Type.INTEGER_TYPE.coerceExp( exp ) );
	}

	public void visitCallNode(StatementNode.CallNode node) {
		SymEntry.ProcedureEntry procEntry;
		Type.ProcedureType procType;
		// Look up the symbol table entry for the procedure.
		SymEntry entry = symtab.lookup( node.getId() );
		if( entry instanceof SymEntry.ProcedureEntry ) {
			procEntry = (SymEntry.ProcedureEntry)entry;
			node.setEntry( procEntry );
			procType = procEntry.getType();
		} else {
			error( "Procedure identifier required", node.getPosition() );
			return;
		}
	}
	
	@Override
	public void visitForNode(StatementNode.ForNode node) {
		// TODO Auto-generated method stub

		ExpNode lowerBound = node.getLowerBound().transform(this);
		ExpNode upperBound = node.getUpperBound().transform(this);

		node.setLowerBound(lowerBound);
		node.setUpperBound(upperBound);

		Type lowerT = lowerBound.getType();
		
		if( !(lowerT instanceof Type.ReferenceType) && !(lowerT instanceof Type.SubrangeType)
				&& !(lowerT instanceof Type.ScalarType)) {
			
				error("the lower bound is of the wrong type", lowerBound.getPosition());
			
		} else {
			
			Type baseType;
			
			if(lowerT instanceof Type.ReferenceType){
				
				baseType = ((Type.ReferenceType)lowerT).getBaseType();
				
			} else if( lowerT instanceof Type.SubrangeType){
				
				baseType = ((Type.SubrangeType)lowerT).getBaseType();
				
			} else 
				
				baseType = lowerT;
			
			baseType.coerceExp(upperBound);
			
			symtab.extendCurrentScope();
			
			//System.out.println(node.getId());
			
			
			SymEntry.VarEntry controlVar = symtab.addVariable(node.getId(), node.getPosition(), new ReferenceType(baseType));
			
			controlVar.setControlVar(true);
			
			node.getdoStmt().accept(this);
			
			symtab.leaveExtendedScope();
			
			
			
			
		}

		


	}

	public void visitStatementListNode( StatementNode.ListNode node ) {
		for( StatementNode s : node.getStatements() ) {
			s.accept( this );
		}
	}
	private ExpNode checkCondition( ExpNode cond ) {
		// Check and transform the condition
		cond = cond.transform( this );
		/* Validate that the condition is boolean, which may require
		 * coercing the condition to be of type boolean. */     
		return Type.BOOLEAN_TYPE.coerceExp( cond );
	}
	public void visitIfNode(StatementNode.IfNode node) {
		// Check the condition.
		node.setCondition( checkCondition( node.getCondition() ) );
		// Check the 'then' part.
		node.getThenStmt().accept( this );
		// Check the 'else' part.
		node.getElseStmt().accept( this );
	}

	public void visitWhileNode(StatementNode.WhileNode node) {
		// Check the condition.
		node.setCondition( checkCondition( node.getCondition() ) );
		// Check the body of the loop.
		node.getLoopStmt().accept( this );
	}
	/*************************************************
	 *  Expression node static checker visit methods
	 *************************************************/
	public ExpNode visitErrorExpNode(ExpNode.ErrorNode node) {
		// Nothing to do - already invalid.
		return node;
	}

	public ExpNode visitConstNode(ExpNode.ConstNode node) {
		// type already set up
		return node;
	}

	public ExpNode visitReadNode(ExpNode.ReadNode node) {
		node.setType( Type.INTEGER_TYPE );
		return node;
	}

	public ExpNode visitBinaryOpNode( ExpNode.BinaryOpNode node ) {
		/* Check arguments and determine their types */
		ExpNode left = node.getLeft().transform( this );
		node.setLeft( left );
		ExpNode right = node.getRight().transform( this );
		node.setRight( right );
		BinaryOperator op = node.getOp();
		SymEntry.OperatorEntry opEntry = symtab.lookupOperator( op.getName() );
		Type opType = opEntry.getType();
		/* If the binary operator is overloaded it will have an intersection type,
		 * i.e., multiple possible types, otherwise it will have a function
		 * type, with its argument type being a product of two types.
		 */
		if( opType instanceof Type.FunctionType ) {
			/* Just one type for this operator. Coerce each operand
			 * to the argument type of the operator and report any
			 * type mismatch.
			 */
			Type.FunctionType fType = (Type.FunctionType)opType;
			Type.ProductType opArgType = (Type.ProductType)fType.getArgType();
			node.setLeft( opArgType.getLeftType().coerceExp(left ) );
			node.setRight( opArgType.getRightType().coerceExp(right ) );
			node.setType( fType.getResultType() );
		} else if( opType instanceof Type.IntersectionType ) {
			for( Type t : ((Type.IntersectionType)opType).getTypes() ) {
				Type.FunctionType fType = (Type.FunctionType)t;
				Type.ProductType opArgTypes = 
						(Type.ProductType)fType.getArgType();
				try {
					/* Coerce both arguments to the argument type for 
					 * this operator type. If either coercion fails an
					 * exception will be trapped and an alternative 
					 * function type within the intersection tried.
					 */
					ExpNode newLeft = 
							opArgTypes.getLeftType().coerceToType( left );
					ExpNode newRight = 
							opArgTypes.getRightType().coerceToType( right );
					/* Both coercions succeeded if we get here */
					node.setLeft( newLeft );
					node.setRight( newRight );
					node.setType( fType.getResultType() );
					return node;
				} catch ( IncompatibleTypes ex ) {
					// Allow "for" loop to try an alternative
				}
			}
			// no match in intersection
			error( "Type of arguments (" + left.getType() + "," + 
					right.getType() + ")" + " do not match " + opType, 
					node.getPosition() );
			node.setType( Type.ERROR_TYPE );
		} else {
			fatal( "Invalid operator type", node.getPosition() );
		}
		return node;
	}
	public ExpNode visitUnaryOpNode( ExpNode.UnaryOpNode node ) {
		/* Unary operators aren't overloaded */
		ExpNode subExp = node.getSubExp().transform( this );//transforms the subexp, for eg identifier into constant
		Type.FunctionType fType = 
				(Type.FunctionType)node.getOp().getType(); //checks the type of the operator 
		node.setSubExp( fType.getArgType().coerceExp( subExp ) );//coerces subexp to operator, for eg subrange to int
		node.setType( fType.getResultType() );//type of the result of unary operator
		return node;
	}
	public ExpNode visitArgumentsNode( ExpNode.ArgumentsNode node ) {
		List<ExpNode> newExps = new LinkedList<ExpNode>();
		List<Type> types = new LinkedList<Type>();
		for( ExpNode exp : node.getArgs() ) {
			newExps.add( exp.transform( this ) );
			types.add( exp.getType() );
		}
		node.setArgs( newExps );
		node.setType( new Type.ProductType( types ) );
		return node;
	}
	public ExpNode visitDereferenceNode(ExpNode.DereferenceNode node) {
		// Check the left value referred to by this right value.
		ExpNode lVal = node.getLeftValue().transform( this );
		node.setLeftValue( lVal );
		/* The type of the right value is the base type of the left value. */
		Type lValueType = lVal.getType();
		if( lValueType instanceof Type.ReferenceType ) {
			node.setType( ((Type.ReferenceType)lValueType).getBaseType() );
		} else if( lValueType != Type.ERROR_TYPE ) {
			error( "cannot dereference an expression which isn't a reference",
					node.getPosition() );
		}
		return node;
	}
	/** When parsing an identifier within an expression one can't tell
	 * whether it has been declared as a constant or an identifier.
	 * Here we check which it is and return either a constant or 
	 * a variable node.
	 */
	public ExpNode visitIdentifierNode(ExpNode.IdentifierNode node) {
		// First we look up the identifier in the symbol table.
		ExpNode newNode;
		SymEntry entry = symtab.lookup( node.getId() );
		if( entry instanceof SymEntry.ConstantEntry ) {
			// Set up a new node which is a constant.
			SymEntry.ConstantEntry constEntry = 
					(SymEntry.ConstantEntry)entry;
			newNode = new ExpNode.ConstNode( node.getPosition(), 
					constEntry.getType(), constEntry.getValue() );
		} else if( entry instanceof SymEntry.VarEntry ) {
			// Set up a new node which is a variable.
			SymEntry.VarEntry varEntry = (SymEntry.VarEntry)entry;
			newNode = new ExpNode.VariableNode(node.getPosition(), varEntry);
		} else {
			// Undefined identifier (or type or procedure identifier).
			// Set up new node to be an error node.
			newNode = new ExpNode.ErrorNode( node.getPosition() );
			error("Constant or variable identifier required", node.getPosition() );
		}
		// Check the created true node.
		newNode = newNode.transform( this );
		return newNode;
	}

	public ExpNode visitVariableNode(ExpNode.VariableNode node) {
		// Set the type of the node.
		node.setType( node.getVariable().getType() );
		return node;
	}
	public ExpNode visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node) {
		// Nothing to do.
		return node;
	}

	public ExpNode visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node) {
		// Nothing to do.
		return node;
	}


	/** Report a (semantic) error. */
	private void error(String message, Position pos) {
		errors.errorMessage( message, Severity.ERROR, pos );
	}
	/** Report a fatal error in static checker. */
	private void fatal(String message, Position pos) {
		errors.errorMessage( message, Severity.FATAL, pos );
	}
	@Override
	public void visitSkipNode(StatementNode.SkipNode node) {
		// TODO Auto-generated method stub
		//do we add accept, as it doesnt have descendents

	}
	
}
