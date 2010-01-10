package db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;

import dataType.SpiderInfo;

import logger.Logger;
import exception.SpiderDataException;

//the methods in this class arn't thread safe but the class is
//it allows access to the Spider Status information from the database
public class DBSpiderStatus extends DBObject
{
	private static final DBSpiderStatus dBSpiderStatus = new DBSpiderStatus();

	private String delOldSpiderInfoForManagerStmt = "delete from spider_info where spider_manager = ";
	private String setSpiderInfoStmt = "insert into spider_info values(?, ?, ?, ?, ?, ?, ?)";
	private String updateSpiderInfoStmt = "update spider_info set status = ?, running = ?, cur_domain = ?, last_checked = ? where id = ? and type = ? and spider_manager = ?";

	private PreparedStatement setSpiderInfoPS;
	private PreparedStatement updateSpiderInfoPS;

	private DBSpiderStatus()
	{}

	public static DBSpiderStatus getInstance()
	{
		return dBSpiderStatus;
	}

	@Override
	protected void createPreparedStatements() throws SpiderDataException
	{
		try
		{
			updateSpiderInfoPS = con.prepareStatement(updateSpiderInfoStmt);
			setSpiderInfoPS = con.prepareStatement(setSpiderInfoStmt);
		} catch (SQLException e)
		{
			// prepared statement creation failed
			throw new SpiderDataException(this.getClass().getSimpleName(), "createPreparedStatements", e.getMessage());
		}
	}

	@Override
	protected void destroyPreparedStatements() throws SpiderDataException
	{
		// do our best to close the PS
		try
		{
			updateSpiderInfoPS.close();
			setSpiderInfoPS.close();
		} catch (Exception e)
		{}
	}

	// should be executed at the start of spider manager execution to clear old spider info out
	public void removeOldSpiderInfoManager(String managerName) throws SpiderDataException
	{
		ensureConnection();

		try
		{
			Statement stmt = con.createStatement();
			stmt.execute(delOldSpiderInfoForManagerStmt + "'" + managerName + "'");
			stmt.close();

		} catch (SQLException e)
		{
			Logger.log(1, this.getClass().getSimpleName(), "removeOldSpiderInfoManager", "wiping spider info failed: " + e.getMessage());
		}
	}

	public void writeAllSpiderInfo(String managerName, Collection<SpiderInfo> spiderInfo) throws SpiderDataException
	{
		ensureConnection();

		// loop through spider info and write it to db
		for (SpiderInfo i : spiderInfo)
		{
			try
			{
				// set the parameters to update
				updateSpiderInfoPS.setString(1, i.getStatus());
				updateSpiderInfoPS.setString(2, i.isRunning() ? "t" : "f");
				updateSpiderInfoPS.setString(3, i.getDomainName());
				updateSpiderInfoPS.setTimestamp(4, new Timestamp(i.getLastChecked().getTime()));
				updateSpiderInfoPS.setInt(5, i.getId());
				updateSpiderInfoPS.setString(6, i.getType());
				updateSpiderInfoPS.setString(7, managerName);
				// execute update spiderInfo statement
				// this will fail if there aren't any records to update
				if (updateSpiderInfoPS.executeUpdate() > 0)
					continue;
			} catch (SQLException e) 
			{
				e.printStackTrace();
			}

			try
			{
				// if nothing was updated then try to insert instead
				// if update fails then this could be the first so try to insert it instead
				setSpiderInfoPS.setString(1, managerName);
				setSpiderInfoPS.setString(2, i.getType());
				setSpiderInfoPS.setInt(3, i.getId());
				setSpiderInfoPS.setString(4, i.getStatus());
				setSpiderInfoPS.setString(5, i.isRunning() ? "t" : "f");
				setSpiderInfoPS.setString(6, i.getDomainName());
				setSpiderInfoPS.setTimestamp(7, new Timestamp(i.getLastChecked().getTime()));
				// execute insert
				setSpiderInfoPS.execute();
			} catch (SQLException e)
			{
				Logger.log(1, this.getClass().getSimpleName(), "writeALLSpiderInfo", "inserting spider info failed: " + e.toString());
			}
		}

	}
}
