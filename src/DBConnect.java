

package dbConnect; 

import java.sql.Connection;


import java.sql.DriverManager;


import java.sql.SQLException;


public class DBConnect {


    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";


    private static final String URL =
        "jdbc:mysql://localhost:3306/studentdata?useSSL=false&serverTimezone=UTC";


    private static final String USER = "root";
    

    private static final String PASSWORD = "root";
  

    public static Connection getConnection() {
       

        try {
            

            Class.forName(DRIVER);
           

            connection = DriverManager.getConnection(URL, USER, PASSWORD);
           

        } catch (ClassNotFoundException e) {
           
            System.err.println("MySQL JDBC Driver not found.");
            System.err.println("Make sure the JAR is in the /lib folder.");
            System.err.println("Details: " + e.getMessage());
            

            throw new RuntimeException("JDBC Driver class not found: " + DRIVER, e);
          

        } catch (SQLException e) {
          
            System.err.println("Failed to connect to MySQL database.");
            System.err.println("Check: Is MySQL running? Is the password correct?");
            System.err.println("Is the database 'studentdata' created?");
            System.err.println("SQL State : " + e.getSQLState());
            

            System.err.println("Error Code: " + e.getErrorCode());
            
            System.err.println("Message   : " + e.getMessage());

            throw new RuntimeException("Database connection failed.", e);
            
        }

        return connection;
        
    }

}
