package urlFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import dataType.Link;


public class FilterByAnchors implements FilterInt
{
	public Collection<Link> filter(String domainName, Collection<Link> links)
	{
		int lastBackSlash, lastHash;
		
		for(Link l : links)
		{
			//compaire the positions of the last slash with the last #
			//to see if the link is an anchor link
			lastBackSlash = l.getUrl().lastIndexOf('/');
			lastHash = l.getUrl().lastIndexOf('#');
			if(lastBackSlash < lastHash)
				try
				{
					l.setURI(new URI(l.getUrl().substring(0, lastHash)));
				} catch (URISyntaxException e)
				{
					
				}
		}
		return links;
	}

}
