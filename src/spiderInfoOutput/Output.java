package spiderInfoOutput;

import java.util.Collection;

import dataType.SpiderInfo;


public interface Output
{
	public void output(String manager, Collection<SpiderInfo> info);
}
