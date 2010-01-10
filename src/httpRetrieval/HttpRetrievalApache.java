package httpRetrieval;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Collection;

import logger.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import parser.ParserInt;
import config.ConfigHttpRetrieval;
import dataType.Link;
import dataType.SpiderInfo;
import exception.SpiderDataException;

//TODO: remove file stuff, create a stream class that only read a certain number of chars and pass that back instead of a 
//string to the search spider.

//this class retrieves content over http using the apache httpClient
public class HttpRetrievalApache implements HttpRetrievalInt
{
	private static HttpRetrievalApache httpRetrieval = new HttpRetrievalApache();
	private DefaultHttpClient httpClient;
	private ConfigHttpRetrieval conf;
	private ClientConnectionManager cm;
	
	// get a instance of httpRetrieval
	// still need to call init
	public static HttpRetrievalInt getInstance()
	{
		return httpRetrieval;
	}

	// this must be called before class can be used. It can be called multiple
	// times but isn't thread safe
	public void init(ConfigHttpRetrieval conf)
	{
		this.conf = conf;
		
		// if init has already been run then there isn't anything to do
		if (httpClient != null)
			return;

		// Create and initialize HTTP parameters
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, conf
				.getMaxHttpConnections());
		ConnManagerParams.setTimeout(params, conf.getPageTimeout());
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(params, conf.getUserAgentString());

		// Create and initialize scheme registry
		// do I need this?
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		cm = new ThreadSafeClientConnManager(params, schemeRegistry);

		 httpClient = new DefaultHttpClient(cm, params);
		// for testing
		//httpClient = new DefaultHttpClient(params);
		// set retry count to zero
		httpClient
				.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(
						0, false));

		// turn off cookie processing
		httpClient.removeRequestInterceptorByClass(RequestAddCookies.class);
		httpClient.removeResponseInterceptorByClass(ResponseProcessCookies.class);
	}

	private HttpRetrievalApache()
	{

	}

	// public static HttpRetrievalInt getInstance() {return httpRetrieval;}

	// get a html page as a string that corresponds to the link
	public Collection<String> retrievePageAndParseLinks(Link link,
			ParserInt parser, SpiderInfo info) throws SpiderDataException
	{
		return retrieveResource(link, parser, null, info);
	}

	// retrieve a file that corresponds to the link and write it to the
	public void getFile(Link link, File fileToWriteTo, SpiderInfo info)
			throws SpiderDataException
	{
		// get the remote file and write it to the given local file
		retrieveResource(link, null, fileToWriteTo, info);
	}

	// this method returns a page in byte [] form using the apache http client
	private Collection<String> retrieveResource(Link link, ParserInt parser,
			File fileToWriteTo, SpiderInfo info) throws SpiderDataException
	{
		// testing
		// httpClient = null;
		// init(conf);
		// testing
		if (httpClient == null)
			throw new SpiderDataException(
					this.getClass().getSimpleName(),
					"retrieveResource",
					"HttpClient wasn't initilized, need to call init() method of this class",
					"no error thrown");

		Collection<String> rc = null;
		InputStream responseStream = null;
		HttpGet httpGet = null;
		HttpEntity entity = null;
		Reader pageReader = null;
		HttpResponse response = null;

		info.setStatus("4.1");
		try
		{
			// execute the method
			httpGet = new HttpGet(link.getUrl());
			HttpContext context = new BasicHttpContext();
			info.setStatus("4.1.5");

			// TODO: this will sometimes hang
			response = httpClient.execute(httpGet, context);
			info.setStatus("4.2");

			// check to see that the connection was made
			if (response != null)
			{
				// get the response body as an HttpEntity
				entity = response.getEntity();

				info.setStatus("4.2.1");
				
				if (entity != null)
				{
					info.setStatus("4.2.2");
					// get response as a stream
					responseStream = entity.getContent();
					info.setStatus("4.2.3");

					// if the file to write to isn't null then we should
					// retrieve the file and store it locally if not then
					// retrieve the page as a string
					if (fileToWriteTo == null)
					{
						info.setStatus("4.2.4");
						// wrapper the reader to empose configured page read
						// limit
						pageReader = new LimitingInputStreamReader(conf.getMaxPageRetrievalSize(), responseStream);
					} else
					{
						// retrieve the file locally
						retrieveFileAndWriteLocally(responseStream,
								fileToWriteTo, info);
						// deallocate resources and close connection
						httpGet.abort();
					}
					info.setStatus("4.2.5");
					// if responseStream and pageReader arn't null then lets
					// read and parse page to get links using the parser
					if (responseStream != null && pageReader != null)
					{
						info.setStatus("4.2.6");
						Logger.logDebug("reading and parsing page: " + link.getUrl());
						// TODO: this will sometimes hang
						rc = parser.parse(pageReader);
						info.setStatus("4.2.7");

						// abort the connection in case it hadn't reached the
						// eof
						httpGet.abort();
						info.setStatus("4.2.8.1");
						pageReader.close();
						responseStream.close();
						info.setStatus("4.2.8.2");
						
						// deallocate resources and close connection
						entity.consumeContent();
						info.setStatus("4.2.8.3");
						
						// get the url that was loaded. will be different in the
						// case of redirects
						HttpUriRequest finalRequest = (HttpUriRequest) context
								.getAttribute(ExecutionContext.HTTP_REQUEST);
						HttpHost host = (HttpHost) context
								.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

						info.setStatus("4.2.9");
						
						StringBuilder uriString = new StringBuilder();
						uriString.append(host.getSchemeName() + "://");
						uriString.append(host.getHostName());
						if (host.getPort() != -1)
						{
							uriString.append(":" + host.getPort());
						}
						info.setStatus("4.2.9.1");
						uriString.append(finalRequest.getURI().normalize()
								.toString());

						info.setStatus("4.2.9.2");
						URI retrievedLinksURI = new URI(uriString.toString());
						// if the uri is not null and is different then the
						// orriginal then use it as
						// the link source
						if (retrievedLinksURI != null
								&& !retrievedLinksURI.equals(link.getURI()))
						{
							Logger.logDebug("redirected from: "
									+ link.getURI().toString() + " to: "
									+ retrievedLinksURI.toString());
							link.setURI(retrievedLinksURI);
						}

						info.setStatus("4.6");
						Logger.logDebug(rc.size() + " link strings returned");
					}
				}else
					Logger.logDebug("HttpRetrievalApache.retrieveReasource: entity was null");
			}else
				Logger.logDebug("HttpRetrievalApache.retrieveReasource: response was null");
			
		} catch (Exception e)
		{
			// close http connection
			if (httpGet != null)
				httpGet.abort();
			
			// if the response is but the entity isn't
			// set it so we can clean it up properly
			// in the next step
			if(response != null && entity == null)
				entity = response.getEntity();

			if (entity != null)
				// clean up connection
				try
				{
					entity.consumeContent();
				} catch (IOException e1)
				{
					Logger.log(0, "HttpRetrievalApache", "retrieveResource", e1
							.getMessage());
				}

			if (pageReader != null)
				try
				{
					// close streams
					pageReader.close();
				} catch (IOException e1)
				{
					Logger.log(0, "HttpRetrievalApache", "retrieveResource", e
							.getMessage());
				}

			if (responseStream != null)
				try
				{
					// close streams
					responseStream.close();
				} catch (IOException e1)
				{
					Logger.log(0, "HttpRetrievalApache", "retrieveResource", e
							.getMessage());
				}

			Logger.log(0, this.getClass().getSimpleName(), "retrieveReasource",
					"error retrieving link " + link.getUrl() + " error: " + e);
			e.printStackTrace();

			throw new SpiderDataException(this.getClass().getSimpleName(),
					"retrieveReasource", "", e.getMessage());
		}

		info.setStatus("4.6");

		return rc;
	}

	// reads the stream and writes it to the given file
	private void retrieveFileAndWriteLocally(InputStream in,
			File fileToWriteTo, SpiderInfo info) throws IOException
	{
		byte[] read = new byte[5000]; // byte array to read file into
		int byteReadTotal = 0; // the total chars read
		int bytesRead = 0; // the bytes read in that itteration
		boolean shouldRead = true; // if the loop should continue
		FileOutputStream fileOutput = null;
		BufferedOutputStream fileWriter = null;

		try
		{
			// create an outputstream to the file
			fileOutput = new FileOutputStream(fileToWriteTo);
			// buffer the output to the file to increase effency
			fileWriter = new BufferedOutputStream(fileOutput);

			info.setStatus("4.3");
			// TODO: clean this up if it works
			// read responseStream up to the max response size
			while (shouldRead)
			{
				info.setStatus("4.4: " + byteReadTotal);
				// read bytes from stream
				// bytesRead = responseStream.read(read);
				bytesRead = in.read(read);

				if (bytesRead != -1
						&& byteReadTotal < conf.getMaxFileRetrievalSize())
				{
					// write the read data to the file
					fileWriter.write(read, 0, bytesRead);

					// increment the byte count the length that was just
					// written
					byteReadTotal += bytesRead;
				} else
					shouldRead = false;
			}
			
			// flush the remaining data to the file
			fileWriter.flush();
			// close the file
			fileWriter.close();

			Logger.logDebug("HttpRetrievalApache.retrieveFileAndWriteLocally: file size is: " + fileToWriteTo.length());
		} catch (IOException e)
		{
			if (fileOutput != null)
				try
				{
					fileOutput.close();
				} catch (Exception e1)
				{
					Logger.log(0, "HttpRetrievalApache",
							"retrieveFileAndWriteLocally", e1.getMessage());
				}

			if (fileWriter != null)
				try
				{
					fileWriter.close();
				} catch (Exception e1)
				{
					Logger.log(0, "HttpRetrievalApache",
							"retrieveFileAndWriteLocally", e1.getMessage());
				}
		}
		info.setStatus("4.5");
	}
}
