package mediaAnalyzers;

import java.io.File;
import java.util.Collection;

//import javax.sound.midi.Track;

import net.roarsoftware.lastfm.*;

import logger.Logger;
import config.ConfigAnalyzer;
import dataType.AudioLink;
import exception.SpiderDataException;

public class LastFMAnalyzer extends MediaAnalyzerAbs
{

	public LastFMAnalyzer(ConfigAnalyzer conf)
	{
		super(conf);
		supportedFileTypes = new String[] { "mp3", "mp4", "flac", "ogg" };
		Caller.getInstance().setUserAgent("green");
	}

	@Override
	public boolean analyze(File f, AudioLink analyzedFile)
			throws SpiderDataException
	{
		boolean rc = false;
		String artistName = analyzedFile.getArtist();
		String trackName = analyzedFile.getTrack();

		// if an artist and track are already set use last fm to verify and
		// clean data
		if (artistName != null && !artistName.isEmpty())
		{
			Collection<Artist> artists = null;

			try
			{
				// first search for the artist
				artists = Artist.search(artistName, conf.getLastFMKey());
			} catch (Exception e)
			{
				Logger.log(0, "LastFMAnalyzer", "analyze",
						"error getting track: " + e.getMessage());
			}

			if (artists != null && !artists.isEmpty())
			{
				Artist artist = artists.iterator().next();
				artistName = artist.getName();
				// set the artist name
				analyzedFile.setArtist(artistName);
			} else
				// the artist name wasn't found
				artistName = null;

			// using the new artist name search for the track info
			if (trackName != null && !trackName.isEmpty() && artistName != null
					&& !artistName.isEmpty())
			{
				// then search for the track information
				Collection<Track> tracks = null;
				try
				{
					tracks = Track.search(artistName, trackName, 1, conf
							.getLastFMKey());
				} catch (Exception e)
				{
					Logger.log(0, "LastFMAnalyzer", "analyze",
							"error getting track: " + e.getMessage());
				}

				if (tracks != null && !tracks.isEmpty())
				{
					// track was found so set those values in the analyzed file
					// and set return code to true
					Track track = tracks.iterator().next();
					// set the album off of the results returned if not null

					String albumName = track.getAlbum();
					if (albumName != null)
					{
						analyzedFile.setAlbum(albumName);
					}

					// set the track name off of the results returned if not
					// null
					trackName = track.getName();
					if (trackName != null)
						analyzedFile.setTrack(trackName);

					// set search fields
					analyzedFile.setSearchField(artistName);

					rc = true;
				}
			}
		}

		return rc;
	}

}
