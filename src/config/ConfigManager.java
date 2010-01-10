package config;

public class ConfigManager
{
	private String managerName;
	private long pollingInterval;
	
	public String getManagerName()
	{
		return managerName;
	}
	public void setManagerName(String managerName)
	{
		this.managerName = managerName;
	}
	public long getPollingInterval()
	{
		return pollingInterval;
	}
	public void setPollingInterval(long pollingInterval)
	{
		this.pollingInterval = pollingInterval;
	}
}
