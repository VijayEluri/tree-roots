package spiders;

import httpRetrieval.HttpRetrievalInt;

import java.util.*;


import logger.Logger;
import parser.*;
import queue.*;
import urlFilter.*;
import dataType.Link;
import config.ConfigSearch;
import exception.SpiderDataException;

public class SearchSpider extends SpiderAbs
{
	private ConfigSearch conf;
	private int domainPageCount;
	private int domainPageLimit;
	private String domainName;
	private int domainRelevance;
	private TreeSet<Link> links;
	private TreeSet<Link> newLinks;
	private TreeSet<Link> newFiles;
	private ParserInt parser;
	private HttpRetrievalInt http;
	private Filters filters;
	
	private Link curLink;

	private long lastIteration;
	// this holds hash's of all visited links, this will lead to links being
	// ignored
	// that havn't been parsed but it is pretty significant
	private int[] visited;

	public SearchSpider(ConfigSearch conf, ThreadGroup threadGroup, String name, HttpRetrievalInt http)
	{
		super(threadGroup, name);
		info.setType("Search");
		info.setStatus("waiting to start");
		this.conf = conf;
		links = new TreeSet<Link>();
		newLinks = new TreeSet<Link>();
		newFiles = new TreeSet<Link>();
		this.http = http;

		// create parser
		parser = new ParserTagSoup();

		// update the last iteration time;
		updateLastIteration();

		domainRelevance = -1;
		
		//create filters based on config
		filters = new Filters();
		filters.addFilter(new FilterByFileType(conf.getConfFilter()));
		filters.addFilter(new FilterByValid());
		filters.addFilter(new FilterByOneLinkPerDomain());
		filters.addFilter(new FilterByGoogle());
	}

	@Override
	public void run()
	{
		super.run();

		// set the spiderInfo status to running
		info.setStatus("running");
		// until run is false keep spidering
		while (isRunning())
		{
			try
			{
				info.setStatus("1");
				// if the link array is empty or we've reached the max num of
				// spidered pages request a new domain
				if (links.size() == 0 || domainPageCount >= domainPageLimit)
				{
					// get new links
					Link newLink = LinkQueue.getInstance().getLink();
					if(newLink != null)
						links.add(newLink);
					
					//if no links were returned sleep for the configured durration
					if(links.isEmpty())
					{
						try
						{
							sleep(conf.getTimeBetweenChecks());
						} catch (InterruptedException e)
						{
							Logger.logDebug("search spider("+ info.getId() +")couldn't sleep: " + e.getMessage() );
						}
						//start iteration again
						continue;
					}
					

					// set all the domain related info
					domainName = links.first().getDomainName();
					domainRelevance = links.first().getDomainRelevance();
					domainPageCount = 0;
					domainPageLimit = conf.getDomainPageLimitMultiplier() * (domainRelevance > 0 ? domainRelevance : 1);
					//this list holds the links we've been to already for this domain
					visited = new int[domainPageLimit];
					info.setStatus("1.5");
				}

				info.setStatus("2");
				// grab a link from the links array list
				curLink = links.first();
				links.remove(curLink);

				info.setStatus("4");
				// request the link strings from the file from the httpRetrieval
				// object if the link is valid
				Collection<String> linkStrings = null;
				if (curLink.isValid())
					try
					{
						linkStrings = http.retrievePageAndParseLinks(curLink, parser, info);
					} catch (SpiderDataException e)
					{
						Logger.log(0, e.getClazz(), e.getMeth(), "getting and parsing page: " + e.getMessage());
					}
					
				// set page as visited now to avoid adding it to the link queue
				// if a link back to the same page is found or the page can't be found
				visited[domainPageCount] = curLink.hashCode();
				// increment local domain page counter
				domainPageCount++;
					
				info.setStatus("6");

				if (linkStrings != null)
				{

					info.setStatus("7");

					info.setStatus("8");
					Collection<Link> linksFound = LinkParser.createLinks(curLink, linkStrings);
					info.setStatus("9");
					linkStrings = null;

					// apply url Filters if they have been set
					if (filters != null)
						linksFound = filters.filter(domainName, linksFound);

					info.setStatus("10");
					String newLinkUrl;
					String newLinkSuffex;
					int newLinkHashCode;
					boolean newLinkWasVisited;
					
					// if the link queue is half empty then we should fill it
					boolean refillLinks = links.size() < conf.getLinkQueueSize() / 2;
					
					info.setStatus("11");
					// determine what the link is for and where it needs to go if anywhere
					for (Link newLink : linksFound)
					{
						//get the link url or skip if there is an issue with it
						try {newLinkUrl = newLink.getUrl();} catch (NullPointerException e){continue;}

						//get the file  extension if it exists
						int startOfSuffex = newLinkUrl.lastIndexOf('.');
						//check if its one we care about
						if (startOfSuffex != -1)
						{
							// get the suffex and clean for comparison
							newLinkSuffex = newLinkUrl.substring(startOfSuffex + 1).toLowerCase().trim();
							//check if its one that were looking for
						
							for (int i = 0; i < conf.getFileTypes().length; i++)
								if (conf.getFileTypes()[i].equals(newLinkSuffex))
								{
									// this is a file type were looking for so add it to the list and end this itteration
									newFiles.add(newLink);
									continue;
								}
						}
						
						// since the link isn't pointing to a file type were looking for we need to figure out what kind of link it is and if we want it
						//first check if its for our current domain or for a different one
						if(newLink.getDomainName().equals(domainName))
						{
							//since this link is for the current domain we check if we need to refill the queue and if not we skip it
							if(refillLinks)
							{
								//verify that we havn't already been to the url described by the link
								newLinkHashCode = newLink.hashCode();
								newLinkWasVisited = false;
								for (int i = 0; i < domainPageCount; i++)
									if (visited[i] == newLinkHashCode)
									{
										newLinkWasVisited = true;
										continue;
									}

								//if the new link wasn't visited we can add it to the list
								if(!newLinkWasVisited)
								{
									//since this link will be spidered later we need to set its relevance
									newLink.setDomainRelevance(domainRelevance);
									links.add(newLink);
								}
								continue;
							}
							continue;
						}
						
						//since this link must be for a different domain we first check if our current domain is relevant enough to warrant adding this link to the queue
						if(domainRelevance > 0)
						{
							//this link is from a different domain so its relevance will be one less then the current
							newLink.setDomainRelevance(domainRelevance - 1);
							newLinks.add(newLink);
						}
					}

					info.setStatus("12");

					// dereference links found to free up resources
					linksFound = null;

					// add the new links and files to the appropriate queues
					if(newLinks.size() > 0)
						LinkQueue.getInstance().addLinks(newLinks);
					if(newFiles.size() > 0)
						FileQueue.getInstance().addFileLinks(newFiles);
					//clear the link lists out and start again
					newLinks.clear();
					newFiles.clear();

					info.setStatus("14");
				}

				// update the last itteration time
				updateLastIteration();

			} catch (SpiderDataException e)// there was a data error so you must
			// exit
			{
				Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
				info.setRunning(false);
				info.setAsError();
			}
			/*
			 * catch (Exception e) { Logger.log(2, "SpiderSearch", "run: " +
			 * info.getId(), e.getMessage()); info.setRunning(false);
			 * info.setAsError(); }
			 */
		}

		// write remaining newLinks to the Queue
		try
		{
			LinkQueue.getInstance().addLinks(newLinks);
			LinkQueue.getInstance().addLinks(links);
		} catch (SpiderDataException e)
		{
			Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
			info.setRunning(false);
		}

		// write the final status to spiderInfo
		info.setAsCompleted();
	}

	@Override
	public void updateInfo()
	{
		super.updateInfo();
		info.setDomainName(domainName + ":" + domainPageCount + "/" + domainPageLimit);
	}

	public long getLastIteration()
	{
		return lastIteration;
	}

	public void updateLastIteration()
	{
		lastIteration = Calendar.getInstance().getTimeInMillis();
	}
}
