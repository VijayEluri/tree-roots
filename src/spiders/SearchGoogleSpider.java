package spiders;

import httpRetrieval.HttpRetrievalInt;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Random;
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
import urlFilter.FilterByGoogle;
import urlFilter.FilterByOneLinkPerDomain;
import urlFilter.FilterByValid;
import urlFilter.FilterInt;
import urlFilter.Filters;
import config.ConfigSearchGoogle;
import config.ConfigSearch;
import dataType.Link;
import db.DBAudio;
import exception.SpiderDataException;

public class SearchGoogleSpider extends SpiderAbs
{
	private ConfigSearchGoogle conf;
	private String domainName;
	private int domainRelevance;
	private Filters filters;
	private ParserInt parser;
	private HttpRetrievalInt http;
	private String[] queries;
	private int curQuery;
	private Random rand;

	private long lastIteration;

	public SearchGoogleSpider(ConfigSearchGoogle conf, ThreadGroup threadGroup, String name, HttpRetrievalInt http)
	{
		super(threadGroup, name);
		info.setType("SearchGoogle");
		info.setStatus("waiting to start");
		this.conf = conf;
		this.http = http;

		// create parser
		parser = new ParserTagSoup();

		// update the last iteration time;
		updateLastIteration();

		domainRelevance = conf.getDomainRelevance();

		queries = new String[2];
		queries[0] = "http://blogsearch.google.ca/blogsearch?hl=en&ie=UTF-8&q=<artist>&sa=N&start=<page>";
		queries[1] = "http://www.google.ca/search?hl=en&safe=off&q=music+mp3+<artist>&start=<page>";

		rand = new Random(Calendar.getInstance().getTimeInMillis());

		filters = new Filters();
		filters.addFilter(new FilterByGoogle());
		filters.addFilter(new FilterByOneLinkPerDomain());
		filters.addFilter(new FilterByExternalDomains());

		curQuery = -1;
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
				// check if the link queue is empty is under the min
				// if no
				// reset the page count
				// then go to sleep
				// if yes
				// get an existing artists name
				// select the next query builder
				// build the query
				// fetch and parse the page
				// add the links to the queue(only one per domain)
				// increment current page count
				// repeat

				if (LinkQueue.getInstance().isEmpty())
				{
					Collection<String> linkStrings = null;
					// get an artists name

					Link curLink = new Link();
					String query;
					// spider the current google search to the configured depth
					for (int i = 0; i < conf.getSearchResultsPageDepth(); i++)
					{
						// select a google search query at random
						if (curQuery == -1)
							curQuery = rand.nextInt(queries.length);

						Logger.logDebug("querying for an artist name");
						// get a random artists name
						String artistName = DBAudio.getInstance().getRandomArtistName();

						// build google query
						query = queries[curQuery].replace("<artist>", artistName);
						query = query.replace("<page>", Integer.toString(i * 10));
						
						Logger.logDebug("query: " + query);

						curLink = new Link(query);
						if (curLink != null)
						{
							curLink.setDomainRelevance(domainRelevance);
							try
							{
								linkStrings = http.retrievePageAndParseLinks(curLink, parser, info);
							} catch (SpiderDataException e)
							{
								Logger.log(0, e.getClazz(), e.getMeth(), "getting and parsing page: " + e.getMessage());
							}

							if (linkStrings != null && !linkStrings.isEmpty())
							{
								info.setStatus("8");
								Collection<Link> newLinks = LinkParser.createLinks(curLink, linkStrings);
								linkStrings = null;

								info.setStatus("9");

								// filter the unwanted links out
								filters.filter(curLink.getDomainName(), newLinks);

								// set the domain relevance for each child link
								for (Link link : newLinks)
									link.setDomainRelevance(domainRelevance - 1);

								// add the new links and files to the appropriate queue
								LinkQueue.getInstance().addLinks(newLinks);
							}
						}
					}
					// set the curQuery to the not set value
					curQuery = -1;
				} else
					// no need to run there are still links
					Logger.logDebug("google search spider: " + info.getId() + " waiting");

				try
				{
					info.setStatus("sleeping");
					sleep(conf.getTimeBetweenChecks());
					info.setStatus("working");
				} catch (InterruptedException e)
				{
					Logger.logDebug("google search spider(" + info.getId() + ")couldn't sleep: " + e.getMessage());
				}

				// update the last iteration time
				updateLastIteration();

			} catch (SpiderDataException e)// there was a data error so you must
			// exit
			{
				Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
				info.setRunning(false);
				info.setAsError();
			}
			/*
			 * catch (Exception e) { Logger.log(2, "SpiderSearch", "run: " + info.getId(), e.getMessage()); info.setRunning(false); info.setAsError(); }
			 */
		}

		// write the final status to spiderInfo
		info.setAsCompleted();
	}

	@Override
	public void updateInfo()
	{
		super.updateInfo();
		info.setDomainName(domainName);
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
