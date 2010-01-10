package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import logger.Logger;
import config.ConfigDB;
import exception.SpiderDataException;

public class ConnectionManager
{
	private static ConnectionManager connectionManager = new ConnectionManager();
	private static ConfigDB conf;
	
	public static ConnectionManager getInstance()
	{
		return connectionManager;
	}
	
	public static void init(ConfigDB c) 
	{
		conf = c;
	}
	
	//create connection to the local database
	protected Connection createLocalConnection() throws SpiderDataException
	{
		return createConnection(conf.getDriverForLocal(), conf.getConnectionStringForLocal());
	}
	
	//create connection to the local database
	protected Connection createRemoteConnection() throws SpiderDataException
	{
		return createConnection(conf.getDriver(), conf.getConnectionString());
	}
	
	//create connection to the local database
	protected Connection createConnection(String driver, String connectionString) throws SpiderDataException
	{
		Connection con = null;

		try
		{
			Class.forName(driver).newInstance();
			con = DriverManager.getConnection(connectionString);
			con.setAutoCommit(true);
		} catch (Exception e)
		{
			Logger.log(1, this.getClass().getSimpleName(), "createConnection",
					e.toString());
			throw new SpiderDataException("ConnectionManager", "createConnection",e.getMessage());
		}
		return con;
	}

	protected boolean checkConnection(Connection con)
	{
		// TODO: figure out which method to use
		// try
		// {
		// if(!c.isValid(1000))
		// c = createConnection();
		// } catch (SQLException e)
		// {
		// Logger.log(1 ,this.getClass().getSimpleName(), "returnConnection" ,
		// e.toString());
		// }

		// if not java 1.6
		try
		{
			Statement stmt = con.createStatement();
			stmt.executeQuery("select 1");
			stmt.close();
		} catch (Exception e)
		{
			// there is a problem with the connection
			return false;
		}
		
		//the connection seems fine
		return true;
	}
	
}
