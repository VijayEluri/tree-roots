package urlFilter;

import java.util.ArrayList;
import java.util.Collection;

import config.ConfigFilter;
import dataType.Link;

public class FilterByFileType implements FilterInt
{
    private String [] fileTypes;
    
    public FilterByFileType(ConfigFilter conf)
    {
	   fileTypes = conf.getFilteredFileTypes();   
    }
    
    //filters url strings based on their file type
    public Collection<Link>  filter(String domainName, Collection<Link> links)
    {
	   ArrayList<Link> remove = new ArrayList<Link>();
	   
	   for(Link link : links)
		  for(int i = 0 ; i < fileTypes.length ; i++)
		  {
			 //check if the url is not pointing to a undesired file type
			 if(link.getUrl().toLowerCase().endsWith(fileTypes[i]))
			 {
				//if it is remove it from the collection and end this iteration
				remove.add(link);
				i = fileTypes.length;
			 }
		  }
	   
	   links.removeAll(remove);
	   
	   return links;
    }

}
