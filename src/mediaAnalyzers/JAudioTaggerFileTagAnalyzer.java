package mediaAnalyzers;

import java.io.File;
import java.util.logging.Level;

import logger.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

import config.ConfigAnalyzer;
import dataType.AudioLink;
import exception.SpiderDataException;

public class JAudioTaggerFileTagAnalyzer extends MediaAnalyzerAbs
{
	private String artist;
	private String album;
	private String track;
	
	// initalize the supportedFileTypes
	public JAudioTaggerFileTagAnalyzer(ConfigAnalyzer conf)
	{
		super(conf);
		supportedFileTypes = new String[] { "mp3", "mp4", "flac", "ogg" };

		// set the logging for the jAudioTagger
		AudioFile.logger.getParent().setLevel(Level.WARNING);
	}

	@Override
	public boolean analyze(File inFile, AudioLink analyzedFile)
			throws SpiderDataException
	{
		AudioFile f = null;

		try
		{
			f = AudioFileIO.read(inFile);
		} catch (Exception e)
		{
			Logger
					.logDebug("JAudioTaggerFileTagAnalyzer - analyze() No audio header found for: "
							+ inFile.getName());
			// no header found
			return false;
		}

		if (f != null)
		{
			Tag tag = f.getTag();
			// get the audio header
			//AudioHeader header = f.getAudioHeader();

			if (tag != null)
			{

				artist = clean(tag.getFirstArtist());
				album = clean(tag.getFirstAlbum());
				track = clean(tag.getFirstTitle());
					
				// set the artist, album and track from the tags found
				if(artist != null && !artist.isEmpty()) analyzedFile.setArtist(artist);
				if(album != null && !album.isEmpty()) analyzedFile.setAlbum(album);
				if(track != null && !track.isEmpty()) analyzedFile.setTrack(track);
				analyzedFile.setSearchField(artist + " " + track + " " + album);
				
				System.out.println("JAudio artist: " + artist + " track:" + track);
			}
		}

		// if we've found either the artist or track thats enough to return true
		if(analyzedFile.getArtist() != null && analyzedFile.getTrack() != null)
			return true;
		else
			return false;
	}

	private String clean(String in)
	{
		// clean and set artist
		if (in != null && !in.isEmpty())
		{
			// replace underscores
			in = in.replace('_', ' ');
		}

		return in;
	}
}
