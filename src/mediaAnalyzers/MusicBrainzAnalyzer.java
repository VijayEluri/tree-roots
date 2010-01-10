package mediaAnalyzers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import logger.Logger;
import config.ConfigAnalyzer;
import dataType.AudioLink;
import exception.SpiderDataException;

public class MusicBrainzAnalyzer extends MediaAnalyzerAbs
{
	private String cmd;

	// initilize the supportedFileTypes
	public MusicBrainzAnalyzer(ConfigAnalyzer conf)
	{
		super(conf);
		supportedFileTypes = new String[] { "mp3", "mp4", "flac", "ogg" };

		//start with the foler name
		cmd = "genpuid/";
		// the path for the genpuid shouldn't be changed
		// because the file location is relative to it
		if (conf.getIsLinux())
			cmd += "genpuid ";
		else
			//TODO: make this work on windows box
			cmd += "genpuid.exe -noanalysis ";

		// add key
		cmd += conf.getMusicIPKey();

		// add temDir path
		cmd += " " + conf.getTemStorageDir() ;
	}

	@Override
	public boolean analyze(File f, AudioLink analyzedFile)
			throws SpiderDataException
	{
		System.out.println("starting MusicBrainz Analyzer");
		// run the genpuid from the command line
		Runtime rt = Runtime.getRuntime();
		
		Process p;
		try
		{
			// execute the command + filename
			//quote file name to handle spaces
			p = rt.exec(cmd + "/" + f.getName());

		} catch (IOException e)
		{
			throw new SpiderDataException(this.getClass().getSimpleName(),
					"analyze", e.getMessage());
		}

		//read the output form the command
		InputStream in = p.getInputStream();

		String puid = "";

		try
		{
			int tem;
			while ((tem = in.read()) != -1)
				puid += (char) tem;
		} catch (IOException e)
		{
			Logger.log(1, this.getClass().getSimpleName(), "analyze",
			"couldn't read output from genpuid");
		}finally
		{
			//close the command output input stream
			p.destroy();
		}
		
		// parse puid from cmd output
		//a quick and dirty way of parsing the puid
		int puidStartIndex = puid.lastIndexOf(':');
		puid = puid.substring(puidStartIndex, puid.length()-1);

		System.out.println("puid" + puid);
		
		// using the puid query music brainz
		// http://musicbrainz.org/ws/1/track/?puid=01db0703-baed-3c45-3776-e986a7f7c27d&type=xml

		// alter the analyzedFile if the results seem more accurate
		
		//this analyzer always will return full track information
		return true;
	}
}
