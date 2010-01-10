package urlFilter;

import java.util.ArrayList;
import java.util.Collection;

import dataType.Link;


public class FilterByValid implements FilterInt
{
    //removes all invalid links 
    public  Collection<Link>  filter(String domainName, Collection<Link> links)
    {
	   ArrayList<Link> remove = new ArrayList<Link>();
	   
	   for(Link link : links)
		  if(!link.isValid())
			 remove.add(link);

	  links.removeAll(remove);
	   
	   return links;
    }

}
