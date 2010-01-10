package config;

public class ConfigAnalyzer
{
	protected boolean isLinux;
	protected String TemStorageDir;
	protected int spiderCount;
	protected String musicIPKey;
	protected String lastFMKey;
	
	public String getLastFMKey()
	{
		return lastFMKey;
	}
	public void setLastFMKey(String lastFMKey)
	{
		this.lastFMKey = lastFMKey;
	}
	
	public void setIsLinux(boolean isLinux) {this.isLinux = isLinux;}
	public boolean getIsLinux() {return isLinux;}
	
	public String getMusicIPKey()
	{
		return musicIPKey;
	}
	public void setMusicIPKey(String musicIPKey)
	{
		this.musicIPKey = musicIPKey;
	}

	public int getSpiderCount()
	{
		return spiderCount;
	}

	public void setSpiderCount(int spiderCount)
	{
		this.spiderCount = spiderCount;
	}

	public String getTemStorageDir()
	{
		return TemStorageDir;
	}

	public void setTemStorageDir(String temStorageDir)
	{
		TemStorageDir = temStorageDir;
	}
	
}
