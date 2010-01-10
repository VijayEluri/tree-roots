package urlFilter;

import java.util.ArrayList;
import java.util.Collection;

import dataType.Link;


public class Filters implements FilterInt
{
    Collection<FilterInt> filters = new ArrayList<FilterInt>();

    // filters from the passed in collection based on the filters that
    // have been added
    public Collection<Link> filter(String domainName, Collection<Link> links)
    {
	   //System.out.print("links: " + links.size());
	   for (FilterInt filter : filters)
	   {
		  int i = links.size();
		  links = filter.filter(domainName, links);
		  //System.out.print(" - " + (i - links.size()));
	   }
	   
	   return links;
    }

    // add a filter
    public void addFilter(FilterInt filter)
    {
	   filters.add(filter);
    }
}
