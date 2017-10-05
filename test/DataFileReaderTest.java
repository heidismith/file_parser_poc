import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * Test both the ability to read the file and
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Heidi
 * Date: 8/10/17
 */
public class DataFileReaderTest {

    private String testTable = "TESTFORMAT1";
    private String nonExistingTestTable = "NOTEXIST";
    private String testCreateTable = "SCORERECORD";
    private String badSpec1 = "BADSPEC1";
    private String badSpec2 = "BADSPEC2";
    private String badSpec3 = "BADSPEC3";

    @Before
    public void clearDBTable() throws Exception {

        //Do not put in DBHelper, because you should not do this in productions.
        Statement stmt = DBConnection.getConnection().createStatement();
        stmt.executeUpdate("DELETE FROM " + testTable);
        stmt.executeUpdate("DROP TABLE IF EXISTS " + testCreateTable);
        stmt.executeUpdate("DROP TABLE IF EXISTS " + badSpec1);
        stmt.executeUpdate("DROP TABLE IF EXISTS " + badSpec2);
        stmt.executeUpdate("DROP TABLE IF EXISTS " + badSpec3);
        stmt.close();
    }


    @Test
    public void testInvalidDataFilename() throws Exception {

        // Test bad filename
        Path testDataFile = Paths.get("data", "testformat1_2017-05.txt");
        DataFileReader badFileReader = new DataFileReader(testDataFile);
        boolean exceptionCaught = false;

        try {
            badFileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            exceptionCaught = true;
            assertTrue("Caught the wrong exception.  Was: " + ex.getMessage(),
                    ex.getMessage().contains("NoSuchFileException"));
        }
        assertTrue("Did get the exception!", exceptionCaught);
        int resultCount = DBHelper.selectCountFromTable(testTable);
        assertEquals("No data should have been stored", 0, resultCount);
    }

    @Test
    public void testMissingMatchingSpec() throws Exception {

        // Test filename with no matching spec
        Path testDataFile = Paths.get("data", "notexist_2015-06-28.txt");
        DataFileReader fileReader = new DataFileReader(testDataFile);
        boolean exceptionCaught = false;

        try {
            fileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            exceptionCaught = true;
            assertTrue("Caught the wrong exception.  Was: " + ex.getMessage(),
                    ex.getMessage().contains("could not find a matching spec"));
        }
        assertTrue("Did get the exception!", exceptionCaught);

        ResultSet rset = DBConnection.getConnection().getMetaData().getTables(null, null, nonExistingTestTable, null);
        if (rset.next()) {
            fail("Table found but should not exist: " + nonExistingTestTable);
        }
    }


    @Test
    public void testCreateTable() throws Exception {
        // Test known file with no errors & no existing table
        Path testDataFile = Paths.get("data", "scoreRecord_2016-10-04.txt");
        DataFileReader goodFileReader = new DataFileReader(testDataFile);
        try {
            goodFileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            fail("Test should not have had an exception, but it had: " + ex);
        }

        ResultSet rset = DBConnection.getConnection().getMetaData().getTables(null, null, testCreateTable, null);
        if (!rset.next()) {
            fail("Table not found, but should exist: " + testCreateTable);
        }

        //check DB Entries in table
        int resultCount = DBHelper.selectCountFromTable(testCreateTable);
        assertEquals("Two rows should have been stored", 2, resultCount);

    }

    @Test
    public void testSpecBadDatatype() throws Exception {
        // Test known file with no errors, but bad spec file
        Path testDataFile = Paths.get("data", "badspec1_2016-10-04.txt");
        DataFileReader fileReader = new DataFileReader(testDataFile);
        boolean exceptionCaught = false;
        try {
            fileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            exceptionCaught = true;
            assertTrue("Caught the wrong exception.  Was: " + ex.getMessage(),
                    ex.getMessage().contains("contains an unknown datatype"));
        }
        assertTrue("Did get the exception!", exceptionCaught);

        ResultSet rset = DBConnection.getConnection().getMetaData().getTables(null, null, badSpec1, null);
        if (rset.next()) {
            fail("Table found but should not exist: " + badSpec1);
        }
    }

    @Test
    public void testSpecBadWidth() throws Exception {
        // Test known file with no errors, but bad spec file
        Path testDataFile = Paths.get("data", "badspec2_2016-10-04.txt");
        DataFileReader fileReader = new DataFileReader(testDataFile);
        boolean exceptionCaught = false;
        try {
            fileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            exceptionCaught = true;
            assertTrue("Caught the wrong exception.  Was: " + ex.getMessage(),
                    ex.getMessage().contains("width which is not an integer"));
        }
        assertTrue("Did get the exception!", exceptionCaught);

        ResultSet rset = DBConnection.getConnection().getMetaData().getTables(null, null, badSpec2, null);
        if (rset.next()) {
            fail("Table found but should not exist: " + badSpec2);
        }
    }
    @Test
    public void testSpecMissingAttributes() throws Exception {
        // Test known file with no errors, but bad spec file
        Path testDataFile = Paths.get("data", "badspec3_2016-10-04.txt");
        DataFileReader fileReader = new DataFileReader(testDataFile);
        boolean exceptionCaught = false;
        try {
            fileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            exceptionCaught = true;
            assertTrue("Caught the wrong exception.  Was: " + ex.getMessage(),
                    ex.getMessage().contains("does not have exactly 3 attributes"));
        }
        assertTrue("Did get the exception!", exceptionCaught);

        ResultSet rset = DBConnection.getConnection().getMetaData().getTables(null, null, badSpec3, null);
        if (rset.next()) {
            fail("Table found but should not exist: " + badSpec3);
        }
    }


    @Test
    public void testFileWithTwoDataErrors() throws Exception {
        // Test known file with two data errors (invalid boolean and invalid int)
        Path testDataFile = Paths.get("data", "testformat1_2016-10-04.txt");
        DataFileReader fileReader = new DataFileReader(testDataFile);
        boolean exceptionCaught = false;
        try {
            fileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            exceptionCaught = true;
            assertTrue("Caught the wrong exception.  Was: " + ex.getMessage(),
                    ex.getMessage().contains("invalid data in the file"));
            assertEquals("Two lines had errors", 2, ex.getErrorLines().size());
        }
        assertTrue("Did get the exception!", exceptionCaught);
        //check DB Entries in table
        int resultCount = DBHelper.selectCountFromTable(testTable);
        assertEquals("One row should have been stored", 1, resultCount);


    }
    @Test
    public void testFileCompleteSuccess() throws Exception {
        // Test known file with no errors
        Path testDataFile = Paths.get("data", "testformat1_2015-06-28.txt");
        DataFileReader goodFileReader = new DataFileReader(testDataFile);
        try {
            goodFileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            fail("Test should not have had an exception");
        }

        //check DB Entries in table
        int resultCount = DBHelper.selectCountFromTable(testTable);
        assertEquals("Three rows should have been stored", 3, resultCount);

    }

    @Test
    public void testFileWithNullValue() throws Exception {
        // Test file with a null value
        Path testDataFile = Paths.get("data", "testformat1_2016-10-03.txt");
        DataFileReader fileReader = new DataFileReader(testDataFile);
        boolean exceptionCaught = false;
        try {
            fileReader.readAndStoreData();
        } catch (FileReaderException ex) {
            exceptionCaught = true;
            assertTrue("Caught the wrong exception.  Was: " + ex.getMessage(),
                    ex.getMessage().contains("invalid data in the file"));
            assertEquals("One line had an error", 1, ex.getErrorLines().size());
        }
        assertTrue("Did get the exception!", exceptionCaught);
        //check DB Entries in table
        int resultCount = DBHelper.selectCountFromTable(testTable);
        assertEquals("Two rows should have been stored", 2, resultCount);

    }

    @After
    public void closeDB() throws Exception {
        DBConnection.closeConnection();
    }

}
