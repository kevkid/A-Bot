import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Database {
public static boolean createDB(){
	Connection c = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:Database/Database.db");
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      //System.exit(0);
      return false;
    }
    System.out.println("Opened database successfully");
    return true;
}
public static boolean createTables(){
	Connection c = null;
    Statement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:Database/Database.db");
      System.out.println("Opened database successfully");

      stmt = c.createStatement();
      String sql = "CREATE TABLE Exchange " +
                   "(ID INT PRIMARY KEY     NOT NULL," +
                   " Name           TEXT    NOT NULL) "; 
      stmt.executeUpdate(sql);
      sql = "CREATE TABLE Market " +
              "(ID INT PRIMARY KEY     NOT NULL," +
              " Label           TEXT    NOT NULL," +
              "Exchange_ID      INT     NOT NULL," + 
              "URL              TEXT    NOT NULL)"; 
      stmt.executeUpdate(sql);
      stmt.close();
      c.close();
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      //System.exit(0);
      return false;
    }
    System.out.println("Table created successfully");
    return true;
  }
public static ArrayList<ArrayList<Object>> executeSql(String sql){
	Connection c = null;
    Statement stmt = null;
    ResultSet rs = null;
    ArrayList<ArrayList<Object>> resultSet = new ArrayList<ArrayList<Object>>();
    int numColResults;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:Database/Database.db");
      System.out.println("Opened database successfully");

      stmt = c.createStatement();
      rs = stmt.executeQuery(sql);
      numColResults = rs.getMetaData().getColumnCount()+1; 
      while(rs.next()) {
          ArrayList<Object> row = new ArrayList<Object>();
          for(int index = 1; index < numColResults; index++){
        	  row.add(rs.getObject(index));
          }
          resultSet.add(row);
      }
      stmt.close();
      c.close();
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
     System.exit(0);
    }
    System.out.println("SQL executed successfully");
    return resultSet;
  }
public static String getLabelOrderbookURL(int exchangeID, String label, String action){//params: btc_usd, BUY
	String query;
	String result = "";
	String column;
	if(action.equalsIgnoreCase("BUY")){
		query = "Select Orderbook_Buy_URL_Structure from Exchange WHERE ID = " + exchangeID + ";" ;
	}
	else if(action.equalsIgnoreCase("SELL")){
		query = "Select Orderbook_Sell_URL_Structure from Exchange WHERE ID = " + exchangeID + ";" ;
	}
	else{
		return null;
	}
	ArrayList<ArrayList<Object>> queryResults = executeSql(query);
	result = queryResults.get(0).get(0).toString();
	result = result.replace("-label-", label);
	return result;
}
public static int getExchangeID(String input){
	String query = "Select ID from Exchange where Name like '" + input + "';";
	ArrayList<ArrayList<Object>> queryResults = executeSql(query);
	return (int)queryResults.get(0).get(0);
	}
public static String getFeeURL(int exchangeID, String label){
	String query = "Select Order_Fee_Structure from Exchange where ID = " + exchangeID + ";";
	ArrayList<ArrayList<Object>> queryResults = executeSql(query);
	 return queryResults.get(0).get(0).toString().replace("-label-", label);
	}
}

