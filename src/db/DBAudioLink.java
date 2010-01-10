package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import dataType.AudioLink;

import logger.Logger;

import exception.SpiderDataException;

public class DBAudioLink extends DBObject
{
	private static final DBAudioLink dBAudioLink = new DBAudioLink();

	// query strings
	private final String insertAudioLinkStmt = "insert into AudioLinks(audio_id, domain, url) values(?, ?, ?)";
	private final String checkDupUrlQuery = "select 1 from AudioLinks where url = ?";
	// Prepared statements
	private PreparedStatement insertAudioLinkPS;
	private PreparedStatement checkDupUrlPS;

	private DBAudioLink()
	{}

	public static DBAudioLink getInstance()
	{
		return dBAudioLink;
	}

	@Override
	protected void createPreparedStatements() throws SpiderDataException
	{
		try
		{
			insertAudioLinkPS = con.prepareStatement(insertAudioLinkStmt);
			checkDupUrlPS = con.prepareStatement(checkDupUrlQuery);
		} catch (Exception e)
		{
			// prepared statement creation failed
			throw new SpiderDataException(this.getClass().getSimpleName(), "createPreparedStatements", e.getMessage());
		}
	}

	@Override
	protected void destroyPreparedStatements() throws SpiderDataException
	{
		try
		{
			insertAudioLinkPS.close();
		} catch (SQLException e)
		{}
	}

	// adds a collection of audio links to the respective db tables
	public void addAudioLinks(Collection<AudioLink> links) throws SpiderDataException
	{
		ensureConnection();

		for (AudioLink link : links)
		{
			// try to insert the audio link
			try
			{
				// get an audio id based on the artist and track
				try
				{
					DBAudio.getInstance().getAudioIdForAudioLink(link);
				} catch (SpiderDataException e)
				{
					throw e;
				}
				// check if the audio id was set and if so then
				// insert the audio link to the DB
				if (link.getAudioId() != null)
				{
					// insert the url and the domain id as taken from the
					// fileLink
					insertAudioLinkPS.setInt(1, link.getAudioId());
					insertAudioLinkPS.setString(2, link.getDomainName());
					insertAudioLinkPS.setString(3, link.getUrl());

					// execute sql to insert audio link
					insertAudioLinkPS.execute();
				}
			} catch (SQLException e)
			{
				/* this could happen if file is already in db */
				Logger.log(1, this.getClass().getSimpleName(), "addAudioLinks(Collection", "insert failed for: " + link + e);
			}

		}
	}

	// checks to see if a URL isn't already represented in the audio links table
	public boolean checkIsDupUrl(String url) throws SpiderDataException
	{
		ensureConnection();

		// check if the URL is already represented in the table
		try
		{
			//set passed in URL as search criteria
			checkDupUrlPS.setString(1, url);
			ResultSet rs = checkDupUrlPS.executeQuery();
			// check if URL is in table
			if (rs.next())
				// if yes then return
				return true;

		} catch (SQLException e)
		{
			Logger.log(1, this.getClass().getSimpleName(), "checkDupUrl", e.toString());
		}

		return false;
	}
}
