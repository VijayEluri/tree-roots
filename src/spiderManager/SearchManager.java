package spiderManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

import logger.Logger;
import spiderFactory.SpiderFactory;
import spiders.HypeMSpider;
import spiders.SearchSpider;
import spiders.SpiderAbs;
import config.ConfigFactory;
import config.ConfigFilter;
import config.ConfigHypeM;
import config.ConfigSearch;
import config.ConfigSearchGoogle;
import dataType.SpiderInfo;

public class SearchManager implements ManagerInt
{
	private ArrayList<SpiderAbs> spiders;
	private ArrayList<SpiderInfo> spidersInfo;
	private ThreadGroup threadGroup;

	private ConfigFactory configFactory;
	private ConfigSearch configSearch;
	private ConfigFilter configFilter;
	private SpiderFactory spiderFactory;

	private final String THREAD_GROUP = "search group";

	public void init(SpiderFactory spiderFactory, ConfigFactory confFactory)
	{
		threadGroup = new ThreadGroup(THREAD_GROUP);
		spiders = new ArrayList<SpiderAbs>();
		spidersInfo = new ArrayList<SpiderInfo>();

		this.spiderFactory = spiderFactory;

		// get the config objects from the factory that we need
		configFilter = confFactory.getConfigFilter();
		configSearch = confFactory.getConfigSearch();
		configFactory = confFactory;

		SpiderAbs spiderHolder;
		// create and configure search spiders
		for (int i = 0; i < configSearch.getSpiderCount(); i++)
		{
			// create the spider using the id as the thread name
			// based on the search and filter configurations
			spiderHolder = (SearchSpider) spiderFactory.getSpider(configSearch, threadGroup, Integer.toString(i + 1));

			// jvm will close if only daemon is running
			spiderHolder.setDaemon(true);
			spiderHolder.getInfo().setId(i + 1);

			// add spiderInfo to the spidersInfo Array list for polling later
			spidersInfo.add(spiderHolder.getInfo());
			spiders.add(spiderHolder);

			spiderHolder.start();
		}

		if (configFactory.getConfigHypeM().getSpiderCount() > 0)
		{
			ConfigHypeM confHypeM = confFactory.getConfigHypeM();
			// create spider
			spiderHolder = spiderFactory.getSpider(confHypeM, threadGroup, "hypey");
			spiderHolder.getInfo().setId(99);
			// add spiderInfo to the spidersInfo Array list for polling later
			spidersInfo.add(spiderHolder.getInfo());
			spiders.add(spiderHolder);
			spiderHolder.start();
		}
		
		if (configFactory.getConfigSearchGoogle().getSpiderCount() > 0)
		{
			ConfigSearchGoogle confGoogle = confFactory.getConfigSearchGoogle();
			// create spider
			spiderHolder = spiderFactory.getSpider(confGoogle, threadGroup, "google");
			spiderHolder.getInfo().setId(98);
			// add spiderInfo to the spidersInfo Array list for polling later
			spidersInfo.add(spiderHolder.getInfo());
			spiders.add(spiderHolder);
			spiderHolder.start();
		}
	}

	// manage the active spiders and make sure no threads have stalled
	public void manage()
	{
		// set the cut off time in
		long updateCutoff = Calendar.getInstance().getTimeInMillis() - configSearch.getTimeBeforeStalled();

		// Earliest to temporarily hold the new spiders
		ArrayList<SpiderAbs> newSpiders = new ArrayList<SpiderAbs>();

		// check to see if any threads have died and restart if they have
		for (int i = 0; i < spiders.size(); i++)
		{
			// get a search spider from the collection
			SpiderAbs spider = (SpiderAbs) spiders.get(i);

			// find any stalled threads and replace them with new ones
			if (configSearch.getTimeBeforeStalled() > 0 && spider.getLastIteration() != 0 && spider.getLastIteration() < updateCutoff)
			{
				System.out.println("restarting spider: " + spider.getInfo().getId() + " which stalled at: " + spider.getInfo().getStatus() + " for: " + spider.getInfo().getDomainName());
				// remove the spider from the active spider collection
				spiders.remove(i);
				// tell that spider to end. Even though it wont do anything
				// since its stalled
				spider.end();

				// wait half a second for thread to die
				try
				{
					spider.join(500);
				} catch (InterruptedException e)
				{
					Logger.log(0, "SearchManager", "manage", "couldn't preform join on stalled thread: " + e.getMessage());
				}

				// remove the spider info from the info collection
				spidersInfo.remove(spider.getInfo());

				// save the id to assign to new spider
				int spiderId = spider.getInfo().getId();
				String spiderClass = spider.getClass().getSimpleName();
				// create a new spider to replace the stalled one
				spider = spiderFactory.getSpider(configFactory, spiderClass, threadGroup, spider.getName());

				// set the id of the new spider to match the one its replacing
				spider.getInfo().setId(spiderId);

				// add the spider to the collection of new spiders
				newSpiders.add(spider);
				// add the spiderInfo to the spiderInfo collection
				spidersInfo.add(spider.getInfo());

				// start the new thread
				spider.start();
			}
		}

		// add the new spiders to the active collection
		spiders.addAll(newSpiders);
	}

	public Collection<SpiderInfo> poll()
	{
		// get update the status of the spiders info
		for (SpiderAbs s : spiders)
			s.updateInfo();

		// return the spidersInfo
		return spidersInfo;
	}

	public void stop()
	{
		// stop each thread
		for (SpiderAbs s : spiders)
			s.getInfo().setRunning(false);
	}

	// returns the spiders that this manager is responsible for
	public Collection<SpiderAbs> getSpiders()
	{
		return spiders;
	}

}
