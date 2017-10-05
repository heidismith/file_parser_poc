import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Create a singleton Database connection for use in the application.
 * Assumption: this program is not multi-threaded.
 * User: Heidi
 * Date: 10/3/17
 */
public class DBConnection {
    static Connection con = null;

    public static Connection getConnection(){
        if (con != null) return con;
        // todo get db, user, pass from settings file
        return getConnection("test", "sa", "");
    }

    private static Connection getConnection(String dbName, String userName, String password) {
        try {
            Class.forName("org.h2.Driver");
            con = DriverManager.getConnection("jdbc:h2:~/" + dbName, userName, password);
        } catch (Exception e) {
            e.printStackTrace();   //todo
            return null;
        }
        return con;
    }

    public static void closeConnection() {
        try {
            if (con != null) con.close();
        } catch (Exception e) {
            e.printStackTrace();  //todo
        } finally {
            con = null;
        }
    }

}