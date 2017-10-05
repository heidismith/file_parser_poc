import java.util.Map;

/**
 * An exception to return more information about what lines failed when parsing a file.
 * Includes one message about the overall file, plus a list of line numbers and an error message for each line.
 * <p/>
 * User: Heidi
 * Date: 10/3/17
 */
public class FileReaderException extends Exception {
    // Keep track of all lines in the file that did not validate
    final Map<Integer, String> errorLines;

    public FileReaderException(String message) {
        super(message);
        this.errorLines = null;
    }

    public FileReaderException(Map<Integer, String> errorLines, String message) {
        super(message);
        this.errorLines = errorLines;
    }

    public Map<Integer, String> getErrorLines() {
        return errorLines;
    }

}
