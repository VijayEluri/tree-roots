package urlFilter;

import java.util.Collection;
import java.util.Iterator;

import dataType.Link;


public class FilterByExternalDomains implements FilterInt
{

	@Override
	public Collection<Link> filter(String domainName, Collection<Link> links)
	{
		Iterator<Link> it = links.iterator();
		Link tem;
		while(it.hasNext())
		{
			tem = it.next();
			if(tem.getDomainName() == null || domainName.equalsIgnoreCase(tem.getDomainName()))
				it.remove();
		}

		return links;
	}

}
