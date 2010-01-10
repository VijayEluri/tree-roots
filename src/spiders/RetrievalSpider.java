package spiders;

import httpRetrieval.HttpRetrievalInt;

import java.io.File;
import java.util.Date;

import logger.Logger;
import queue.FileAnalysisQueue;
import queue.FileRetrievalQueue;
import config.ConfigRetrieval;
import dataType.FileLink;
import dataType.Link;
import exception.SpiderDataException;

//this spider retrieves files from the internet to a set folder.
//The file url come from either the file queue or the database.
//these files can then be analysed and deleted by another spider thread
public class RetrievalSpider extends SpiderAbs
{
	private ConfigRetrieval conf;
	private File temStorageDir;
	private HttpRetrievalInt http;

	// create a retrieval spider which will be a thread the specified thread
	// group and will have
	// the given name and will conform to the given configuration
	public RetrievalSpider(ConfigRetrieval conf, ThreadGroup threadGroup,
			String name, HttpRetrievalInt http)
	{
		super(threadGroup, name);
		this.conf = conf;

		// the retrieval manager is responsible for checking that the tmp
		// directory should have been checked for existence and availability
		temStorageDir = new File(conf.getTemStorageDir());

		// set initial status
		info.setType("Retrieval");
		info.setStatus("waiting to start");
		updateInfo();

		this.http = http;
	}

	// start the retreivalSpider
	public void run()
	{
		super.run();
		
		String fileName;
		String fileExtension;
		Link linkToFile;
		FileLink fileLink; 
		
		info.setStatus("0");
		// main execution loop
		while (isRunning())
		{
			try
			{
				info.setStatus("1");
				// get file from queue
				linkToFile = FileRetrievalQueue.getInstance().getFileLink();

				// if file link is null then there arn't anymore files
				// ready for retrieval/analysis so skip to the end and wait
				if (linkToFile != null)
				{
					// set the current domain
					info.setDomainName("file id:" + linkToFile.getDomainName());

					info.setStatus("2");

					// get the file extension from the url
					fileExtension = linkToFile.getUrl().substring(linkToFile.getUrl().lastIndexOf('.'));
					// create the local file name based on the current time and the retrieval id
					fileName = info.getId() + "-" + new Date().getTime();
					
					// create file to write into
					File fileToWriteTo = new File(temStorageDir.getAbsolutePath(), fileName + ".part");
					info.setStatus("3");

					// retrieve file and store it in the tem directory
					try
					{
						// this method will also update the links url if it was redirected
						http.getFile(linkToFile, fileToWriteTo, info);
					} catch (Exception e)
					{
						Logger.log(0, "RetrievalSpider", "run", "getFile: "
								+ e.getMessage());
					}

					info.setStatus("4.9");

					// if the file size is at or above the max then the file was
					// retrieved successfully, if less its either too small
					// or was removed
					Logger.logDebug("RetruevalSpider file size " + fileToWriteTo.length() + " for: " + linkToFile.getUrl());
					if (fileToWriteTo.length() >= conf.getMaxFileRetrievalSize())
					{
						// rename the file with the proper extension now that
						// the transfer is complete
						File renamedFile = new File(temStorageDir, fileName + fileExtension);
						fileToWriteTo.renameTo(renamedFile);
						
						// add file name and link to the queue for the analyzer to pick up
						fileLink = new FileLink(renamedFile.getName(), linkToFile);
						FileAnalysisQueue.getInstance().addLocalFileLink(fileLink);
						
					} else
					{
						Logger.logDebug("the file(" + fileToWriteTo.getAbsolutePath() + ") size was under the configured min so it will be removed");
						// too small something is wrong so delete
						fileToWriteTo.delete();
					}
				}
				
				// Interrupt the thread if there is no more work
				// to be done b/c either the queue is full or there 
				// are no more links to retrieve
				if (FileAnalysisQueue.getInstance().isQueueFull() || linkToFile == null)
				{
					// set the thread as not running
					info.setRunning(false);
					info.setStatus("5");
					try
					{
						synchronized (this)
						{
							Logger.logDebug("retrieval spider: " + info.getId() + " waiting");
							wait();
						}
					} catch (InterruptedException e)
					{
						Logger.log(2, "RetrievalSpider", "Run", e.getMessage());
					}

				}

				info.setStatus("6");
			} catch (SpiderDataException e)// there was a data error so you must
			// exit
			{
				Logger.log(2, e.getClazz(), e.getMeth(), e.getMessage());
				info.setRunning(false);
				info.setAsError();
			}
		}

		// set final status
		info.setAsCompleted();
	}

	@Override
	public void updateInfo()
	{
		super.updateInfo();
	}

}
