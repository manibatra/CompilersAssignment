package source;

/**
 * interface Errors - interface to allow reporting of compilation
 *      errors and other messages. Use flush() to cause output.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */
public interface Errors {

    /** report an error with the given severity and source position */
    public void errorMessage( CompileError e );
    
    /** report an error with the given severity and source position */
    public void errorMessage( String message, Severity severity, Position pos );

    /** report an error with the given severity. */
    public void errorMessage(String message, Severity severity);
    
    /** report an error */
    public void errorMessage( String message);

    /** Print all pending messages immediately */
    public void listMessages();

    /** Print immediately a summary of all errors reported */
    public void errorSummary();

    /** List impending error messages, and clear accumulated errors. */
    public void flush();

    /** Return whether any errors have been reported at all */
    public boolean hadErrors();
    
}
