package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import logger.Logger;
import config.ConfigDB;
import dataType.Link;
import exception.SpiderDataException;

//the methods in this class arn't threadsafe but the class is
//it allows access to the file information in database
public class DBFile extends DBObject
{
	private static final DBFile file = new DBFile();
	protected ConfigDB conf;
	// query srings
	private final String getFilesQuery = "select url from files where del = 'f' limit ?";
	private final String chkDupeFilesQuery = "select 1 from files where url = ?";
	private final String setFileStmt = "insert into files (url) values(?)";
	private final String delFileStmt = "update files set del = 't' where url = ?";

	private PreparedStatement getFilesPS;
	private PreparedStatement chkDupeFilesPS;
	private PreparedStatement setFilePS;
	private PreparedStatement delFilePS;

	private DBFile()
	{}

	public static DBFile getInstance()
	{
		return file;
	}

	// configure this object based on a confDB
	public void init(ConfigDB conf)
	{
		this.conf = conf;
	}

	@Override
	protected void createPreparedStatements() throws SpiderDataException
	{
		// create prepared statements
		try
		{
			getFilesPS = con.prepareStatement(getFilesQuery);
			chkDupeFilesPS = con.prepareStatement(chkDupeFilesQuery);
			delFilePS = con.prepareStatement(delFileStmt);
			setFilePS = con.prepareStatement(setFileStmt);
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
			getFilesPS.close();
			chkDupeFilesPS.close();
			delFilePS.close();
			setFilePS.close();
		} catch (Exception e)
		{}
	}

	// adds file links to DB
	public void addFileLinks(Collection<Link> fileLinks) throws SpiderDataException
	{
		ensureConnection();

		for (Link fileLink : fileLinks)
			try
			{
				// check if the file link already exists and skip it if so
				chkDupeFilesPS.setString(1, fileLink.getUrl());
				// check if the query returned a result which indicates that there is a dupe
				if (chkDupeFilesPS.executeQuery().next())
					continue;

				// check to see if this links url isn't already represented in the audio links table
				if (DBAudioLink.getInstance().checkIsDupUrl(fileLink.getUrl()))
					//skip if it is already represented
					continue;

				// add the link to the db
				setFilePS.setString(1, fileLink.getUrl());
				setFilePS.execute();

			} catch (SQLException e)
			{
				Logger.log(1, this.getClass().getSimpleName(), "addFileLinks", e.toString());
			}
	}

	public ArrayList<Link> getLinksAndDelete(int numberOfLinks) throws SpiderDataException
	{
		ensureConnection();

		ArrayList<Link> rc = new ArrayList<Link>(numberOfLinks);
		try
		{
			// execute prepared statement to get links
			getFilesPS.setInt(1, numberOfLinks);
			ResultSet rs = getFilesPS.executeQuery();
			String fileUrl;

			while (rs.next())
			{
				// get the link info that was returned
				fileUrl = rs.getString(1);

				// now remove the file link from the db
				delFilePS.setString(1, fileUrl);
				delFilePS.execute();

				// add link to the list
				rc.add(new Link(fileUrl));
			}
			rs.close();

		} catch (SQLException e)
		{
			// if no links were returned then this spider has nothing to do so
			// kill thread
			throw new SpiderDataException(e);
		}

		Logger.logDebug("retrieved  " + rc.size() + " file links");

		return rc;
	}

	/*
	 * // get a single filelink based on its id public FileLink getFile(int fileId) throws SpiderDataException { FileLink rc = null;
	 * 
	 * Connection con = ConnectionPool.getInstance().getConnection();
	 * 
	 * PreparedStatement ps; ResultSet rs = null; try {
	 * 
	 * ps = con.prepareStatement(queryFile); ps.setInt(1, fileId); rs = ps.executeQuery();
	 * 
	 * } catch (SQLException e) { throw new SpiderDataException(this.getClass().getSimpleName(), "getFile", "querying for file", e.toString()); }
	 * 
	 * if (rs != null) try { if (rs.next()) rc = new FileLink(fileId, rs.getString(2), new Integer(rs.getString(1))); } catch (Exception e) {
	 * 
	 * }
	 * 
	 * try { if (ps != null) ps.close(); } catch (SQLException e) {
	 * 
	 * }
	 * 
	 * return rc;
	 * 
	 * }
	 */
}
