package config;

//configures a retrieval spider
public class ConfigRetrieval extends ConfigHttpRetrieval
{
	private String temStorageDir;
	private int spiderCount;
	private long minimumOperatingStorageSpace;

	public void setTemStorageDir(String temStorageDir)
	{
		this.temStorageDir = temStorageDir;
	}

	public String getTemStorageDir()
	{
		return temStorageDir;
	}

	public void setSpiderCount(int spiderCount)
	{
		this.spiderCount = spiderCount;
	}

	public int getSpiderCount()
	{
		return spiderCount;
	}

	public void setMinimumOperatingStorageSpace(long minimumOperatingStorageSpace)
	{
		this.minimumOperatingStorageSpace = minimumOperatingStorageSpace;
	}

	public long getMinimumOperatingStorageSpace()
	{
		return minimumOperatingStorageSpace;
	}
}
