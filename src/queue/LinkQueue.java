package queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import logger.Logger;
import config.ConfigQueue;
import dataType.Link;
import db.DBLink;
import exception.SpiderDataException;


//singleton that interacts with the DB
public class LinkQueue
{
	private static final LinkQueue linkQueue = new LinkQueue();
	private ArrayList<Link> links;
	private HashMap<String, Link> newLinks;
	private ConfigQueue conf;
	
	private LinkQueue()
	{
		links = new ArrayList<Link>();
		newLinks = new HashMap<String, Link>();
	}
	
	public void init(ConfigQueue conf) {this.conf = conf;}
	
	public static LinkQueue getInstance()
	{
		return linkQueue;
	}
	
	//thread safe method that returns if there are more links
	public synchronized boolean isEmpty()
	{
		if(links.size() == 0)
		{
			try
			{
				links = DBLink.getInstance().getLinksAndDelete(conf.getLinkRetrieveCount());
			} catch (SpiderDataException e)
			{
				Logger.log(1, e.getClazz(), e.getMeth(), e.getMessage());
			}
			//if still empty then there are no more links 
			if(links.size() == 0)
				return true;
		}
		
		return false;
	}
	
	//thread safe method that pulls links from DB for local spidering
	public synchronized Link getLink() throws SpiderDataException
	{	
		ArrayList<Link> rc;
		if(links.size() == 0)
			links = DBLink.getInstance().getLinksAndDelete(conf.getLinkRetrieveCount());

		if(links.isEmpty())
			return null;
		else
			return links.remove(0);
	}
	
	//adds an array of links to newlinks hash.  wont flush till entire
	//array is added
	public synchronized void addLinks(Collection<Link> links) throws SpiderDataException
	{
		// iterate through links and add each one thats for a unique domain
		for(Link l : links)
			if(!newLinks.containsKey(l.getDomainName()))
				newLinks.put(l.getDomainName(), l);
		
		//check if newLinks queue is full
		if(newLinks.size() > conf.getNewLinkQueueSize())
		{
			flush();
		}
	}
	
	//checks if newLinks queue is full and flushes the links to the db if its over the max size
	private synchronized void flush() throws SpiderDataException
	{
		//if it is write links to data source
		DBLink.getInstance().addNewLinks(newLinks.values());
		newLinks.clear();
	}
	
	//write remaining links to db
	//unlock domains
	//synchronized?
	@Override
	public synchronized void finalize()
	{
		if(newLinks.size() != 0)
			try
			{
				//write the new links to the db
				flush();
				//write links that wern't used to db
				DBLink.getInstance().addNewLinks(links);
			} catch(SpiderDataException e)//there was a data error
			{
				Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
			}
	}
}
