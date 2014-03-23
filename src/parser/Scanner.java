package parser;

import source.ErrorHandler;
import source.Severity;
import source.Source;
import source.Position;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * class Scanner - hand coded lexical analyzer for PL0
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 * Tokenizes the requested input file or standard input. 
 * The tokens are defined in the enumeration Token.
 * Returns one token on each call to getNextToken()
 */
public class Scanner {
      private final static Map<String, Token> keywords;

      /* Static initializer */
      static {
          keywords = new HashMap<String, Token>();
          addKeyword( Token.KW_BEGIN );
          addKeyword( Token.KW_CALL );
          addKeyword( Token.KW_CONST );
          addKeyword( Token.KW_DO );
          addKeyword( Token.KW_ELSE );
          addKeyword( Token.KW_END );
          addKeyword( Token.KW_FOR );
          addKeyword( Token.KW_IF );
          addKeyword( Token.KW_PROCEDURE );
          addKeyword( Token.KW_READ );
          addKeyword( Token.KW_SKIP );
          addKeyword( Token.KW_THEN );
          addKeyword( Token.KW_TYPE );
          addKeyword( Token.KW_VAR );
          addKeyword( Token.KW_WHILE );
          addKeyword( Token.KW_WRITE );
      }

      /** Size of the lookahead buffer */
      private static final int BUFFERSIZE = 16384;

      /*************** Instance Variables *****************/
      private Source source; /* The source handler used by this lexer */
      private char charBuffer[] = new char[BUFFERSIZE];
      private int nextCh; /* The one character of look-ahead */
      private int bufferPos = 0; /* Position in charBuffer */
      private int bufferLength = 0; /* Number of characters in buffer */
      private int currentPosition = -1; /* Character position in input file */

      /****************** Constructors ********************/
      /** Basic constructor
       * @param src input source program stream */
      public Scanner( Source src ) throws IOException {
          source = src;
          nextCh = getNextChar();
      }
      /** Constructor with file name argument
       * @param fileName input file containing source program */
      public Scanner( String fileName ) throws IOException {
          this( new Source(fileName) );
      }
      /******************* Public Methods *****************/
      /** @return the current source handler */
      public Source getSourceHandler() {
          return source;
      }
      /** Fetch the next token from the input stream. 
       * @return next token unless end of file is reached
       * in which case an EOF token is returned
       */
      public LexicalToken getNextToken() throws IOException {
          Position currentPosn;
          char ch;
          /* Use a loop to allow multiple whitespace elements to be skipped.
           * When a token is matched it is returned,
           * but when a white space element is recognised 
           * this loop repeats to search for the next token or skip more
           * white space.
           */
          do {
              currentPosn = new Position( currentPosition );
              // Check if we've hit end of file
              if( nextCh == -1 ) {
                  return new LexicalToken( Token.EOF, currentPosn );
              }
              ch = (char)nextCh;
              nextCh = getNextChar();
              /* If ch is a letter, read an identifier or keyword */
              if( Character.isLetter(ch) ) {
                  return getIdentifierToken( ch, currentPosn );
              }
              /* if ch is a digit, read a number */
              if( Character.isDigit(ch) ) {
                  return getNumberToken( ch, currentPosn );
              }
              switch( ch ) {
              // Skip over whitespace
              case ' ':   // blank
              case '\t':  // tab
              case '\f':  // form feed
              case '\n':  // newline
              case '\r':  // carriage return
                  break;
              case '/':
                  if( nextCh == '/' ) {
                      // skip comment until end of line or end of file
                      while( nextCh != '\n' && nextCh != -1 ) {
                          nextCh = getNextChar();
                      }
                      // newline or end of file handled by next iteration
                      break;
                  } else {
                      /* We have a divide sign */
                      return new LexicalToken( Token.DIVIDE, currentPosn );
                  }
              case '+': 
                  return new LexicalToken( Token.PLUS, currentPosn );
              case '-':
                  return new LexicalToken( Token.MINUS, currentPosn );
              case '*': 
                  return new LexicalToken( Token.TIMES, currentPosn );
              case '(': 
                  return new LexicalToken( Token.LPAREN, currentPosn );
              case ')': 
                  return new LexicalToken( Token.RPAREN, currentPosn );
              case ';': 
                  return new LexicalToken( Token.SEMICOLON, currentPosn );
              case ':':
                  if( nextCh == '=' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.ASSIGN, currentPosn );
                  }
                  return new LexicalToken( Token.COLON, currentPosn );
              case ',':
                  return new LexicalToken( Token.COMMA, currentPosn );
              case '.': 
                  if( nextCh == '.' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.RANGE, currentPosn);
                  }
                  return new LexicalToken( Token.ILLEGAL, currentPosn );
              case '=':
                  return new LexicalToken( Token.EQUALS, currentPosn );
              case '!':
                  if( nextCh == '=' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.NEQUALS, currentPosn );
                  }
                  return new LexicalToken( Token.LOG_NOT, currentPosn );
              case '<':
                  if( nextCh == '=' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.LEQUALS, currentPosn );
                  }
                  return new LexicalToken( Token.LESS, currentPosn );
              case '>':
                  if( nextCh == '=' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.GEQUALS, currentPosn );
                  }
                  return new LexicalToken( Token.GREATER, currentPosn );
              case '&':
                  if( nextCh == '&' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.LOG_AND, currentPosn );
                  }
                  return new LexicalToken( Token.ILLEGAL, currentPosn );  
              case '|':
                  if( nextCh == '|' ) {
                      nextCh = getNextChar();
                      return new LexicalToken( Token.LOG_OR, currentPosn );
                  }
                  return new LexicalToken( Token.ILLEGAL, currentPosn );
              case '[':
                  return new LexicalToken( Token.LBRACKET, currentPosn );
              case ']':
                  return new LexicalToken( Token.RBRACKET, currentPosn );
              default:
                  return new LexicalToken( Token.ILLEGAL, currentPosn );
              }
          } while ( true );                
      }
    
      /** read an identifier (or keyword) starting from the given character ch,
       * and return the resulting token */
      private LexicalToken getIdentifierToken( char ch,Position currentPosn )
        throws IOException {
          StringBuffer buf = new StringBuffer();
          buf.append( ch );
          while( nextCh != -1 && Character.isLetterOrDigit((char)nextCh) ) {
              buf.append( (char)nextCh );
              nextCh = getNextChar();
          } 
          String word = buf.toString(); // .toLowerCase(); // Case insensitive
          if( keywords.containsKey( word ) ) {
              return new LexicalToken( keywords.get(word), currentPosn );
          } else {
              return new IdentifierToken( Token.IDENTIFIER, currentPosn, word );
          }
      }

      /** read a number starting from the given character ch and return the
       * resulting token */
      private LexicalToken getNumberToken( char ch, Position currentPosn )
        throws IOException {
          StringBuffer buf = new StringBuffer();
          buf.append( ch );
          while( nextCh != -1 && Character.isDigit((char)nextCh) ) {
              buf.append( (char)nextCh );
              nextCh = getNextChar();
          }
          int value = 0x80808080; // Nonsense value
          try {
              value = Integer.parseInt( buf.toString() );
          } catch( NumberFormatException e ) { 
              /* Can only happen if the number is too big */
              error( "integer too large", currentPosn );
          }
          return new NumberToken( Token.NUMBER, currentPosn, value );
      }
      /* Fetch the next character from the input stream and return it, updating
       * the current position. 
       */
      private int getNextChar() throws IOException {
          if( bufferPos == bufferLength ) {
              bufferPos = 0;
              bufferLength = source.read( charBuffer, 0, charBuffer.length );
              if( bufferLength == -1 ) {
                  return -1;
              }
          }
          currentPosition++;
          return charBuffer[bufferPos++];
      }
      /** Add a keyword to the keyword look up table */
      private static void addKeyword( Token keyword ) {
          if( keywords.put( keyword.toString(), keyword ) != null) {
              ErrorHandler.getErrorHandler().errorMessage( 
                      "duplicate keyword in scanner", Severity.FATAL,
                      Position.NO_POSITION );
          }
      }
      //* Error error message */
      private void error( String message, Position pos ) {
          ErrorHandler.getErrorHandler().errorMessage( 
                  message, Severity.ERROR, pos );
      }
}
