package spiderManager;

import java.util.Collection;

import spiderFactory.SpiderFactory;
import spiders.SpiderAbs;
import config.ConfigFactory;
import dataType.SpiderInfo;

public interface ManagerInt
{
	public void manage();
	public Collection<SpiderInfo> poll();
	public void init(SpiderFactory spiderFactory, ConfigFactory confFactory);
	public Collection<SpiderAbs> getSpiders();
	public void stop();
}
