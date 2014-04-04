package syms;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedMap;
import java.util.TreeMap;

import machine.StackMachine;

import source.ErrorHandler;
import source.Position;
import source.Severity;
import syms.SymEntry.ConstantEntry;
import tree.ConstExp;
import tree.ExpNode;

/** This class provides the type structures defining the types
 * available in the language.
 * @version $Revision: 17 $  $Date: 2013-05-13 08:25:39 +1000 (Mon, 13 May 2013) $
 * It provides subclasses for each of the different kinds of "type",
 * e.g., scalar types, subranges, products of types, function types,
 * intersections of types, reference types, and the procedure type.
 * IdRefType is provided to allow type names to be used as types.
 * 
 * As well as the constructors for the types it provides a number of
 * access methods.
 * Each type provides a method for coercing an expression to the type.
 * Type also provides structures for the predefined types and 
 * the special type ERROR_TYPE, which is used for handling type errors.
 */
public abstract class Type 
{
    /** The coercion procedures will throw an IncompatibleTypes exception
     * if the expression being coerced can't be coerced to the given type.
     */
    public static class IncompatibleTypes extends Exception {
        Position pos;
        
        /** Constructor.
         * @param msg error message to be reported
         * @param pos position of the expression for error reporting
         */
        public IncompatibleTypes( String msg, Position pos ) {
            super( msg );
            this.pos = pos;
        }
        public Position getPosition() {
            return pos;
        }
    }
    /** All types require space to be allocated (may be 0) */
    protected int space;
    /** Track whether type has been resolved */
    protected boolean resolved;
    /** Only subclasses provide public constructors. */
    private Type( int n ) {
        space = n;
        resolved = false;
    }
    private Type( int n, boolean resolved ) {
        space = n;
        this.resolved = resolved;
    }
    /** return the space required for an element of the type */
    public int getSpace() {
        assert resolved;
        return space;
    }
    /** Resolve identifier references anywhere within type 
     * default just sets resolved true; it needs to be overridden 
     * when appropriate.
     * @param pos - position for error messages (in overriding methods) 
     */
    public Type resolveType( Position pos ) {
        resolved = true;
        return this;
    }
    /** Coerce an expression to this type and report error if incompatible
     * @param exp is the expression to be coerced
     * @returns the coerced expression or ErrorNode on failure
     */
    public ExpNode coerceExp( ExpNode exp ) {
        /** Try coercing the expression. */
        try {
            return this.coerceToType( exp );
        } catch( IncompatibleTypes e ) {
            /** At this point the coercion has failed. */
                ErrorHandler.getErrorHandler().errorMessage( e.getMessage(),
                    Severity.ERROR, e.getPosition() );
            return new ExpNode.ErrorNode( e.getPosition() ); 
        }
    }
     /** Coerce exp to this type or throw IncompatibleTypes exception if can't
     * @param exp expression to be coerced
     * @return coerced expression
     * @throws IncompatibleTypes if cannot coerce
     */
    public ExpNode coerceToType( ExpNode exp ) 
            throws IncompatibleTypes {
        /** Unless this type is a reference type, optionally dereference 
         * the expression to get its base type.
         */
        ExpNode newExp = exp;
        if( !(this instanceof ReferenceType) ) {
            newExp = optDereference( newExp );
        }
        /** If the type of the expression is this type or
         * of ERROR_TYPE, we are done.
         */
        Type fromType = newExp.getType();
        if( fromType == this || fromType == Type.ERROR_TYPE ) {
            return newExp;
        }
        /** Try coercing the expression. Dynamic dispatch on the desired
         * type is used to control the coercion process.
         */
        return this.coerce( newExp );
    }    
    /** Coerce an expression node passed as a parameter to be of this type.
     * This default version is just checking they are the same type. 
     * Subclasses of Type override this method.
     * @param exp expression to be coerced
     * @return resulting coerced expression node. 
     * @throws IncompatibleTypes exception if it can't coerce.
     */
    protected ExpNode coerce( ExpNode exp ) throws IncompatibleTypes {
        Type fromType = exp.getType();
        if( this.equals( fromType ) ) {
            return exp;
        }
        throw new IncompatibleTypes( 
                "cannot treat " + fromType + " as " + this,
                exp.getPosition() );
    }
    /** Type equality. Overridden for most subclasses.
     * @param other - type to be compared with this. */
    public boolean equals( Type other ) {
        return this == other;
    }
    @Override
    public String toString() {
        return "size " + space;
    }
    /** If something is of type ErrorType an error message for it will already
     * have been issued and hence to avoid generating spurious error messages 
     * ERROR_TYPE is compatible with everything.
     */
    public static final Type ERROR_TYPE = new Type( 0, true ) {
        
        @Override
        protected ExpNode coerce( ExpNode exp ) {
            return exp;
        }
        @Override
        public String toString() { 
            return "ERROR_TYPE";
        }
    };  
    /** Predefined integer type. */
    public static final ScalarType INTEGER_TYPE = 
        new ScalarType( StackMachine.SIZE_OF_INT, 
                Integer.MIN_VALUE, Integer.MAX_VALUE ) {
            @Override
            public String toString() { 
                return "INT"; 
            }
        };
    /** Predefined boolean type. */
    public static final ScalarType BOOLEAN_TYPE = 
        new ScalarType( StackMachine.SIZE_OF_BOOLEAN, 
                StackMachine.FALSE_VALUE, StackMachine.TRUE_VALUE ) {
            @Override
            public String toString() { 
                return "BOOLEAN"; 
            }
        };

    public static final ProductType PAIR_INTEGER_TYPE =
        new ProductType( INTEGER_TYPE, INTEGER_TYPE );
                
    public static final ProductType PAIR_BOOLEAN_TYPE =
        new ProductType( BOOLEAN_TYPE, BOOLEAN_TYPE );
                
    public static final FunctionType ARITHMETIC_BINARY =
        new FunctionType( PAIR_INTEGER_TYPE, INTEGER_TYPE );
    
    public static final FunctionType INT_RELATIONAL_TYPE =
        new FunctionType( PAIR_INTEGER_TYPE, BOOLEAN_TYPE );
            
    public static final FunctionType BOOL_RELATIONAL_TYPE =
        new FunctionType( PAIR_BOOLEAN_TYPE, BOOLEAN_TYPE );
    
    public static final FunctionType ARITH_UNARY =
        new FunctionType( INTEGER_TYPE, INTEGER_TYPE );
            
    public static final FunctionType LOGICAL_UNARY =
        new FunctionType( BOOLEAN_TYPE, BOOLEAN_TYPE );
            

    /** Scalar types are simple unstructured types that just have a range of
     * possible values. int and boolean are scalar types */
    public static class ScalarType extends Type {
        /** lower and upper bounds of scalar type */
        protected int lower, upper;
 
        public ScalarType( int size, int lower, int upper ) {
            super( size, true );
            this.lower = lower;
            this.upper = upper;
        }
        /** Constructor when bounds evaluated later */
        public ScalarType( int size ) {
            super( size );
        }
        /** The least element of the type */
        public int getLower() {
            return lower;
        }
        protected void setLower( int lower ) {
            this.lower = lower;
        }
        /** The greatest element of the type */
        public int getUpper() {
            return upper;
        }
        protected void setUpper( int upper ) {
            this.upper = upper;
        }
        /** Coerce expression to this Scalar type.
         * The objective is to create an expression of the this scalar type
         * from exp.
         * @param exp expression to be coerced
         * @throws IncompatibleTypes exception if it is not possible to coerce 
         *         exp to this scalar type
         */
        @Override
        protected ExpNode coerce( ExpNode exp ) throws IncompatibleTypes {
            Type fromType = exp.getType();
            if( fromType instanceof SubrangeType ) {
                /** This code implements Rule Widen subrange. 
                 * If the types don't match, the only other possible type
                 * for the expression which can be coerced to this scalar type 
                 * is a subrange type, provided its base type matches
                 * this type. If that is the case we insert a WidenSubrangeNode
                 * of this type and with the expression as a subtree.
                 */
                Type baseType = ((SubrangeType)fromType).getBaseType();
                if( this.equals( baseType ) ) {
                    return new ExpNode.WidenSubrangeNode( exp.getPosition(), 
                            this, exp );
                }
            } 
            /** Otherwise we report the failure to coerce the expression via
             * an IncompatibleTypes exception.
             */
            throw new IncompatibleTypes( "can't coerce " + exp.getType() + 
                    " to " + this, exp.getPosition() );
        }
    }
    /** If SubrangeType then cast to SubrangeType and return
     * else return null */
    public SubrangeType getSubrangeType() {
        if( this instanceof SubrangeType ) {
            return (SubrangeType)this;
        }
        return null;
    }
    /** If this is a subrange type widen it to its base type. */
    public Type optWidenSubrange() {
        if( this instanceof SubrangeType ) {
            return ((SubrangeType)this).getBaseType();
        }
        return this;
    }
    
    /** Types defined as a subrange of a scalar type. */
    public static class SubrangeType extends ScalarType {
        /** The base type of the subrange type */
        private Type baseType;
        /** Constant expression trees for lower and upper bounds 
         * before evaluation */
        private ConstExp lowerExp, upperExp;

        public SubrangeType( ConstExp lowerExp, ConstExp upperExp ) {
            /** On a byte addressed machine, the size could be scaled to
             * just fit the subrange, e.g., a subrange of 0..255
             * might only require 1 byte.
             */
            super( StackMachine.SIZE_OF_INT );
            this.lowerExp = lowerExp;
            this.upperExp = upperExp;
        }
        public Type getBaseType() {
            return baseType;
        }
        /** Coerce expression to this subrange type
         * The objective is to create an expression of the this subrange type
         * from exp.
         * @param exp expression to be coerced
         * @throws IncompatibleTypes exception if it is not possible to coerce 
         *         exp to this subrange type
         */
        @Override
        protected ExpNode coerce( ExpNode exp ) throws IncompatibleTypes {
            /** This implements Rule Narrow subrange in the static semantics. 
             * If the types don't match, we can try coercing the expression
             * to the base type of this subrange, and then narrow that
             * to this type. If the coercion to the base type fails it will
             * generate an exception, which is allowed to pass up to the caller.
             */
            ExpNode coerceExp = getBaseType().coerceToType( exp );
            /** If we get here, coerceExp is of the same type as the base 
             * type of this subrange type. We just need to narrow it
             * down to this subrange. 
             */
            return new ExpNode.NarrowSubrangeNode( coerceExp.getPosition(), 
                        this, coerceExp );
        }
        /* Resolving a subrange type requires the upper and lower bound 
         * expressions to be evaluated.
         */
        @Override
        public Type resolveType( Position pos ) {
            if( !resolved ) {
                lower = lowerExp.getValue();
                upper = upperExp.getValue();
                if( upper < lower ) {
                    error( "Upper bound of subrange less than lower bound", pos );
                }
                baseType = upperExp.getType();
                if( !upperExp.getType().equals(lowerExp.getType())) {
                    error( "Types of bounds of subrange should match", pos );
                    baseType = Type.ERROR_TYPE;
                }
                resolved = true;
            }
            return this;
        }
        /** A subrange type is equal to another subrange type only if they have
         * the same upper and lower bounds.
         */
        @Override
        public boolean equals( Type other ) {
            if( other instanceof SubrangeType ) {
                SubrangeType otherSubrange = (SubrangeType)other;
                return baseType.equals( otherSubrange.getBaseType() ) &&
                        lower == otherSubrange.getLower() &&
                        upper == otherSubrange.getUpper();
            } else {
                return false;
            }
        }
        @Override
        public String toString() {
            return "subrange(" + baseType.toString() + "," +
                lower + "," + upper + ")";
        }   
    }

    /** Product types represent the product of a sequence of types */
    public static class ProductType extends Type {
        private List<Type> types;
        
        private ProductType() {
            super( 0 );
            types = new LinkedList<Type>();
        }
        /** Constructor allowing individual types to be specified */
        public ProductType( Type... typeList ) {
            this();
            for(Type t : typeList ) {
                types.add( t );
            }
        }
        /** Constructor when list of types available */
        public ProductType( List<Type> types ) {
            this();
            this.types = types;
        }
        /** The space required for a product of types is the sum of
         * the spaces required for each type in the product.
         */
        private int calcSpace( List<Type> types ) {
            int space = 0;
            for( Type t : types ) {
                space += t.getSpace();
            }
            return space;
        }
        public List<Type> getTypes() {
            return types;
        }
        /** For a product of two types return first (left) */
        public Type getLeftType() {
            assert types.size() == 2;
            return types.get(0);
        }
        /** For a product of two types return second (right) */
        public Type getRightType() {
            assert types.size() == 2;
            return types.get(1);
        }
        /** Resolve identifier references anywhere within type */
        @Override
        public ProductType resolveType( Position pos ) {
            if( ! resolved ) {
                /* Build a list of resolved types */
                List<Type> resolvedTypes = new LinkedList<Type>();
                for( Type t : types ) {
                    resolvedTypes.add( t.resolveType( pos ) );
                }
                types = resolvedTypes;
                space = calcSpace( types );
                resolved = true;
            }
            return this;
        }
        /** Two product types are equal only if each element of the list
         * of types for one is equal to the corresponding element of the
         * list of types for the other.
         */
        @Override
        public boolean equals( Type other ) {
            if( other instanceof ProductType ) {
                List<Type> otherTypes = ((ProductType) other).getTypes();
                if( types.size() == otherTypes.size() ) {
                    Iterator<Type> iterateOther = otherTypes.iterator();
                    for( Type t : types ) {
                        Type otherType = iterateOther.next();
                        if( ! t.equals( otherType ) ) {
                            return false;
                        }
                    }
                    /* If we reach here then every type in the product has
                     * matched the corresponding type in the other product
                     */
                    return true;
                }
            }
            return false;
        }
        /** Coerce expression to this product type.
         * @param exp should be an ArgumentsNode with a list of 
         *     expressions of the same length as this product type 
         * @throws IncompatibleTypes exception if it is not possible to coerce 
         *         exp to this product type
         */
        @Override
        protected ExpNode.ArgumentsNode coerce( ExpNode exp ) 
                throws IncompatibleTypes {
            /** If exp is not an ArgumentsNode consisting of a list of expressions
             * of the same length as the product type toProductType, then we can't
             * coerce exp to toProductType and we raise an exception.
             */
            if( exp instanceof ExpNode.ArgumentsNode) {
                ExpNode.ArgumentsNode args = (ExpNode.ArgumentsNode)exp;
                if( this.getTypes().size() == args.getArgs().size() ) {
                    /** If exp is an ArgumentNode of the same size as toProductType,
                     * we coerce each expression in the list of arguments, to the
                     * corresponding type in the product, accumulating a new
                     * (coerced) list of expressions as we go.
                     * If any of the argument expressions can't be coerced,
                     * an exception will be raised, which we allow the
                     * caller to handle because the failure to coerce any
                     * expression in the list of arguments, corresponds to a 
                     * failure to coerce the whole arguments node.
                     */
                    ListIterator<ExpNode> iterateArgs = 
                        args.getArgs().listIterator();
                    List<ExpNode> newArgs = new LinkedList<ExpNode>();
                    for( Type t : this.getTypes() ) {
                        ExpNode subExp = iterateArgs.next();
                        /** Type incompatibilities detected in the
                         * coercion will generate an exception,
                         * which we allow to pass back up to the next level
                         */
                        ExpNode coerced = t.coerce( subExp );
                        newArgs.add( coerced );
                    }
                    /** If we get here, all expressions in the list have been
                     * successfully coerced to the corresponding type in the 
                     * product, and the coerced list of expressions newArgs will
                     * be of type toProductType. We return an ArgumentsNode of 
                     * type toProductType, with newArgs as its list of expressions.
                     */
                    ExpNode.ArgumentsNode result = new ExpNode.ArgumentsNode( 
                            args.getPosition(), this, newArgs );
                    return result;
                } else {
                    throw new IncompatibleTypes( 
                        "length mismatch in coercion to ProductType", 
                        exp.getPosition() );
                }
            } else {
                throw new IncompatibleTypes( 
                    "Arguments node expected for coercion to ProductType",
                    exp.getPosition() );
            }
        }
        @Override
        public String toString() {
            String result = "(";
            String sep = "";
            for( Type t: types ) {
                result += sep + t;
                sep = "*";
            }
            return result + ")";
        }
    }
    /** Function types represent a function from an argument type
     * to a result type.
     */
    public static class FunctionType extends Type {
        private Type argType;
        private Type resultType;
        
        public FunctionType( Type arg, Type result ) {
            super( 0 );
            this.argType = arg;
            this.resultType = result;
        }
        public Type getArgType() {
            return argType;
        }
        public Type getResultType() {
            return resultType;
        }
        /** Resolve identifier references anywhere within type */
        @Override
        public FunctionType resolveType( Position pos ) {
            if( ! resolved ) {
                argType = argType.resolveType( pos );
                resultType = resultType.resolveType( pos );
                resolved = true;
            }
            return this;
        }
        /** Two function types are equal only if their argument and result
         * types are equal.
         */
        @Override
        public boolean equals( Type other ) {
            if( other instanceof FunctionType ) {
                FunctionType otherFunction = (FunctionType)other;
                return getArgType().equals(otherFunction.getArgType()) &&
                    getResultType().equals(otherFunction.getResultType());
            }
            return false;
        }
        @Override
        public String toString() {
            return "(" + argType + "->" + resultType + ")";
        }
    }
    /** Intersection types represent the intersection of a set of types.
     * They can be used as the types of overloaded operators. 
     * For example "=" has two types to allow two integers to be compared
     * and two booleans to be compared. */
    public static class IntersectionType extends Type {
        private List<Type> types;
        
        /** @param typeArray - list of types in the intersection
         * @requires the types in typeArray are distinct */
        public IntersectionType( Type... typeArray ) {
            super( 0 );
            types = new LinkedList<Type>();
            for( Type t : typeArray ) {
                addType( t );
            }
        }
        /** Add a type to the list of types, but if it is a IntersectionType
         * flatten it and add each type in the intersection. 
         */
        public void addType( Type t ) {
            if( t instanceof IntersectionType ) {
                types.addAll( ((IntersectionType)t).getTypes() );
            } else {
                types.add( t );
            }
        }
        public List<Type> getTypes() {
            return types;
        }
        /** Resolve identifier references anywhere within type */
        @Override
        public IntersectionType resolveType( Position pos ) {
            if( !resolved ) {
                /* Build a list of resolved types */
                List<Type> resolvedTypes = new LinkedList<Type>();
                for( Type t : types ) {
                    resolvedTypes.add( t.resolveType( pos ) );
                }
                types = resolvedTypes;
                resolved = true;
            }
            return this;
        }
        /* Two intersection types are equal if they contain the same sets of types.
         * @param other - type to be compared with this
         * @requires the lists in each intersection type have distinct elements 
         */
        @Override
        public boolean equals( Type other ) {
            if( other instanceof IntersectionType ) {
                List<Type> otherTypes = ((IntersectionType) other).getTypes();
                if( types.size() == otherTypes.size() ) {
                    for( Type t : types ) {
                        if( ! otherTypes.contains( t ) ) {
                            return false;
                        }
                    }
                    /** If we reach here then all types in this intersection
                     * are also contained in the other intersection, and hence
                     * the two intersections are equivalent.
                     */
                    return true;
                }
            }
            return false;
        }
        /** An ExpNode can be coerced to a IntersectionType if it can be coerced
         * to one of the types of the intersection.
         * @throws IncompatibleTypes exception if it is not possible to coerce 
         *         exp to any type within the intersection
         */
        @Override
        protected ExpNode coerce( ExpNode exp ) throws IncompatibleTypes {
            /** We iterate through all the types in the intersection, trying 
             * to coerce the exp to each, until one succeeds and we return
             * that coerced expression. If a coercion to a type in the intersection 
             * fails it will throw an exception, which is caught. Once caught 
             * we ignore the exception, and allow the for loop to try the next
             * type in the intersection.
             */
            for( Type toType : this.getTypes() ) {
                try {
                    return toType.coerce( exp );
                } catch( IncompatibleTypes ex ) {
                    // allow "for" loop to try the next alternative 
                }
            }
            /** If we get here, we were unable to to coerce exp to any one of 
             * the types in the intersection, and hence we can't coerce exp the the 
             * intersection type.
             */
            throw new IncompatibleTypes( "none of types match",
                    exp.getPosition() );
        }
        @Override
        public String toString() {
            String s = "(";
            String sep = "";
            for( Type t : types ) {
                s += sep + t;
                sep = " | ";
            }
            return s + ")";
        }
    }
    /** Type for a procedure. */
    public static class ProcedureType extends Type {
        
        public ProcedureType() {
            super(2); // allow for procedures as parameters
        }
        @Override
        public ProcedureType resolveType( Position pos ) {
            return this;
        }
        @Override
        public String toString() {
            String s = "PROCEDURE ("; 
            return s;
        }
    }

    /** If this type is an IdRefType then cast to IdRefType and return
     * else return null */
    public IdRefType getIdRefType() {
        if( this instanceof IdRefType ) {
            return (IdRefType)this;
        }
        return null;
    }
    /** Type for a type identifier. Used until the type identifier can
     * be resolved.
     */
    public static class IdRefType extends Type {
        /** Name of the referenced type */
        private String name;
        /** Symbol table scope at the point of definition of the type
         * reference. Used when resolving the reference. */
        private Scope scope;
        /** Position of use of type identifier */
        Position pos;
        /** Resolved real type, or ERROR_TYPE if can't be resolved. */
        private Type realType;
        /** Status of resolution of reference. */
        private enum Status{ Unresolved, Resolving, Resolved }
        private Status status;
        
        public IdRefType( String name, Scope scope, Position pos ) {
            super( 0 );
            this.name = name;
            this.scope = scope;
            this.pos = pos;
            this.status = Status.Unresolved;
        }
        public String getName() {
            return name;
        }
        /** Resolve the type identifier and return the real type. */
        @Override
        public Type resolveType( Position pos ) {
            // System.out.println( "Resolving " + name );
            switch( status ) {
            case Unresolved:
                status = Status.Resolving;
                realType = Type.ERROR_TYPE;
                SymEntry entry = scope.lookup( name );
                if( entry != null && entry instanceof SymEntry.TypeEntry ) {
                    /* resolve identifiers in the referenced type */
                    entry.resolve();
                    /* if status of this entry has resolved then there was a
                     * circular reference and we leave the realType as 
                     * ERROR_TYPE to avoid other parts of the compiler getting
                     * into infinite loops chasing types.
                     */
                    if( status == Status.Resolving ) {
                        realType = entry.getType();
                    }
                    assert realType != null;
                } else {
                    error( "Undefined type: " + name, pos );
                }
                status = Status.Resolved;
                break;
            case Resolving:
                error( name + " is circularly defined", pos );
                /* Will resolve to ERROR_TYPE */
                status = Status.Resolved;
                break;
            case Resolved:
                /* Already resolved */
                break;
            }
            return realType;
        }
        @Override
        public String toString() {
            return name;
        }
    }
    /** AddressType is the common part of ReferenceType (and PointerType) */ 
    public static class AddressType extends Type {
        protected Type baseType;
        
        public AddressType( Type baseType ) {
            super( StackMachine.SIZE_OF_ADDRESS );
            this.baseType = baseType;
        }
        public Type getBaseType() {
            return baseType;
        }
        @Override
        public AddressType resolveType( Position pos ) {
            if( !resolved ) {
                baseType = baseType.resolveType( pos );
                resolved = true;
            }
            return this;
        }
        @Override
        public String toString() {
            return "address(" + baseType + ")";
        }
    }
    /** If this type is a reference type return its base type
     * otherwise just return this.
     */
    public Type optDereference() {
        if( this instanceof ReferenceType ) {
            return ((ReferenceType)this).getBaseType();
        }
        return this;
    }
    /** This method implements Rule Dereference in the static semantics if
     * applicable, otherwise it leaves the expression unchanged. 
     * Optionally dereference a Reference type expression to get its base type
     * If exp is type ReferenceType(T) for some base type T,
     * a new DereferenceNode of type T is created with exp as a subtree
     * and returned, otherwise exp is returned unchanged.
     */
    public static ExpNode optDereference( ExpNode exp ) {
    	
        Type fromType = exp.getType();
        if( fromType instanceof ReferenceType ) {
            return new ExpNode.DereferenceNode( 
            		
                    ((ReferenceType)fromType).getBaseType(), exp );
        } else {
            return exp;
        }
    }
    /** Type used for variables in order to distinguish a variable
     * of type ref(int), say, from its value which is of type int.
     */
    public static class ReferenceType extends AddressType {
        
        public ReferenceType( Type baseType ) {
            super( baseType );
        }
        /** Two reference types are equal only if their base types are equal */
        @Override
        public boolean equals( Type other ) {
            return other instanceof ReferenceType &&
                ((ReferenceType)other).getBaseType().equals(
                        this.getBaseType() );
        }
        @Override
        public String toString() {
            return "ref(" + baseType + ")";
        }
    }
    
    public static void error( String message, Position pos ) {
        ErrorHandler.getErrorHandler().errorMessage( message, 
                Severity.ERROR, pos);
    }
    public static void fatal( String message, Position pos ) {
        ErrorHandler.getErrorHandler().errorMessage( message, 
                Severity.FATAL, pos);
    }
}
