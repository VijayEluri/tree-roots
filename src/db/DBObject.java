package db;

import java.sql.Connection;

import exception.SpiderDataException;

public abstract class DBObject
{
	Connection con = null;

	protected void ensureConnection() throws SpiderDataException
	{
		// check if connection is still valid
		if (con != null)
		{
			if (ConnectionManager.getInstance().checkConnection(con))
				return;

			// since its invalid we need to close(if possible) and create a new one
			try {destroyPreparedStatements();} catch (Exception e) {}
			try {con.close();} catch (Exception e) {}
		}
		// get new connection
		con = ConnectionManager.getInstance().createLocalConnection();

		// create the prepared statements using this connection
		createPreparedStatements();
	}

	abstract protected void createPreparedStatements() throws SpiderDataException;

	abstract protected void destroyPreparedStatements() throws SpiderDataException;
}
