package mediaAnalyzers;

import java.io.File;
import java.io.IOException;

import org.cmc.music.common.ID3ReadException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import config.ConfigAnalyzer;
import dataType.AudioLink;

import exception.SpiderDataException;

public class MyId3TagAnalizer extends MediaAnalyzerAbs
{
	private String artist;
	private String album;
	private String track;

	public MyId3TagAnalizer(ConfigAnalyzer conf)
	{
		super(conf);
		supportedFileTypes = new String[] { "mp3", "mp4", "flac", "ogg" };
	}

	@Override
	public boolean analyze(File f, AudioLink analyzedFile) throws SpiderDataException
	{
		// read metadata
		MusicMetadataSet src_set = null;
		try
		{
			src_set = new MyID3().read(f);
		} catch (ID3ReadException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}catch(Exception e)
		{
			System.out.println("MyID3: Exception reading tag");
			return false;
		}

		if (src_set == null) // perhaps no metadata
		{
			System.out.println("didn't find any meta data");
		} else
		{
			IMusicMetadata metadata = src_set.getSimplified();
			artist = clean(metadata.getArtist());
			album = clean(metadata.getAlbum());
			track = clean(metadata.getSongTitle());
			
			//set fields from ID3 tags
			if(artist != null && !artist.isEmpty()) analyzedFile.setArtist(artist);
			if(album != null && !album.isEmpty()) analyzedFile.setAlbum(album);
			if(track != null && !track.isEmpty()) analyzedFile.setTrack(track);
			analyzedFile.setSearchField(artist + " " + track + " " + album);
			System.out.println("MyID3 artist: " + artist + " track:" + track);
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
