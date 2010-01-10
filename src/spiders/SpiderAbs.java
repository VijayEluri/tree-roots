package spiders;

import java.util.Calendar;

import dataType.SpiderInfo;



public abstract class SpiderAbs extends Thread
{
	protected SpiderInfo info;
	
	public SpiderAbs(ThreadGroup threadGroup, String name)
	{
		super(threadGroup, name);
		info = new SpiderInfo();
	}
	
	//runs this spider thread
	@Override
	public void run()
	{
		info.setRunning(true);
	}
	
	//signals the end of this spider thread
	public void end()
	{
		info.setRunning(false);
	}
	
	//returns if the thread is still running
	public boolean isRunning() {return info.isRunning();}
	
	//returns the spiderInfo object for this spider
	public SpiderInfo getInfo() {return info;}
	
	public void updateInfo()
	{
		info.setLastChecked(Calendar.getInstance().getTime());
	}

	public long getLastIteration()
	{
		return 0l;
	}
}
