package spiderFactory;

import httpRetrieval.*;
import spiders.*;
import config.*;

public class SpiderFactory
{
	private HttpRetrievalInt http;
	
	public SpiderFactory()
	{
		http = null;
	}
	
	//create a spider based on the config object given
	public SpiderAbs getSpider(Object conf, ThreadGroup threadGroup, String name)
	{
	
		if(http == null && conf instanceof ConfigHttpRetrieval)
		{
			http = HttpRetrievalApache.getInstance();
			http.init((ConfigHttpRetrieval)conf);
		}

		SpiderAbs rc = null;
		String classType = conf.getClass().getSimpleName();
		//create the applicable spider based on the config that wass passed in
		if(classType.equals("ConfigSearch"))
			rc = new SearchSpider((ConfigSearch)conf, threadGroup, name, http);
		else if(classType.equals("ConfigRetrieval"))
			rc = new RetrievalSpider((ConfigRetrieval)conf, threadGroup, name, http);
		else if(classType.equals("ConfigAnalyzer"))
			rc = new AnalyzeSpider((ConfigAnalyzer)conf, threadGroup, name);
		else if(classType.equals("ConfigHypeM"))
			rc = new HypeMSpider((ConfigHypeM)conf, threadGroup, name, http);
		else if(classType.equals("ConfigSearchGoogle"))
			rc = new SearchGoogleSpider((ConfigSearchGoogle)conf, threadGroup, name, http);
		return rc; 
	}
	
	public SpiderAbs getSpider(ConfigFactory conf, String spiderType, ThreadGroup threadGroup, String name)
	{
		SpiderAbs rc = null;
		
		//create the applicable spider based on the config that was passed in
		if(spiderType.equals("SearchSpider"))
			rc = getSpider(conf.getConfigSearch(), threadGroup, name);
		else if(spiderType.equals("RetrievalSpider"))
			rc = getSpider(conf.getConfigRetrieval(), threadGroup, name);
		else if(spiderType.equals("AnalyzerSpider"))
			rc = getSpider(conf.getConfigAnalyzer(), threadGroup, name);
		else if(spiderType.equals("HypeMSpider"))
			rc = getSpider(conf.getConfigHypeM(), threadGroup, name);
		else if(spiderType.equals("SearchGoogleSpider"))
			rc = getSpider(conf.getConfigSearchGoogle(), threadGroup, name);
		return rc; 
	}
}
