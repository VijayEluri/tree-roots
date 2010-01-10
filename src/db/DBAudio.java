package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import dataType.AudioLink;

import logger.Logger;
import exception.SpiderDataException;

public class DBAudio extends DBObject
{
	private static final DBAudio dBAudio = new DBAudio();

	// query strings
	private final String getAudioIdByArtistAndTrackQuery = "select id from Audio where artist <=> ? and track <=> ?";
	private final String insertAudioStmt = "insert into Audio(artist, track, search_field) values(?, ?, ?)";
	private final String updateAudioFileCountStmt = "update Audio set file_count = file_count + 1 where id = ?";
	private final String getRandomArtistQuery = "SELECT artist FROM Audio WHERE id >= (SELECT FLOOR( MAX(id) * RAND()) FROM Audio) ORDER BY id LIMIT 1;";

	private PreparedStatement getAudioIdByArtistAndTrackPS;
	private PreparedStatement insertAudioPS;
	private PreparedStatement updateAudioFileCountPS;
	private PreparedStatement getRandomArtistPS;

	private DBAudio()
	{}

	public static DBAudio getInstance()
	{
		return dBAudio;
	}

	@Override
	protected void createPreparedStatements() throws SpiderDataException
	{
		try
		{
			getAudioIdByArtistAndTrackPS = con.prepareStatement(getAudioIdByArtistAndTrackQuery);
			insertAudioPS = con.prepareStatement(insertAudioStmt);
			updateAudioFileCountPS = con.prepareStatement(updateAudioFileCountStmt);
			getRandomArtistPS = con.prepareStatement(getRandomArtistQuery);
		} catch (SQLException e)
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
			getAudioIdByArtistAndTrackPS.close();
			insertAudioPS.close();
			updateAudioFileCountPS.close();
			getRandomArtistPS.close();
		} catch (SQLException e)
		{
			// nothing to do now were destroying everything anyway
		}

	}

	// get the audio id from the audio table based on the artist
	// and track name. If the artist/track combination isn't found
	// then it will create it and set audio id of the link to the
	// the newly created id
	public void getAudioIdForAudioLink(AudioLink link) throws SpiderDataException
	{
		ensureConnection();

		ResultSet rs = null;

		try
		{
			// set artist and track parameters to search on
			getAudioIdByArtistAndTrackPS.setString(1, link.getArtist());
			getAudioIdByArtistAndTrackPS.setString(2, link.getTrack());

			// execute sql to get audio id
			rs = getAudioIdByArtistAndTrackPS.executeQuery();

		} catch (SQLException e)
		{
			// this query can fail acceptably we're going to try again after
		}

		try
		{
			// get result if it was found
			if (rs != null && rs.next())
			{
				link.setAudioId(rs.getInt(1));
				rs.close();
			} else
			{
				// since the audio id wasn't found we need to create it
				// and then query the id again
				try
				{
					insertAudio(link);
				} catch (SQLException e)
				{
					// this can fail if another spider inserted the same
					// record after we queried for it.
					Logger.logDebug("Audio couldn't be created: " + e);
				}
			}
		} catch (SQLException e)
		{
			// this can fail if another spider inserted the same
			// record after we queried for it.
		}

		// re query for the audio id
		// we can just reuse the prepaired statment since the parameters
		// are the same
		if (link.getAudioId() == null)
		{
			try
			{
				rs = getAudioIdByArtistAndTrackPS.executeQuery();
			} catch (SQLException e)
			{
				// this will be handled later
			}

			try
			{
				// get result if it was found
				if (rs != null && rs.next())
					link.setAudioId(rs.getInt(1));
				else
				{
					throw new SpiderDataException(this.getClass().getSimpleName(), "getAudioIdForAudioLink", "id couldn't be created or found");
				}

			} catch (SQLException e)
			{
				throw new SpiderDataException(this.getClass().getSimpleName(), "getAudioIdForAudioLink", "getting audio id in final stage failed", e.toString());
			}
		}
	}

	// insert an audio link
	private void insertAudio(AudioLink link) throws SQLException
	{
		// set artist and track parameters to search on
		insertAudioPS.setString(1, link.getArtist());
		insertAudioPS.setString(2, link.getTrack());
		insertAudioPS.setString(3, link.getSearchField());

		// execute sql to get audio id
		insertAudioPS.execute();
	}

	// TODO: would a single query with a huge in statement work faster?
	// increments the file count for each of the referenced audio links passed
	// in
	public void incrementAudioFileCount(Collection<AudioLink> audioLinks) throws SpiderDataException
	{
		ensureConnection();

		for (AudioLink link : audioLinks)
		{
			try
			{
				// set artist and track parameters to search on
				updateAudioFileCountPS.setInt(1, link.getAudioId());
				// execute sql to get audio id
				updateAudioFileCountPS.execute();
			} catch (SQLException e)
			{
				// log exception but don't stop its not fatal
				Logger.log(0, "DBAudio", "incrementAudioFileCount", link.getAudioId() + " couldn't be incremented" + e.getMessage());
			}
		}
	}

	//returns a random artist from the audio table
	public String getRandomArtistName() throws SpiderDataException
	{
		ensureConnection();

		String artist = "";
		try
		{
			ResultSet rs = getRandomArtistPS.executeQuery();
			rs.next();
			artist = rs.getString(1);
		} catch (SQLException e)
		{
			Logger.log(0, "DBAudio", "getRandomArtistName", "random Artist couldn't be retrieved: " + e.getMessage());
		}

		return artist;
	}
}
