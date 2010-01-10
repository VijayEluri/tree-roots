package config;

public class ConfigSearchGoogle extends ConfigSearch
{
	private int domainRelevance;
	private int searchResultsPageDepth;
	
	public void setDomainRelevance(int domainRelevance)
	{
		this.domainRelevance = domainRelevance;
	}

	public int getDomainRelevance()
	{
		return domainRelevance;
	}

	public void setSearchResultsPageDepth(int searchResultsPageDepth)
	{
		this.searchResultsPageDepth = searchResultsPageDepth;
	}

	public int getSearchResultsPageDepth()
	{
		return searchResultsPageDepth;
	}
}
