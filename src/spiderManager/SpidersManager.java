package spiderManager;

import java.io.File;
import java.util.ArrayList;

import logger.Logger;
import queue.*;
import spiderFactory.SpiderFactory;
import spiderInfoOutput.*;
import config.*;
import db.*;

public class SpidersManager
{
	public static void main(String[] args)
	{
		File confFile = new File("F:\\spider\\config.xml");

		ConfigFactory confFactory = new ConfigFactory(confFile);
		ConfigManager configManager = confFactory.getConfigManager();
		SpiderFactory spiderFactory = new SpiderFactory();

		//configure connection pool and DOA's
		ConnectionManager.getInstance().init(confFactory.getConfigDB());
		DBFile.getInstance().init(confFactory.getConfigDB());
		DBLink.getInstance().init(confFactory.getConfigDB());
		
		//configure the queue objects
		LinkQueue.getInstance().init(confFactory.getConfigQueue());
		FileQueue.getInstance().init(confFactory.getConfigQueue());
		FileRetrievalQueue.getInstance().init(confFactory.getConfigQueue());
		FileAnalysisQueue.getInstance().init(confFactory.getConfigQueue());
		AudioFileQueue.getInstance().init(confFactory.getConfigQueue());
		
		// create output arraylist
		ArrayList<Output> output = new ArrayList<Output>();
		output.add(new DBOutput());

		// create managers
		ArrayList<ManagerInt> managers = new ArrayList<ManagerInt>();
		//only bring up spider managers if they have spiders to manage
		//TODO: take the hypeM part out of if
		if(confFactory.getConfigSearch().getSpiderCount() > 0 || confFactory.getConfigHypeM().getSpiderCount() > 0  || confFactory.getConfigSearchGoogle().getSpiderCount() > 0)
			managers.add(new SearchManager());
		if(confFactory.getConfigRetrieval().getSpiderCount() > 0)
			managers.add(new RetrievalManager());
		if(confFactory.getConfigAnalyzer().getSpiderCount() > 0)
			managers.add(new AnalyzerManager());
		
		// initilize all managers
		for (ManagerInt m : managers)
			m.init(spiderFactory, confFactory);

		// main loop
		while (true)
		{
			// manage all managers
			for (ManagerInt m : managers)
			{
				// get the managers to manage their spiders
				m.manage();
				
				// poll spider for status
				for (Output o : output)
					// send spider info to output source
					o.output(configManager.getManagerName(), m.poll());			
			}
			
			try
			{
				Thread.sleep(configManager.getPollingInterval());
			} catch (InterruptedException e)
			{
				Logger.log(1, "SpiderManager", "run",
						"couldn't sleep threads after polling");
			}
		}

		// TODO: uncomment this when you have a way to exit the loop
		// write spider info to output source one last time
		// for(Output o : output)
		// o.output(conf.getManagerName(), spidersInfo);

		// no threads left so finalize singletons that need it
		// LinkQueue.getInstance().finalize();
		// FileQueue.getInstance().finalize();
		// DomainQueue.getInstance().finalize();
	}
}
