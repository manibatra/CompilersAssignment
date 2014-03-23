package parser;

/** enumeration Token - Defines the basic tokens returned from the lexical analyzer.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */
public enum Token {
    EOF( "End-of-file"),
    PLUS( "+" ),
    MINUS( "-" ),
    TIMES( "*"),
    DIVIDE( "/" ),
    LPAREN( "(" ),
    RPAREN( ")" ),
    LBRACKET( "[" ),
    RBRACKET( "]" ),
    SEMICOLON( ";" ),
    COLON( ":" ),
    ASSIGN( ":=" ),
    COMMA( "," ),
    RANGE( ".." ),
    EQUALS( "=" ),
    NEQUALS( "!=" ),
    LEQUALS( "<=" ),
    LESS( "<" ),
    GEQUALS( ">=" ),
    GREATER( ">" ),
    LOG_AND( "&&" ),
    LOG_OR( "||" ),
    LOG_NOT( "!" ),
    KW_BEGIN( "begin" ),
    KW_CALL( "call" ),
    KW_CONST( "const" ),
    KW_DO( "do" ),
    KW_ELSE( "else" ),
    KW_END( "end" ),
    KW_FOR( "for" ),
    KW_IF( "if" ),
    KW_PROCEDURE( "procedure" ),
    KW_READ( "read" ),
    KW_SKIP( "skip" ),
    KW_THEN( "then" ),
    KW_TYPE( "type" ),
    KW_VAR( "var" ),
    KW_WHILE( "while" ),
    KW_WRITE( "write" ),
    IDENTIFIER( "identifier" ),
    NUMBER( "number" ),
    ILLEGAL( "illegal" );
    
    /** The name of the token */
    String name;
    
    private Token( String name ) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
