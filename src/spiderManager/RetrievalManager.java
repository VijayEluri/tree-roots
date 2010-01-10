package spiderManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import logger.Logger;
import queue.FileAnalysisQueue;
import queue.FileRetrievalQueue;
import spiderFactory.SpiderFactory;
import spiders.SpiderAbs;
import config.ConfigFactory;
import config.ConfigRetrieval;
import dataType.SpiderInfo;

//Manager for the RetrievalSpiders
//checks the system to see that the current file count is less then the max and
//the system still has storage space.
public class RetrievalManager implements ManagerInt
{
	private ConfigRetrieval conf;
	private ArrayList<SpiderAbs> spiders;
	private ArrayList<SpiderInfo> spidersInfo;
	private File temStorageDir;
	private ThreadGroup threadGroup;

	private final String THREAD_GROUP = "retreival group";

	public void init(SpiderFactory spiderFactory, ConfigFactory confFactory)
	{
		temStorageDir = null;

		// create the thread group for the retrieval spider threads
		threadGroup = new ThreadGroup(THREAD_GROUP);

		// set the configRetrieval
		conf = confFactory.getConfigRetrieval();
		temStorageDir = new File(conf.getTemStorageDir());

		// Initialise the queue
		FileRetrievalQueue.getInstance().init(confFactory.getConfigQueue());

		// check if the dir for temp storage exists and if not create
		if (!temStorageDir.exists())
			temStorageDir.mkdirs();

		// check if filepath is a dir that can be used for tem storage
		if (!temStorageDir.isDirectory())
			Logger.log(1, this.getClass().getSimpleName(), "init", "specified dir path isn't a directory");
		
		//clean the old files from this directory
		for(File oldFile : temStorageDir.listFiles())
			oldFile.delete();
		File warningFile = new File(temStorageDir.toString() + File.pathSeparator + "This Dir Gets Erased");
		try
		{
			warningFile.createNewFile();
		} catch (IOException e)
		{
			Logger.log(1, this.getClass().getSimpleName(), "init", "couldn't create warning file");
		}

		// Initalize the spider and spider info collections
		spiders = new ArrayList<SpiderAbs>();
		spidersInfo = new ArrayList<SpiderInfo>();

		// create retrieval threads if the local directory exists and start them
		if (temStorageDir != null)
		{
			for (int i = 0; i < conf.getSpiderCount(); i++)
			{
				// get a spider from the factory setting the configuration,
				// threadgroup and name
				SpiderAbs spiderHolder = spiderFactory.getSpider(conf, threadGroup, Integer.toString(i));
				// add spiders to the spider collection
				spiders.add(spiderHolder);
				// set spider id
				spiderHolder.getInfo().setId(i + 1);
				// add spiderInfo to the spidersInfo collection
				spidersInfo.add(spiderHolder.getInfo());

				// start the spider
				spiderHolder.start();
			}
		}
	}

	// this method is manages the waking of retrievalSpider threads
	// based on the amount of work that is availiable and the current
	// file count
	public void manage()
	{
		//make sure there are files to return
		if (!FileRetrievalQueue.getInstance().isQueueEmpty())
		{
			int wakeThreadCount = threadWakeCount(conf);
			// check to see that there is storage space availiable and the max files
			// and max thread counts hasn't been reached
			if (wakeThreadCount > 0)
			{
				// notify interrupted threads
				int wokenThreads = 0, j = 0;
				while (wokenThreads < wakeThreadCount && j < spiders.size())
				{
					SpiderAbs tem = spiders.get(j);

					if (tem.getState().equals(Thread.State.WAITING))
					{
						synchronized (tem)
						{
							tem.getInfo().setRunning(true);
							tem.notify();
						}
						// increment waken thread count
						wokenThreads++;
					}
					j++;
				}
			}
		}

	}

	// this calculates the number of threads that should be woken up
	// based on the availiable work. Its not thread safe and will thus
	// cause more then the max files to be created. This isn't critical in
	// the scope of the app so I'm fine with that.
	protected int threadWakeCount(ConfigRetrieval conf)
	{
		// check if the systems free memory is within the desired range
		if (temStorageDir.getFreeSpace() > conf.getMinimumOperatingStorageSpace())
		{
			// get the remaining storage capacity
			int remainingSpace = FileAnalysisQueue.getInstance().getRemainingQueueCapacity();

			if (remainingSpace > 0)
			{
				// get the number of threads that could be notified based on the
				// total running thread count
				int remainingSpiderThreadCapacity = conf.getSpiderCount() - getRunningThreads();

				// return the lesser of the remaining space or the remaining spider thread capacity
				return remainingSpace > remainingSpiderThreadCapacity ? remainingSpiderThreadCapacity : remainingSpace;
			}
		}

		return 0;
	}

	// this returns the number of running threads
	private int getRunningThreads()
	{
		int rc = conf.getSpiderCount();
		for (SpiderAbs s : spiders)
			if (s.getState().equals(Thread.State.WAITING))
				rc--;

		return rc;
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
