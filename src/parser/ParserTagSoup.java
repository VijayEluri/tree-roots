package parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import logger.Logger;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ParserTagSoup implements ParserInt
{
	private Parser p;
	private LinkContextHandler linkContextHandler;

	public ParserTagSoup()
	{
		p = new Parser();
		linkContextHandler = new LinkContextHandler();
		p.setContentHandler(linkContextHandler);
	}

	public Collection<String> parse(Reader page)
	{
		// TODO
		// need to find out if just recreating the parser or the
		// linkContextHandler will
		// work or if it needs to be both
		//Parser p = new Parser();
		//linkContextHandler = new LinkContextHandler();
		//p.setContentHandler(linkContextHandler);
		//

		try
		{
			
			//create an input source to parse from the stream
			InputSource pageInputSource = new InputSource(page);
			
			// parse page
			p.parse(pageInputSource);
			
			return linkContextHandler.removeLinkStrings();
		} catch (IOException e)
		{

			Logger.log(0, this.getClass().getSimpleName(), "parse",
					"input error, couldn't parse page: " + e.toString());
		} catch (SAXException e)
		{
			Logger.log(0, this.getClass().getSimpleName(), "parse",
					"SAX exception, tag soup couldn't parse page: "
							+ e.toString());
		}

		return null;
	}

}
