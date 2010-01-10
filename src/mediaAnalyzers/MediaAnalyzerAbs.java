package mediaAnalyzers;

import java.io.File;

import config.ConfigAnalyzer;
import dataType.AudioLink;
import exception.SpiderDataException;

public abstract class MediaAnalyzerAbs implements MediaAnalyzerInt
{
	String [] supportedFileTypes;
	ConfigAnalyzer conf;
	
	public MediaAnalyzerAbs(ConfigAnalyzer conf)
	{
		this.conf = conf;
	}
	
	public boolean canAnalyze(String fileType)
	{
		for(String f : supportedFileTypes)
			if(f.equalsIgnoreCase(fileType))
				return true;
		
		return false;
	}
	
	public abstract boolean analyze(File f, AudioLink analyzedFile) throws SpiderDataException;	
}
