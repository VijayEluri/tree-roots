package config;

public class ConfigHypeM extends ConfigSearch
{
	private long timeBetweenCrawls;
	private int domainRelevance;

	public void setTimeBetweenCrawls(long timeBetweenCrawls)
	{
		this.timeBetweenCrawls = timeBetweenCrawls;
	}

	public long getTimeBetweenCrawls()
	{
		return timeBetweenCrawls;
	}

	public void setDomainRelevance(int domainRelevance)
	{
		this.domainRelevance = domainRelevance;
	}

	public int getDomainRelevance()
	{
		return domainRelevance;
	}
}
