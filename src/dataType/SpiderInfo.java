package dataType;

import java.util.Date;

//this class contains the information about a spider and its state
public class SpiderInfo
{
	private int id;
	private String type;
	private String status;
	private boolean running;
	private String domainName;
	private Date lastChecked;
	
	public String getDomainName()
	{
		return domainName;
	}
	
	public void setDomainName(String domainName)
	{
		this.domainName = domainName;
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		this.type = type;
	}
	public String getStatus()
	{
		return status;
	}
	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setRunning(boolean running)
	{
		this.running = running;
		if(running)
			status = "running";
		else
			status = "stopped";
	}

	public boolean isRunning()
	{
		return running;
	}
	
	public void setAsCompleted()
	{
		running = false;
		status = "finished";
		domainName = "n/a";
	}
	
	public void setAsError()
	{
		setAsCompleted();
		this.status = "error";
	}

	public void setLastChecked(Date lastChecked)
	{
		this.lastChecked = lastChecked;
	}

	public Date getLastChecked()
	{
		return lastChecked;
	}
}
