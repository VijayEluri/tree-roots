package config;

//configures an httpRetrieval object
public abstract class ConfigHttpRetrieval
{
    private long pageTimeout;
    private int maxHttpConnections;
    private long maxPageRetrievalSize;
    private long maxFileRetrievalSize;
    private String userAgentString;
    
    public long getPageTimeout()
    {
	   return pageTimeout;
    }

    public int getMaxHttpConnections()
    {
	   return maxHttpConnections;
    }

    public void setMaxHttpConnections(int maxHttpConnections)
    {
	   this.maxHttpConnections = maxHttpConnections;
    }

    public void setPageTimeout(long pageTimeout)
    {
	   this.pageTimeout = pageTimeout;
    }

    public void setMaxPageRetrievalSize(long maxPageRetrievalSize)
    {
	   this.maxPageRetrievalSize = maxPageRetrievalSize;
    }

    public long getMaxPageRetrievalSize()
    {
	   return maxPageRetrievalSize;
    }

	public void setMaxFileRetrievalSize(long maxFileRetrievalSize)
	{
		this.maxFileRetrievalSize = maxFileRetrievalSize;
	}

	public long getMaxFileRetrievalSize()
	{
		return maxFileRetrievalSize;
	}

	public void setUserAgentString(String userAgentString)
	{
		this.userAgentString = userAgentString;
	}

	public String getUserAgentString()
	{
		return userAgentString;
	}
}
