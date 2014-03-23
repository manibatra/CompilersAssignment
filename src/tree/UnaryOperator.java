package tree;
import syms.Type;

/**
 * enumeration UnaryOperator - Unary operators in abstract syntax tree.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */

public enum UnaryOperator {
    NEG_OP( "-", Type.ARITH_UNARY );

    /** The name of the unary operator. */
    String name;
    Type type;

    private UnaryOperator( String name, Type type ) {
        this.name = name;
        this.type = type;
    }
    public Type getType() {
        return type;
    }
    @Override
    public String toString() {
        return name;
    }
}
