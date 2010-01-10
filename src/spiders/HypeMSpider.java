package spiders;

import httpRetrieval.HttpRetrievalInt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeSet;

import logger.Logger;
import parser.LinkParser;
import parser.ParserInt;
import parser.ParserTagSoup;
import queue.FileQueue;
import queue.LinkQueue;
import urlFilter.FilterByExternalDomains;
import urlFilter.FilterByFileType;
import urlFilter.FilterByOneLinkPerDomain;
import urlFilter.FilterByValid;
import urlFilter.Filters;
import config.ConfigHypeM;
import config.ConfigSearch;
import dataType.Link;
import exception.SpiderDataException;

public class HypeMSpider extends SpiderAbs
{
	private final String domainName = "hypem.com";
	private final String domainRoot = "http://hypem.com/";

	private ConfigHypeM conf;
	private int domainPageCount;
	private int domainPageLimit;
	private int linksDomainRelevance;
	private Collection<Link> linksFound;
	private ParserInt parser;
	private HttpRetrievalInt http;
	private Filters filters;
	private int numberOfRuns;

	// this contains the first link from the last iteration.
	// its used as a marker of where to stop.
	private String FirstLinkFromLastIteration;
	private String FirstLinkFromThisIteration;
	private boolean reachedLastIterationStartPoint;

	public HypeMSpider(ConfigHypeM conf, ThreadGroup threadGroup, String name, HttpRetrievalInt http)
	{
		super(threadGroup, name);
		info.setType("HypeMSearch");
		info.setStatus("waiting to start");
		this.conf = conf;
		this.http = http;

		// create parser
		parser = new ParserTagSoup();

		// create filters ignoring the config
		filters = new Filters();
		filters.addFilter(new FilterByValid());
		filters.addFilter(new FilterByExternalDomains());
		filters.addFilter(new FilterByOneLinkPerDomain());

		domainPageLimit = conf.getDomainPageLimitMultiplier();
		domainPageCount = 0;
		linksDomainRelevance = conf.getDomainRelevance() - 1;

		// flag that tracks when we have reached the end of the previous crawl
		reachedLastIterationStartPoint = false;
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
				// check if we've reached the max num of spidered pages
				if (reachedLastIterationStartPoint || domainPageCount >= domainPageLimit)
				{
					// we've reached the max domain page count so
					// sleep for a given amount of time until the
					// manager wakes us up

					// set the thread as not running
					info.setRunning(false);
					//update the sipders info
					updateInfo();

					//increment the number of runs
					numberOfRuns++;

					try
					{
						synchronized (this)
						{
							sleep(conf.getTimeBetweenCrawls());
						}
					} catch (InterruptedException e)
					{
						Logger.log(2, "RetrievalSpider", "Run", e.getMessage());
					}


					info.setRunning(true);

					reachedLastIterationStartPoint = false;
					FirstLinkFromLastIteration = FirstLinkFromThisIteration;
					FirstLinkFromThisIteration = null;
					domainPageCount = 0;
				}

				info.setStatus("4");
				// request the link strings from the file from the httpRetrieval
				// object if the link is valid
				Collection<String> linkStrings = null;
				try
				{
					linkStrings = http.retrievePageAndParseLinks(new Link(domainRoot + domainPageCount), parser, info);
				} catch (SpiderDataException e)
				{
					Logger.log(0, e.getClazz(), e.getMeth(), "getting and parsing page: " + e.getMessage());
				}

				// increment local domain page counter
				domainPageCount++;

				info.setStatus("6");

				if (linkStrings != null && !linkStrings.isEmpty())
				{
					if (FirstLinkFromThisIteration == null)
						FirstLinkFromThisIteration = linkStrings.iterator().next();

					// if the FirstLinkFromLastIteration is set then check if we've reached
					// the start of the last crawl
					if (FirstLinkFromLastIteration != null)
					{
						// check if we've reached the first link from the last iteration
						for (String link : linkStrings)
							if (link.equals(FirstLinkFromLastIteration))
								reachedLastIterationStartPoint = true;
					}

					info.setStatus("8");
					linksFound = LinkParser.createLinks(null, linkStrings);

					linkStrings = null;

					// apply url Filters if they have been set
					if (filters != null)
						linksFound = filters.filter(domainName, linksFound);

					// set the domain relevance for each link
					for (Link l : linksFound)
						l.setDomainRelevance(linksDomainRelevance);

					info.setStatus("12");

					// add the new links the queues
					if (linksFound.size() > 0)
						LinkQueue.getInstance().addLinks(linksFound);
					// clear the link lists out and start again
					linksFound.clear();

					info.setStatus("14");
				}

			} catch (SpiderDataException e)// there was a data error so you must
			// exit
			{
				Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
				info.setRunning(false);
				info.setAsError();
			}

		}

		// write remaining newLinks to the Queue
		try
		{
			LinkQueue.getInstance().addLinks(linksFound);
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
		if (info.isRunning())
			info.setDomainName(domainName + "@" +Calendar.getInstance().getTime() + " : " + numberOfRuns);
	}

}
