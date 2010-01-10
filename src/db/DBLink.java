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

//the methods in this class arn't thread safe but the class is
//it allows access to the link information in database
public class DBLink extends DBObject
{
	private static final DBLink link = new DBLink();
	private ConfigDB conf;
	private final String getLinksQuery = "select domain, url, relevance from links where del = 'f' limit ?";
	private final String chkDupeLinkDomainQuery = "select 1 from links where domain = ?";
	private final String delLinkStmt = "update links set del = 't' where domain = ?";
	private final String setLinkStmt = "insert LOW_PRIORITY into links (domain, url, relevance) values (?, ?, ?)";
	

	// Prepared statements
	private PreparedStatement getLinksPS;
	private PreparedStatement chkDupeLinkDomianPS;
	private PreparedStatement delLinkPS;
	private PreparedStatement setLinkPS;

	private DBLink()
	{}

	public static DBLink getInstance()
	{
		return link;
	}

	public void init(ConfigDB conf)
	{
		this.conf = conf;
	}

	protected void createPreparedStatements() throws SpiderDataException
	{
		// create prepared statements
		try
		{
			getLinksPS = con.prepareStatement(getLinksQuery);
			chkDupeLinkDomianPS = con.prepareStatement(chkDupeLinkDomainQuery);
			delLinkPS = con.prepareStatement(delLinkStmt);
			setLinkPS = con.prepareStatement(setLinkStmt);
		} catch (SQLException e)
		{
			// prepared statement creation failed
			throw new SpiderDataException(this.getClass().getSimpleName(), "createPreparedStatements", e.getMessage());
		}
	}
	
	@Override
	protected void destroyPreparedStatements() throws SpiderDataException
	{
		//do our best to close the PS
		try
		{
			getLinksPS.close();
			chkDupeLinkDomianPS.close();
			delLinkPS.close();
			setLinkPS.close();
		}catch(Exception e){}
	}

	// this method should only be called from linkQueue which is thread safe
	// because on its own this method isn't
	// it gets the links for a specified number of domains and locks the domains
	// and marks the links for removal
	public ArrayList<Link> getLinksAndDelete(int numberOfLinks) throws SpiderDataException
	{
		ensureConnection();

		ArrayList<Link> rc = new ArrayList<Link>(numberOfLinks);
		try
		{
			//execute prepared statement to get links
			getLinksPS.setInt(1, numberOfLinks);
			ResultSet rs = getLinksPS.executeQuery();
			String linkDomain, linkUrl;
			int linkRelevance;
			
			while(rs.next())
			{
				//get the link info that was returned
				linkDomain = rs.getString(1);
				linkUrl = rs.getString(2);
				linkRelevance = rs.getInt(3);
				
				//remove the link from the db
				delLinkPS.setString(1, linkDomain);
				delLinkPS.execute();
				
				//add link to the list
				rc.add(new Link(linkDomain, linkUrl, linkRelevance));
			}
			rs.close();

		} catch (SQLException e)
		{
			// if no links were returned then this spider has nothing to do so
			// kill thread
			throw new SpiderDataException(e);
		}

		return rc;
	}

	// adds links to the links table
	public void addNewLinks(Collection<Link> links) throws SpiderDataException
	{
		ensureConnection();
		
		// add links to db
		for (Link l : links)
		{
			// if the link is too long then skip it since its over the set limit
			if (l.getUrl().length() > conf.getMaxUrlLength())
				continue;

			//check if there is a link already for the links domain
			try
			{
				chkDupeLinkDomianPS.setString(1, l.getDomainName());
				//check if the query returned a result which indicates that there is a dupe
				if(chkDupeLinkDomianPS.executeQuery().next())
					continue;
				
			} catch (SQLException e)
			{
				Logger.log(1, "DBLink", "addNewLinks", "couldn't check for duplicate domains: " + e.getMessage());
			}

			//add the link
			try
			{
				// set link domain and url and relevance in prepared statement
				setLinkPS.setString(1, l.getDomainName());
				setLinkPS.setString(2, l.getUrl());
				setLinkPS.setInt(3, l.getDomainRelevance());
				setLinkPS.execute();

			} catch (SQLException e)
			{
				Logger.log(1, "DBLink", "addNewLinks", "couldn't insert link: " + e.getMessage());
			}
		}
	}

}
