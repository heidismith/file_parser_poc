import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

/**
 * User: Heidi
 * Date: 10/3/17
 */
public class DataFileReader {

    private Path file;

    public DataFileReader(Path file) {
        this.file = file;
    }

    /**
     * Parse file and store valid data entries in their associated tables.  We could have separated out reading from
     * storing for better modularization.  However, we expect these files to be large, and we prefer not to
     * iterate twice (once to read and one to put into a hashmap).
     * Use BufferedReader for speed
     *
     * @throws FileReaderException With details on errors parsing the file
     */
    public void readAndStoreData() throws FileReaderException {

        // Find the associated specifications
        String dataTable = this.getDataTable();
        ArrayList<ColumnSpecification> specs = this.readSpec(dataTable);

        // Create the table if needed
        try {
            DBHelper.createTableIfNotExists(dataTable, specs);
        } catch (SQLException ex){
            throw new FileReaderException("Error creating associated database table: " + ex);
        }

        // For file validation, we want all the file errors at once, not one by one.  And line number for reference.
        HashMap<Integer, String> linesWithError = new HashMap<Integer, String>();
        Integer currentLineNumber = 1;

        try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {

            String currentDataLine;
            while ((currentDataLine = reader.readLine()) != null) {

                // Each line in the file is one data row.  Parse the line to determine the values for each column
                ArrayList<DataCellEntry> dataCellEntries = new ArrayList<DataCellEntry>() ;
                int curIndex = 0;
                boolean allCellsValid = true;
                for (ColumnSpecification spec : specs) {

                    // Find the value in the line based off the width in the spec
                    int newIndex = curIndex+spec.getWidth();
                    String value = currentDataLine.substring(curIndex,newIndex).trim();
                    curIndex = newIndex;

                    // validate each data cell
                    try {
                        String formattedValue = DBHelper.validateAndFormatDataValue(value, spec.getDataType());
                        dataCellEntries.add(new DataCellEntry(spec.getName(), formattedValue));
                    } catch (InvalidDataException ide) {
                        allCellsValid = false;
                        linesWithError.put(currentLineNumber, currentDataLine + " - INVALID: " + ide.getMessage());
                    }
                }

                // All data cells valid, try to save the whole row in the database
                if (allCellsValid){
                    try {
                        DBHelper.insertDataRow(dataTable, dataCellEntries);
                    } catch (SQLException x) {
                        linesWithError.put(currentLineNumber, currentDataLine + " NOT STORED: " + x);
                    }
                }

                currentLineNumber++;
            }

        } catch (IOException ex){
            throw new FileReaderException("Error reading file.  Exception message: " + ex);
        }

        if (linesWithError.size() > 0) {
            throw new FileReaderException(linesWithError, "We found invalid data in the file.  " +
                    "Some lines were not stored.");
        }

    }

    private String getDataTable() {
        String filename = this.file.getFileName().toString();
        return filename.substring(0,filename.indexOf('_'));
    }

    private ArrayList<ColumnSpecification> readSpec(String dataTable) throws FileReaderException {
        String specFileName = dataTable +".csv";
        Path specsDir = Paths.get("specs" , specFileName);

        if (Files.notExists(specsDir)) {
            throw new FileReaderException(
                    "Whole file not processed. We could not find a matching spec file: " + specFileName);
        }

        ArrayList<ColumnSpecification> specs = new ArrayList<ColumnSpecification>();

        try (BufferedReader reader = Files.newBufferedReader(specsDir, Charset.forName("UTF-8"))) {

            // Parse the file into a list of specifications.  Ignore the first line
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] attributes = currentLine.split(",");
                if (attributes.length !=3 ) {
                    throw new FileReaderException(
                            "Whole file not processed. Matching spec file: " + specFileName +
                                    " does not have exactly 3 attributes: " + currentLine);
                } else if (attributes[1].equalsIgnoreCase("width")) {
                    continue;
                }

                String dataType = attributes[2].trim();
                if (!Arrays.asList("INTEGER", "BOOLEAN","TEXT").contains(dataType)) {
                    throw new FileReaderException(
                            "Whole file not processed. Matching spec file: " + specFileName +
                                    " contains an unknown datatype: " + dataType);
                }

                try {
                    int width = Integer.parseInt(attributes[1].trim());  //todo NFE
                    specs.add(new ColumnSpecification(attributes[0].trim(), width, dataType));
                } catch (NumberFormatException nfe) {
                    throw new FileReaderException(
                            "Whole file not processed. Matching spec file: " + specFileName +
                                    " contains a width which is not an integer: " + currentLine);
                }

            }
        } catch (IOException ex){
            throw new FileReaderException(
                    "Whole file not processed due to an error reading: " + ex);
        }

        return specs;

    }
}
