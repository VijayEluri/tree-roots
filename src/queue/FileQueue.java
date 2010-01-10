package queue;

import java.util.Collection;
import java.util.TreeSet;

import logger.Logger;
import config.ConfigQueue;
import dataType.Link;
import db.DBFile;
import exception.SpiderDataException;

public class FileQueue
{
	private static TreeSet<Link> fileLinks;
	private static final FileQueue fileQueue = new FileQueue();
	private ConfigQueue conf;

	private FileQueue()
	{
		fileLinks = new TreeSet<Link>();
	}
	
	public void init(ConfigQueue conf) {this.conf = conf;}
	
	public static FileQueue getInstance() {return fileQueue;}
	
	public synchronized void addFileLinks(Collection<Link> links) throws SpiderDataException
	{
		fileLinks.addAll(links);
		
		if(fileLinks.size() > conf.getFileLinkQueueSize())
			flush();
	}
	
	public synchronized Link getFileLink()
	{
		Link rc = null;
		
		if(fileLinks.size() > 0)
		{
			rc = fileLinks.first();
			fileLinks.remove(rc);
		}
		return rc;
	}
	 
	public synchronized void flush() throws SpiderDataException
	{
		DBFile.getInstance().addFileLinks(fileLinks);
		fileLinks.clear();
	}
	
	//write remaining files if any exist
	public void finalize()
	{
	    try
	    {
		flush();
	    } catch (SpiderDataException e)
	    {
		Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
	    }
	}
}
