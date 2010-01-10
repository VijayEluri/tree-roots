package mediaAnalyzers;

import java.io.File;

import dataType.AudioLink;

import exception.SpiderDataException;

public interface MediaAnalyzerInt
{
	public boolean canAnalyze(String fileType);
	 //returns if the file is sufficiently analysed
	public boolean analyze(File f, AudioLink analyzedFile) throws SpiderDataException; 
}
