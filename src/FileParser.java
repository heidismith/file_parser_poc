import java.nio.file.*;
import java.util.Map;

/**
 * An application to load data into a database from drops of data files and specification files.
 * It iterates over all files currently in the data directory and stores the data in the corresponding data tables.
 * It expects matching specification files for each type of data file.
 *
 * It includes some basic output with success messages and/or error reporting of data rows not successfully
 * stored in the database.
 *
 * This application could be run in a cron job, or be extended to watch for file additions to the data directory,
 * to process data from any new file drops (though note the caution below.)
 *
 * CAUTION: Rerunning the application with the same files in the data directory will result in the data entered
 * into the database twice.
 * Desired behavior on a rerun of the application needs to be further explored with the customer.
 *
 * User: Heidi Smith
 * Date: 10/3/17
 */
public class FileParser {

    public static void main(String args[]) throws Exception {

        // Find the data directory
        Path dataDir = Paths.get("data");

        // Iterate over all the data files in the data directory
        try (DirectoryStream < Path > stream = Files.newDirectoryStream(dataDir)) {

            for (Path file : stream) {

                //parse file and store valid data in the associated database tables. Report invalid data
                try {
                    DataFileReader reader = new DataFileReader(file.toAbsolutePath());
                    reader.readAndStoreData();
                    System.out.println("File: " + file.getFileName().toString() + " processed successfully.");
                } catch (FileReaderException fre) {
                    System.out.println("ERROR for file: " + file.getFileName().toString() + ". " + fre.getMessage());
                    if (fre.getErrorLines() != null && fre.getErrorLines().size() > 0) {
                        for (Map.Entry errorLine : fre.getErrorLines().entrySet()) {
                            System.out.println("  Data not saved for Line #" + errorLine.getKey() + ": "
                                    + errorLine.getValue());
                        }
                    }
                }
            }

        } catch (Exception ex) {
            System.out.println("There was an unexpected error.");
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection();
        }

    }

}
