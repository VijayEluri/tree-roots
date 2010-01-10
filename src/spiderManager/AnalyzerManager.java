package spiderManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import logger.Logger;
import queue.FileAnalysisQueue;
import spiderFactory.SpiderFactory;
import spiders.SpiderAbs;
import config.ConfigAnalyzer;
import config.ConfigFactory;
import dataType.FileLink;
import dataType.SpiderInfo;

//Manager for the AnalyzeSpiders
//checks the system to see if there are files to be analyzed and
//launches a thread to do that if the max thread count hasn't been 
//reached
public class AnalyzerManager implements ManagerInt
{
	private ConfigAnalyzer conf;
	private ArrayList<SpiderAbs> spiders;
	private ArrayList<SpiderInfo> spidersInfo;
	private ThreadGroup threadGroup;
	private FileLink nextLink;

	private final String THREAD_GROUP = "analyze group";

	public void init(SpiderFactory spiderFactory, ConfigFactory confFactory)
	{
		// create the thread group for the retrieval spider threads
		threadGroup = new ThreadGroup(THREAD_GROUP);

		// set the configRetrieval
		conf = confFactory.getConfigAnalyzer();
		File temStorageDir = new File(conf.getTemStorageDir());

		// check if the dir for temp storage exists and if not create
		if (!temStorageDir.exists())
			temStorageDir.mkdirs();

		// check if filepath is a dir that can be used for tem storage
		if (!temStorageDir.isDirectory())
			Logger.log(1, this.getClass().getSimpleName(), "init", "specified dir path isn't a directory");

		// Initalize the spider and spider info collections
		spiders = new ArrayList<SpiderAbs>();
		spidersInfo = new ArrayList<SpiderInfo>();

		// create analyze threads if the local directory exists and start them
		if (temStorageDir != null)
		{
			for (int i = 0; i < conf.getSpiderCount(); i++)
			{
				// get a spider from the factory setting the configuration,
				// threadgroup and name
				SpiderAbs spiderHolder = spiderFactory.getSpider(conf, threadGroup, Integer.toString(i));
				// set spider id
				spiderHolder.getInfo().setId(i + 1);
				// add spiders to the spider collection
				spiders.add(spiderHolder);
				// add spiderInfo to the spidersInfo collection
				spidersInfo.add(spiderHolder.getInfo());

				// start the spider
				spiderHolder.start();
			}
		}
	}

	// this method is manages the waking of analyzeSpider threads
	// based on the amount of work that is availiable and the current
	// file count
	// Its not Thread safe in that it will start threads when race conditions
	// occur when it shouldn't but the thread it notifies is Threadsafe so that
	// ok.
	public void manage()
	{
		// figure out if there are waiting spiders and there are new files
		int waitingSpiders = 0;

		// check how many spiders are waiting
		for (SpiderAbs s : spiders)
			if (s.getState().equals(Thread.State.WAITING))
				waitingSpiders++;

		if (waitingSpiders > 0)
		{
			for (int i = 0; i < waitingSpiders; i++)
			{
				// there are spiders are waiting and there is work to be done
				// then notify the approperate spiders so they can get
				// back to work
				if (!FileAnalysisQueue.getInstance().isQueueEmpty())
				{
					synchronized (spiders.get(i))
					{

						// restart spider
						spiders.get(i).notify();
					}
				}else
					// since there are no more links in the queue we can't start any more spiders
					break;
			}
		}
	}
	
	public Collection<SpiderInfo> poll()
	{
		// get update the status of the spiders info
		for (SpiderAbs s : spiders)
			s.updateInfo();

		return spidersInfo;
	}

	public void stop()
	{
		// set each thread to not running and wake them so they can stop
		for (SpiderAbs s : spiders)
		{
			s.getInfo().setRunning(false);
			s.notify();
		}
	}

	// returns the spiders that this manager is responsible for
	public Collection<SpiderAbs> getSpiders()
	{
		return spiders;
	}

}
