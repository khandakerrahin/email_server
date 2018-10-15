package org.spider.emailservices.DataSources;

import databasehub.oracle.connect.OracleConnect;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;

import org.spider.emailservices.Utilities.NullPointerExceptionHandler;


public class PostalServicesDS {
	PreparedStatement preparedStatement; 
	InitialContext initialContext;
	ResultSet resultSet;
	CallableStatement callableStatement;
	public Connection con = null;

	public PostalServicesDS(){
		super();
		String url = "jdbc:oracle:thin:@spiderdxb-db-oracle.cytde62j1uix.ap-southeast-1.rds.amazonaws.com:1521:spiora";
		String userName = "spiderdbuserTest";
		String password = "spiderdbuserTest";/**/
		
		
		OracleConnect oracleConnect = new OracleConnect(url, userName, password);

		try {
			initialContext = new InitialContext();
			con = oracleConnect.getConnection();
			con.setAutoCommit(false);
		}catch(Exception e){
			System.out.println("Exception thrown " +e);
		}
	}
	public CallableStatement prepareCall(String statement) throws SQLException {
		this.callableStatement=con.prepareCall(statement);
		return this.callableStatement;
	}
	public CallableStatement getCallableStatement() {
		return this.callableStatement;
	}
	public void setCallableStatement(CallableStatement callableStatement) {
		this.callableStatement = callableStatement;
	}
	public PreparedStatement prepareStatement(String statement) throws SQLException {
		this.preparedStatement=con.prepareStatement(statement);
		return this.preparedStatement;
	}
	public PreparedStatement prepareStatement(String statement,boolean returnGeneratedKeys) throws SQLException {
		if(returnGeneratedKeys) {
			this.preparedStatement=con.prepareStatement(statement,PreparedStatement.RETURN_GENERATED_KEYS);
		}else {
			this.preparedStatement=con.prepareStatement(statement);
		}
		return this.preparedStatement;
	}
	public ResultSet getGeneratedKeys() throws SQLException {
		return this.preparedStatement.getGeneratedKeys();
	}
	public ResultSet executeQuery() throws SQLException {
		this.resultSet= this.preparedStatement.executeQuery();
		return this.resultSet;
	}
	
	public boolean execute() throws SQLException {
		return this.preparedStatement.execute();
	}
	
	public long executeUpdate() throws SQLException {
		return this.preparedStatement.executeUpdate();
	}
	
	public void closePreparedStatement() throws SQLException {
		this.preparedStatement.close();
	}
	public void closeResultSet() throws SQLException {
		this.resultSet.close();
	}
	public void closeCallableStatement() throws SQLException {
		this.callableStatement.close();
	}
	public boolean isCallableStatementClosed() throws SQLException {
		if(this.callableStatement==null) {
			return true;
		}else {
			return this.callableStatement.isClosed();
		}
	}
	public boolean isPreparedStatementClosed() throws SQLException {
		if(this.preparedStatement==null) {
			return true;
		}else {
			return this.preparedStatement.isClosed();
		}
	}
	public boolean isResultSetClosed() throws SQLException {
		if(this.resultSet==null) {
			return true;
		}else {
			return this.resultSet.isClosed();
		}
	}

	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return con;
	}



	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		con = connection;
	}



	/**
	 * @return the preparedStatement
	 */
	public PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}


	/**
	 * @param preparedStatement the ps to set
	 */
	public void setPreparedStatement(PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}


	/**
	 * @return the initialContext
	 */
	public InitialContext getInitialContext() {
		return initialContext;
	}



	/**
	 * @param initialContext the initialContext to set
	 */
	public void setInitialContext(InitialContext initialContext) {
		this.initialContext = initialContext;
	}



	/**
	 * @return the resultSet
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}

	/**
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}
	
	public void commit() throws SQLException {
		con.commit();
	}
	
	public void rollback() throws SQLException {
		con.rollback();
	}
	
	public void setAutoCommitOff() throws SQLException {
		con.setAutoCommit(false);
	}
	
	public void setAutoCommitOn() throws SQLException {
		con.setAutoCommit(true);
	}
	
	public boolean getAutoCommitStatus() throws SQLException{
		return con.getAutoCommit();
	}
	
	public void setString(int index, String value) throws SQLException{
		if(NullPointerExceptionHandler.isNullOrEmpty(value)) {
			this.preparedStatement.setNull(index, java.sql.Types.NULL);
		}else {
			this.preparedStatement.setString(index, value);
		}
	}
}

/*import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnector {
      public static String url = "jdbc:mysql://localhost:3306/";
      public static String dbName = "messageapps";
      public static String userName = "root";
      public static String password = "root";
            public static Connection getConnection() {
            Connection connection = null;
            
            try {
                       
                        Class.forName("com.mysql.jdbc.Driver").newInstance();
                        connection = DriverManager.getConnection(url+dbName,userName,password);
            } catch (Exception e) {
                  e.printStackTrace();
            }
            return connection;
      }
}/**/
