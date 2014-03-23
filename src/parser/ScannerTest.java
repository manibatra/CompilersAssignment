package parser;

import junit.framework.TestCase;

/**
 * class ScannerTest - JUnit test code for scanner
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */ 
public class ScannerTest extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSymbols() throws Exception {
        Token expectedSyms[] = {
            Token.PLUS, Token.MINUS, Token.TIMES, Token.DIVIDE, 
            Token.LPAREN, Token.RPAREN, Token.SEMICOLON, Token.COLON,
            Token.ASSIGN, Token.COMMA, Token.RANGE, 
            Token.EQUALS, Token.NEQUALS, Token.LEQUALS,
            Token.LESS, Token.GEQUALS, Token.GREATER,
            Token.LOG_AND, Token.LOG_OR, Token.LOG_NOT,
            Token.ILLEGAL };
        
        LexicalToken next;
        int pos = 0;
        Scanner scanner = new Scanner( "test-pgm/scannerTestSymbols.pl0" );
        for( Token expected : expectedSyms ) {
            next = scanner.getNextToken();
            assertEquals( expected, next.getKind() );
            assertEquals( pos, next.getPosn().getIndex() );
            pos += expected.toString().length();
        }
    }
        
    public void testKeywords() throws Exception {
        Token expectedKeywords[] = {
            Token.KW_BEGIN, Token.KW_CALL, Token.KW_CONST,
            Token.KW_DO, Token.KW_ELSE, Token.KW_END,
            Token.KW_IF, Token.KW_PROCEDURE, Token.KW_READ,
            Token.KW_THEN, Token.KW_TYPE, 
            Token.KW_VAR, Token.KW_WHILE,
            
            Token.KW_WRITE
        };
        
        LexicalToken next;
        int pos = 0;
        Scanner scanner = new Scanner( "test-pgm/scannerTestKeywords.pl0" );
        for( Token expected : expectedKeywords ) {
            next = scanner.getNextToken();
            assertEquals( expected, next.getKind() );
            assertEquals( pos, next.getPosn().getIndex() );
            pos += expected.toString().length() + 1;
        }
    }
        
    public void testIdentifiers() throws Exception {
        String expectedIdentifiers[] = {
            "x", "y", "xy", "xx", "a0", "x9", "a00", "x99", 
            "abcdefghijklmnopqrstuvwxyz0123456789"
        };
        
        LexicalToken next;
        int pos = 0;
        Scanner scanner = new Scanner( "test-pgm/scannerTestIdentifiers.pl0" );
        for( String expected : expectedIdentifiers ) {
            next = scanner.getNextToken();
            assertEquals( Token.IDENTIFIER, next.getKind() );
            assertEquals( expected, next.getName() );
            assertEquals( pos, next.getPosn().getIndex() );
            pos += expected.toString().length() + 1;
        }
    }
    public void testNumbers() throws Exception {
        int expectedNumbers[] = {
            0, 1, 00001, 123456789, 2147483647
        };
        int length[] = {
            1, 1, 5,     9,         10
        };
        
        LexicalToken next;
        int pos = 0;
        Scanner scanner = new Scanner( "test-pgm/scannerTestNumbers.pl0" );
        int n = 0;
        for( int expected : expectedNumbers ) {
            next = scanner.getNextToken();
            assertEquals( Token.NUMBER, next.getKind() );
            assertEquals( expected, next.getIntValue() );
            assertEquals( pos, next.getPosn().getIndex() );
            pos += length[n] + 1;
            n++;
        }
    }
}
