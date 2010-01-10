package spiderInfoOutput;

import java.util.Collection;

import logger.Logger;
import dataType.SpiderInfo;
import db.DBSpiderStatus;
import exception.SpiderDataException;

public class DBOutput implements Output
{
	private boolean firstRun = true; 
	
	public void output(String manager, Collection<SpiderInfo> info)
	{
		//write spider info to DB
		try
		{
			
			//if first time through wipe the old spiderinfo from the DB
			if(firstRun)
			{
				DBSpiderStatus.getInstance().removeOldSpiderInfoManager(manager);
				firstRun = false;
			}
			DBSpiderStatus.getInstance().writeAllSpiderInfo(manager, info);
			
		} catch (SpiderDataException e)
		{
			Logger.log(1, this.getClass().getSimpleName(), "output", "writing spider info failed: " + e.toString());
		}
			
	}

}
