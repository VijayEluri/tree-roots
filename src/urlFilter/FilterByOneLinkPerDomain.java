package urlFilter;

import java.util.ArrayList;
import java.util.Collection;

import dataType.Link;


public class FilterByOneLinkPerDomain implements FilterInt
{
    //limits the number of links per domain returned
    //dosn't limit links for current domain
    public Collection<Link> filter(String domainName, Collection<Link> links)
    {
	   ArrayList<Link> remove = new ArrayList<Link>();
	   ArrayList<String> domainNames = new ArrayList<String>();

	   for (Link link : links)
		  if (!domainNames.contains(link.getDomainName()))
			 domainNames.add(link.getDomainName());
		  else if(! domainName.equals(link.getDomainName()))
			 remove.add(link);
	   
	   links.removeAll(remove);

	   return links;
    }

}
