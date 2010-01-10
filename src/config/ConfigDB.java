package config;

public class ConfigDB
{
	private String driver;
	private String connectionString;
	private String driverForLocal;
	private String connectionStringForLocal;
	private String userName;
	private String passwd;
	private int connectionCreationWaitInterval;
	private int maxDomainLength;
	private int maxUrlLength;

	public String getDriverForLocal()
	{
		return driverForLocal;
	}
	public void setDriverForLocal(String driverForLocal)
	{
		this.driverForLocal = driverForLocal;
	}
	public String getConnectionStringForLocal()
	{
		return connectionStringForLocal;
	}
	public void setConnectionStringForLocal(String connectionStringForLocal)
	{
		this.connectionStringForLocal = connectionStringForLocal;
	}
	
	public int getConnectionCreationWaitInterval()
	{
		return connectionCreationWaitInterval;
	}
	public void setConnectionCreationWaitInterval(int connectionCreationWaitInterval)
	{
		this.connectionCreationWaitInterval = connectionCreationWaitInterval;
	}
	
	private int connectionPoolSize;
	
	public String getDriver()
	{
		return driver;
	}
	public void setDriver(String driver)
	{
		this.driver = driver;
	}
	
	public String getConnectionString()
	{
		return connectionString;
	}
	public void setConnectionString(String connectionString)
	{
		this.connectionString = connectionString;
	}
	public int getConnectionPoolSize()
	{
		return connectionPoolSize;
	}
	public void setConnectionPoolSize(int connectionPoolSize)
	{
		this.connectionPoolSize = connectionPoolSize;
	}
	public String getUserName()
	{
		return userName;
	}
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	public String getPasswd()
	{
		return passwd;
	}
	public void setPasswd(String passwd)
	{
		this.passwd = passwd;
	}
	public void setMaxDomainLength(int maxDomainNameLength)
	{
		this.maxDomainLength = maxDomainNameLength;
	}
	public int getMaxDomainLength()
	{
		return maxDomainLength;
	}
	public void setMaxUrlLength(int maxUrlLength)
	{
		this.maxUrlLength = maxUrlLength;
	}
	public int getMaxUrlLength()
	{
		return maxUrlLength;
	}
	
}
