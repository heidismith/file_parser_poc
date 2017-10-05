import java.awt.datatransfer.StringSelection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Helpers to communicate with an H2 SQL Database.  Keeps the DB formatting and implementation details separated
 * User: Heidi
 * Date: 10/4/17
 */
public class DBHelper {

    public static int insertDataRow(String dataTable, ArrayList<DataCellEntry> dataCellEntries)
            throws SQLException {

        StringBuilder columnNames = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (int i = 0; i < dataCellEntries.size(); i++) {

            DataCellEntry dataCellEntry = dataCellEntries.get(i);

            // build up list of column names & values
            columnNames.append(dataCellEntry.getColumnName());
            values.append(dataCellEntry.getFormattedValue());

            if (i != dataCellEntries.size() - 1) {
                columnNames.append(",");
                values.append(",");
            }

        }

        Statement stmt = DBConnection.getConnection().createStatement();
        int rowsUpdated = stmt.executeUpdate("INSERT INTO " + dataTable + " (" + columnNames.toString() + ") VALUES ( "
                + values.toString() + " )");
        stmt.close();

        return rowsUpdated;

    }

    public static int selectCountFromTable(String dataTable) throws SQLException {

        Statement stmt = DBConnection.getConnection().createStatement();
        ResultSet queryResults = stmt.executeQuery("SELECT count(*) FROM " + dataTable);
        queryResults.next();
        int count = queryResults.getInt(1);
        stmt.close();

        return count;

    }

    public static boolean createTableIfNotExists(String dataTable, ArrayList<ColumnSpecification> specs)
            throws SQLException {

        StringBuilder columnDefs = new StringBuilder();

        for (ColumnSpecification spec : specs) {

            //Add name
            columnDefs.append(spec.getName()).append(" ");

            //Add datatype
            switch(spec.getDataType()) {
                case "INTEGER":
                    columnDefs.append("INT,");
                    break;
                case "BOOLEAN":
                    columnDefs.append("BOOLEAN,");
                    break;
                default:
                    String defaultText = "VARCHAR("+spec.getWidth()+"),";
                    columnDefs.append(defaultText);
            }
        }

        //remove final comma
        if (columnDefs.length() > 0) {
            String columnDefString = columnDefs.toString().substring(0, columnDefs.length() -1);
            Statement stmt = DBConnection.getConnection().createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + dataTable + " (" + columnDefString + ")");
            stmt.close();
            return true;
        }

        return false;

    }

    public static String validateAndFormatDataValue(String value, String dataType) throws InvalidDataException {

        // There are no nullable/not nullable specifications in the database table specs.  Therefore, assume
        // all columns are not-nullable.  Verify assumption with client.
        if (value == null || value.equals("")) {
            throw new InvalidDataException("Data cannot be empty.");
        }

        switch (dataType) {
            case "INTEGER":
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException nfe) {  //error if not an int
                    throw new InvalidDataException(value + " is not an INTEGER");
                }
                break;
            case "TEXT":
                value = "'" + value + "'"; // enclose in single quotes for DB insert statement
                break;
            case "BOOLEAN":
                if (!value.equals("1") && !value.equals("0")) {  //error if not 1 or 0
                    throw new InvalidDataException(value + " is not a valid BOOLEAN flag");
                }
                value = value.equals("1") + "";   //return boolean as string
                break;
            default:
                throw new InvalidDataException(dataType + " is not an known datatype");
        }
        return value;

    }



}
