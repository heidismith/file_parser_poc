import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A description of data to entered into one cell of a database table.
 *
 * User: Heidi
 * Date: 10/4/17
 */
public class DataCellEntry {
    private final String columnName;
    private final String formattedValue;

    public DataCellEntry(String columnName, String formattedValue) {
        this.columnName = columnName;
        this.formattedValue = formattedValue;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getFormattedValue() {
        return formattedValue;
    }

}
