package queue;

import java.util.ArrayList;
import java.util.Collection;

import logger.Logger;
import config.ConfigQueue;
import dataType.Link;
import db.DBFile;
import exception.SpiderDataException;

public class FileRetrievalQueue
{
	private static ArrayList<Link> fileLinks;
	private static final FileRetrievalQueue fileRetrievalQueue = new FileRetrievalQueue();
	private ConfigQueue conf;

	private FileRetrievalQueue()
	{
		fileLinks = new ArrayList<Link>();
	}

	public void init(ConfigQueue conf)
	{
		this.conf = conf;
	}

	public static FileRetrievalQueue getInstance()
	{
		return fileRetrievalQueue;
	}

	public synchronized boolean isQueueEmpty()
	{
		if(fileLinks.isEmpty())
			try
			{
				//if queue is empty attempt to fill it
				fillFileLinks();
			} catch (SpiderDataException e)
			{
				Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
			}
		
		return fileLinks.isEmpty();
	}

	// This will return a Link from either the FileQueue which contains links retrieved
	// from the spidering process or if that queue is empty then it will get a link
	// from the DB
	public synchronized Link getFileLink() throws SpiderDataException
	{
		Link rc = null;

		// if fileLinks is empty refill it
		if (fileLinks.isEmpty())
			fillFileLinks();

		// if the db and file queue is empty the last retrieval will fail
		// and this method will return null
		if (fileLinks.size() > 0)
			rc = fileLinks.remove(0);

		return rc;
	}

	// this function pulls the files from the fileQueue or the DB and fills the queue
	private synchronized void fillFileLinks() throws SpiderDataException
	{
		Collection<Link> files = new ArrayList<Link>();

		// take the files from the file queue if possible
		boolean fileQueueIsEmpty = false;
		for (int i = 0; i < conf.getFileRetrievalCount() && !fileQueueIsEmpty; i++)
		{
			// request a file from the fileQueue
			Link tem = FileQueue.getInstance().getFileLink();
			// if the fileQueue returns a file add it to the file collection
			if (tem != null)
				files.add(tem);
			else
				fileQueueIsEmpty = true;
		}
		// pull a number of files from DB specified in the config
		if (conf.getFileRetrievalCount() > files.size())
			// if the files collection has links in it from the fileQueue then only retrieve the difference in files
			files.addAll(DBFile.getInstance().getLinksAndDelete(conf.getFileRetrievalCount() - files.size()));

		// add the retrieved links to the queue
		fileLinks.addAll(files);
	}

	// write remaining files if any exist
	public void finalize()
	{
		try
		{
			// write remaining fileLinks to DB
			DBFile.getInstance().addFileLinks(fileLinks);
		} catch (SpiderDataException e)
		{
			Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
		}
	}
}
