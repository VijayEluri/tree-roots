package parser;

import java.util.Collection;
import java.util.TreeSet;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class LinkContextHandler extends DefaultHandler
{
	private TreeSet<String> linkStrings;

	public LinkContextHandler()
	{
		// create tree set to hold links
		linkStrings = new TreeSet<String>();
	}

	//return the linkStrings and empty internal collection
	public Collection<String> removeLinkStrings() 
	{
		//TODO: is there a better way?
		//old way
	    //Collection<String> rc = (Collection<String>) linkStrings.clone();
	    //linkStrings = new TreeSet<String>();
	    //return rc;
	    //
	    
	    //new way
		Collection<String> rc;
	    rc = linkStrings;
	    linkStrings = new TreeSet<String>();
	    return rc;
	    //
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	{
		if (!qName.equals("a"))
			return;

		for (int i = 0; i < attributes.getLength(); i++)
			if (attributes.getQName(i).equals("href"))
				linkStrings.add(attributes.getValue(i));
	}
}
