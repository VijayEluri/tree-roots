package urlFilter;

import java.util.Collection;
import java.util.Iterator;

import dataType.Link;

public class FilterByGoogle implements FilterInt
{

	/**
	 * Filters all links with the word google in them
	 */
	@Override
	public Collection<Link> filter(String domainName, Collection<Link> links)
	{

		String url;
		Iterator it = links.iterator();
		while(it.hasNext())
		{
			url = ((Link) it.next()).getUrl().toLowerCase();
			if(url.indexOf("google") >= 0)
				it.remove();
		}
		return links;
	}

}
