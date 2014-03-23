package tree;

/**
 * enumeration BinaryOperator - Binary operators in abstract syntax tree.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */
public enum BinaryOperator {
    ADD_OP( "+" ),
    SUB_OP( "-" ),
    MUL_OP( "*" ),
    DIV_OP( "/" ),
    EQUALS_OP( "=" ),
    NEQUALS_OP( "!=" ),
    GREATER_OP( ">" ),
    LESS_OP( "<" ),
    LEQUALS_OP( "<=" ),
    GEQUALS_OP( ">=" ),

    INVALID_OP( "INVALID" );
    
    /** The name of the binary operator */
    String name;
    
    private BinaryOperator( String name ) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
