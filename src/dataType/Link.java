package dataType;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import logger.Logger;

public class Link implements Comparable<Link>
{
	protected URI uri;
	protected String domainName;
	protected int domainRelevance;

	protected boolean valid;

	// the max length thats allowed for the url field
	protected static final int urlMaxLength = 700;
	
	//copy constructor
	public Link(Link l)
	{
		uri = l.getURI();
		domainName = l.getDomainName();
		domainRelevance = l.getDomainRelevance();
		
		valid = l.isValid();
	}

	public Link(String domainName, String url, int domainRelevance)
	{
		if (url.length() > urlMaxLength)
			url = null;
		
		//encode the url to correctly represent special chars (spaces)
		url = encodeURL(url);

		try
		{
			uri = new URI(url);
		} catch (URISyntaxException e)
		{
			Logger.log(1, this.getClass().getSimpleName(), "Link(url)",
					"link couldn't be created: " + e.toString());
		}
		
		this.domainName = domainName;
		this.domainRelevance = domainRelevance;
		valid = true;
	}
	
	public Link(String url)
	{
		this();

		if (url.length() > urlMaxLength)
			url = null;
		
		//encode the url to correctly represent special chars (spaces)
		url = encodeURL(url);

		try
		{
			uri = new URI(url);
		} catch (URISyntaxException e)
		{
			Logger.log(1, this.getClass().getSimpleName(), "Link(url)",
					"link couldn't be created: " + e.toString());
		}
		initDomainName();
	}

	public Link(URI uri)
	{
		this();
		setURI(uri);
	}

	public Link()
	{
		valid = true;
	}

	protected void initDomainName()
	{
		// if the uri isn't null get its domainName
		if (uri != null)
			domainName = uri.getHost();

		// get the domains
		if (domainName != null)
		{
			domainName = domainName.toLowerCase();
			
			//remove www. prefix if it exists
			if(domainName.startsWith("www."))
				domainName = domainName.substring(4);

			//check if there still is a domain left
			if(domainName.isEmpty())
				valid = false;
			
		} else
		{
			domainName = "";
			valid = false;
		}
	}

	public boolean isValid()
	{
		return valid;
	}

	public URI getURI()
	{
		return uri;
	}

	public void setURI(URI uri)
	{
		// if the url length is less then the max then set it
		// if not then we can't use it so reset to default values
		if (uri.toString().length() < urlMaxLength)
		{
			this.uri = uri;
		} else
			uri = null;

		initDomainName();
	}

	public String getUrl()
	{
		if (uri != null)
			return uri.toString();
		else
			return "";
	}

	// returns the domain name. Will remove the www. prefix if it exists
	public String getDomainName()
	{
		return domainName;
	}
	
	public int getDomainRelevance()
	{
		return domainRelevance;
	}

	public void setDomainRelevance(int domainRelevance)
	{
		this.domainRelevance = domainRelevance;
	}

	public Link resolve(String url)
	{
		URI tem = null;
		try
		{
			tem = uri.resolve(url);
		} catch (Exception e)
		{
			// this could happen if the uri is bad
			// it will be handled by returning null
			return null;
		}

		if (tem != null)
			return new Link(tem);
		else
			return null;
	}

	public int compareTo(Link l)
	{
		int rc = 1;
		int localHash = hashCode();
		int linkHash = l.hashCode();

		if (localHash > linkHash)
			rc = -1;
		else if (localHash == linkHash)
			rc = 0;

		return rc;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean rc = false;

		try
		{
			if (obj != null && ((Link) obj).getUrl().equals(getUrl()))
				rc = true;
		} catch (ClassCastException e)
		{}// if not a link it will return false

		return rc;
	}

	public int hashCode()
	{
		// if(domainId == null)
		String url = getUrl();
		if (url != null)
			return url.hashCode();
		else
			return 0;
		// else
		// return domainId.hashCode() ^ getUrl().hashCode();
	}
	
	//TODO: this is insufficent
	//encode the url to correctly represent special chars (spaces)
	protected String encodeURL(String url)
	{
		return url.replace(" ", "%20");
		/*String protocol = null;
		
		//make sure url isn't empty
		if(url == null || url.isEmpty())
			return;

		//get the protocol
		if(url.contains("://"))
		{
			int protocolPos =  url.indexOf("://") + 3;
			protocol = url.substring(0, protocolPos);
			url = url.substring(protocolPos);
		}
		
		//encode the url
		String [] urlParts = url.split("/");
		String encodedUrl;
		try
		{
			//encode each part of the url
			for(String part : urlParts)
				encodedUrl = URLEncoder.encode(part, "UTF-8") + "/";

		} catch (UnsupportedEncodingException e)
		{
			Logger.log(2, this.getClass().getSimpleName(), "url couldn't be encoded",
					"for:" + url + " error: " + e.toString());
		}
		
		if(protocol != null)
			url = protocol + url;*/
	}
}
