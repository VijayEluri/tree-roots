package parser;

import java.util.Collection;
import java.util.TreeSet;

import dataType.Link;


public class LinkParser
{
	//creates links from input strings 
	public static TreeSet<Link> createLinks(Link parentPage, Collection<String> linkStrings)
	{
		//if linkStrings is null then exit, no work to be done
		if(linkStrings == null)
			return null;
		
		TreeSet<Link> links = new TreeSet<Link>();
		
		Link tem;
		//String url;
		//go through the link strings and create links if possible
		for(String linkString : linkStrings)
		{
			//skip link if its empty
			if(linkString.isEmpty() || linkString.startsWith("#"))
				continue;

			//if parent page is null then these links shouldn't be resolved
			if(parentPage != null)
				//resolve the link using the parent page
				tem = parentPage.resolve(linkString);
			else
				tem = new Link(linkString);
					
			//if link isn't null then set the relevance and add it
			if(tem != null && tem.isValid())
			{
			    links.add(tem);
			}
		}
		
		return links;
	}

	
}
