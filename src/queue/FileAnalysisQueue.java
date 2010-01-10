package queue;

import java.util.ArrayList;

import logger.Logger;
import config.ConfigQueue;
import dataType.FileLink;
import dataType.Link;
import db.DBFile;
import exception.SpiderDataException;

public class FileAnalysisQueue
{
	private static ArrayList<Link> localFileLinks;
	private static final FileAnalysisQueue fileAnalysisQueue = new FileAnalysisQueue();
	private ConfigQueue conf;

	private FileAnalysisQueue()
	{
		localFileLinks = new ArrayList<Link>();
	}
	
	public void init(ConfigQueue conf) {this.conf = conf;}
	
	public static FileAnalysisQueue getInstance() {return fileAnalysisQueue;}
	
	//This will return a Link from either the FileQueue which contains links retrieved
	//from the spidering process or if that queue is empty then it will get a link 
	//from the DB
	public synchronized FileLink getLocalFileLink()
	{
		if(!localFileLinks.isEmpty())
			return (FileLink)localFileLinks.remove(0);
		else
			return null;
	}
	
	// this isn't thread safe and wont be accurate but can be used to 
	//roughly gauge if the queue is empty
	public boolean isQueueEmpty() {return localFileLinks.isEmpty();}

	public synchronized void addLocalFileLink(FileLink fileLink)
	{
		//add local file to the queue
		localFileLinks.add(fileLink);
	}
	
	// this isn't thread safe and wont be accurate but can be used to 
	//roughly gauge if the queue is full
	public boolean isQueueFull()
	{
		if(localFileLinks.size() >= conf.getFileAnalysisQueueCapacity())
			return true;
		else
			return false;
	}
	
	// returns a non thread safe approximation of the remaining size left in the queue
	public int getRemainingQueueCapacity()
	{
		int remainingSize = conf.getFileAnalysisQueueCapacity() - localFileLinks.size();
		
		return remainingSize < 0 ? 0 : remainingSize; 
	}
	
	//write remaining files if any exist
	public void finalize()
	{
	    try
	    {
	    	//write remaining fileLinks to DB
	    	DBFile.getInstance().addFileLinks(localFileLinks);	
	    } catch (SpiderDataException e)
	    {
		Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
	    }
	}
}
