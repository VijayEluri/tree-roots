package urlFilter;

import java.util.ArrayList;
import java.util.Collection;

import config.ConfigFilter;
import dataType.Link;

public class FilterByRatio implements FilterInt
{
    int ratio;

    public FilterByRatio(ConfigFilter conf)
    {
	   ratio = conf.getRatio();
    }

    // filters url strings based on a ratio of the total
    public Collection<Link> filter(String domainName, Collection<Link> links)
    {
	   ArrayList<Link> remove = new ArrayList<Link>();

	   int i = 0;
	   for (Link link : links)
	   {
		  if (!domainName.equals(link.getDomainName()) && i % ratio != 0)
			 remove.add(link);
		  i++;
	   }

	   links.removeAll(remove);

	   return links;
    }

}
