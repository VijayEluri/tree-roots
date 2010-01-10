package config;

import java.io.File;

//this class reads the config.xml and creates the other configuration classes
public class ConfigFactory
{
	ConfigSearch configSearch;
	ConfigHypeM configHypeM;
	ConfigSearchGoogle configSearchGoogle;
	ConfigManager configManager;
	ConfigDB configDB;
	ConfigQueue configQueue;
	ConfigFilter configFilter;
	ConfigRetrieval configRetrieval;
	ConfigAnalyzer configAnalyzer;
	
	public ConfigFactory(File f)
	{
		//parse the config file and create config objects
		configSearch = new ConfigSearch();
		configManager = new ConfigManager();
		configDB = new ConfigDB();
		configQueue = new ConfigQueue();
		configFilter = new ConfigFilter();
		configRetrieval = new ConfigRetrieval();
		configAnalyzer = new ConfigAnalyzer();
		configHypeM = new ConfigHypeM();
		configSearchGoogle = new ConfigSearchGoogle();
		
		configFilter.setFilteredFileTypes(new String[] {".pdf",".jpg",".gif",".png",".bmp",".rar", ".zip", ".z7", ".doc", ".flv", ".ppt", ".avi", ".mov", ".tar", ".odf", ".xls",".csv",".txt",".xml", ".jpeg"});
		//TODO: read configuration from xml or properties file
		//set some configuration values temporarily
		configSearch.setSpiderCount(3);
		configSearch.setDomainPageLimitMultiplier(5);
		configSearch.setFileTypes("mp3,flac,ogg,m4a,mp4");
		configSearch.setLinkQueueSize(200);
		configSearch.setPageTimeout(2000);
		configSearch.setMaxHttpConnections(30);
		configSearch.setUserAgentString("Mozilla/5.0 (compatible; MSIE 7.0; Windows 2000)");
		configSearch.setMaxPageRetrievalSize(150 * 10000);// in characters
		configSearch.setTimeBeforeStalled(1000 * 60 * 25);
		configSearch.setConfFilter(configFilter);
		configSearch.setTimeBetweenChecks(1000 * 60 * 10);
		
		configHypeM.setSpiderCount(1);
		configHypeM.setLinkQueueSize(10);
		configHypeM.setPageTimeout(2000);
		configHypeM.setMaxHttpConnections(30);
		configHypeM.setDomainPageLimitMultiplier(10);
		configHypeM.setMaxPageRetrievalSize(150 * 10000);// in characters
		configHypeM.setTimeBeforeStalled(1000 * 60 * 15);
		configHypeM.setTimeBetweenCrawls(3 * 60 * 60 * 1000); // in mills
		configHypeM.setDomainRelevance(1);

		configSearchGoogle.setSpiderCount(1);
		configSearchGoogle.setDomainRelevance(2);
		configSearchGoogle.setSearchResultsPageDepth(6);
		configSearchGoogle.setPageTimeout(2000);
		configSearchGoogle.setMaxHttpConnections(30);
		configSearchGoogle.setMaxPageRetrievalSize(150 * 10000);// in characters
		configSearchGoogle.setTimeBeforeStalled(1000 * 60 * 15);
		configSearchGoogle.setTimeBetweenChecks(1000 * 60 * 10);
		
		configAnalyzer.setSpiderCount(1);
		configAnalyzer.setTemStorageDir("tem");
		configAnalyzer.setIsLinux(true);
		configAnalyzer.setMusicIPKey("7772a970718480a4ee79f6690d7808bb");
		configAnalyzer.setLastFMKey("8dce1fb4463e63647e4a0c4a333e6216");
		
		configRetrieval.setSpiderCount(1);
		configRetrieval.setMinimumOperatingStorageSpace(1000 * 1000 * 100); //in bytes
		configRetrieval.setMaxFileRetrievalSize(7000);
		configRetrieval.setTemStorageDir("tem");
		configRetrieval.setPageTimeout(2000);
		configRetrieval.setMaxHttpConnections(30);
		configRetrieval.setUserAgentString("Mozilla/5.0 (compatible; MSIE 7.0; Windows 2000)");
		
		configManager.setManagerName("local");
		configManager.setPollingInterval(1000 * 180); //in mills

		//derby
		//configDB.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
		//configDB.setConnectionString("jdbc:derby:spiderDB;create=true");
		
		//mysql
		configDB.setDriver("com.mysql.jdbc.Driver");
		configDB.setConnectionString("jdbc:mysql://192.168.1.101/mixtree?user=mixtree&password=conflic1");
		configDB.setDriverForLocal("com.mysql.jdbc.Driver");
		configDB.setConnectionStringForLocal("jdbc:mysql://192.168.1.101/mixtree?user=mixtree&password=conflic1");
		
		//postgreSQL
		//configDB.setDriver("org.postgresql.Driver");
		//configDB.setConnectionString("jdbc:postgresql://localhost:5432/spider?user=spider&password=spider");
		
		//configDB.setConnectionPoolSize(5);
		//configDB.setConnectionCreationWaitInterval(3000);
		configDB.setMaxDomainLength(300);
		configDB.setMaxUrlLength(700);
		
		configQueue.setLinkRetrieveCount(configSearch.getSpiderCount() * 2);
		configQueue.setNewLinkQueueSize(50);
		configQueue.setFileLinkQueueSize(0);
		configQueue.setFileRetrievalCount(50);
		configQueue.setAudioLinkQueueCapacity(0);
		configQueue.setFileAnalysisQueueCapacity(50);
	}
	
	public ConfigSearch getConfigSearch() {return configSearch;}
	public ConfigHypeM getConfigHypeM() {return configHypeM;}
	public ConfigSearchGoogle getConfigSearchGoogle() {return configSearchGoogle;}
	public ConfigManager getConfigManager() {return configManager;}
	public ConfigDB getConfigDB() {return configDB;}
	public ConfigQueue getConfigQueue() {return configQueue;}
	public ConfigFilter getConfigFilter() {return configFilter;}
	public ConfigRetrieval getConfigRetrieval() {return configRetrieval;}
	public ConfigAnalyzer getConfigAnalyzer()	{return configAnalyzer;}
}
