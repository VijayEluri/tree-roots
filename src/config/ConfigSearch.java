package config;

import java.util.StringTokenizer;

public class ConfigSearch extends ConfigHttpRetrieval
{
	private int spiderCount;
	private int linkQueueSize;
	private int pageQueueSize;
	private int domainPageLimitMultiplier;
	private String[] fileTypes;
	private long timeBeforeStalled;
	private ConfigFilter confFilter;
	private long timeBetweenChecks;

	public void setTimeBetweenChecks(long timeBetweenCrawls)
	{
		this.timeBetweenChecks = timeBetweenCrawls;
	}

	public long getTimeBetweenChecks()
	{
		return timeBetweenChecks;
	}

	public int getLinkQueueSize()
	{
		return linkQueueSize;
	}

	public void setLinkQueueSize(int linkQueueSize)
	{
		this.linkQueueSize = linkQueueSize;
	}

	public int getPageQueueSize()
	{
		return pageQueueSize;
	}

	public void setPageQueueSize(int pageQueueSize)
	{
		this.pageQueueSize = pageQueueSize;
	}

	public int getDomainPageLimitMultiplier()
	{
		return domainPageLimitMultiplier;
	}

	public void setDomainPageLimitMultiplier(int domainPageLimit)
	{
		this.domainPageLimitMultiplier = domainPageLimit;
	}

	public void setFileTypes(String fileTypes)
	{
		StringTokenizer st = new StringTokenizer(fileTypes, ",");

		this.fileTypes = new String[st.countTokens()];

		for (int i = 0; st.hasMoreTokens(); i++)
			this.fileTypes[i] = st.nextToken();

	}

	public String[] getFileTypes()
	{
		return fileTypes;
	}

	public int getSpiderCount()
	{
		return spiderCount;
	}

	public void setSpiderCount(int spiderCount)
	{
		this.spiderCount = spiderCount;
	}

	public void setTimeBeforeStalled(long timeBeforeStalled)
	{
		this.timeBeforeStalled = timeBeforeStalled;
	}

	public long getTimeBeforeStalled()
	{
		return timeBeforeStalled;
	}

	public void setConfFilter(ConfigFilter confFilter)
	{
		this.confFilter = confFilter;
	}

	public ConfigFilter getConfFilter()
	{
		return confFilter;
	}
}
