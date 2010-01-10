package spiders;

import java.io.File;
import java.util.ArrayList;

import logger.Logger;
import mediaAnalyzers.JAudioTaggerFileTagAnalyzer;
import mediaAnalyzers.LastFMAnalyzer;
import mediaAnalyzers.MediaAnalyzerInt;
import mediaAnalyzers.MyId3TagAnalizer;
import queue.AudioFileQueue;
import queue.FileAnalysisQueue;
import config.ConfigAnalyzer;
import dataType.AudioLink;
import dataType.FileLink;
import exception.SpiderDataException;

// This spider analyzes files to determine what they are.
// The thread will wait if no new files exist to be analyzed
// and relies on the Analyze manager to notify it when there
// is work to be done.
// After the file is analyzed the this spider will delete the
// file from the fileSystem
public class AnalyzeSpider extends SpiderAbs
{

	private ConfigAnalyzer conf;
	private File temStorageDir;
	private ArrayList<MediaAnalyzerInt> mediaAnalyzers;

	public AnalyzeSpider(ConfigAnalyzer conf, ThreadGroup threadGroup, String name)
	{
		super(threadGroup, name);
		this.conf = conf;

		temStorageDir = new File(conf.getTemStorageDir());

		// check if the dir for temp storage exists and if not create
		if (!temStorageDir.exists())
			temStorageDir.mkdirs();

		// check if filepath is a dir that can be used for tem storage
		if (!temStorageDir.isDirectory())
			Logger.log(1, this.getClass().getSimpleName(), "init", "specified dir path isn't a directory");

		// initilize the media analyzers
		mediaAnalyzers = new ArrayList<MediaAnalyzerInt>();
		mediaAnalyzers.add(new JAudioTaggerFileTagAnalyzer(conf));
		mediaAnalyzers.add(new MyId3TagAnalizer(conf));

		// set initial status
		info.setType("Analyze");
		info.setStatus("waiting to start");
		updateInfo();
	}

	// start the analyze spider
	public void run()
	{
		File currentFile;
		AudioLink currentLink = null;

		super.run();

		// main execution loop
		while (isRunning())
		{
			try
			{
				// get a link to analize, will return null if there isn't one availiable
				FileLink tem = FileAnalysisQueue.getInstance().getLocalFileLink();
				// if tem is not null then create an AudioLink based on it
				if (tem != null)
					currentLink = new AudioLink(tem);
				else 
					//else current link is null, no more work to do
					currentLink = null;
				
				if (currentLink != null)
				{
					// get the current file from the currentLink
					currentFile = new File(temStorageDir.getPath() + temStorageDir.separator + currentLink.getFileName());

					// set the info.domain to the current links domain
					info.setDomainName(currentLink.getDomainName());

					boolean analysisComplete = false;
					// analyse the file
					for (int i = 0; i < mediaAnalyzers.size(); i++)
						analysisComplete |= mediaAnalyzers.get(i).analyze(currentFile, currentLink);

					// check to see if the file was successfully analysed
					if (analysisComplete)
						// write analysed file to queue
						AudioFileQueue.getInstance().addAudioLink(currentLink);
					else
					{
						// TODO: move the file to the unknown file table?
						// probably not
						Logger.logDebug("didn't add: " + currentLink);
					}

					// delete the file
						currentFile.delete();

				} else
				{
					// no work to be done so wait
					// relies on the manager to notify
					try
					{
						synchronized (this)
						{
							wait();
						}
					} catch (InterruptedException e)
					{
						Logger.log(2, this.getClass().getSimpleName(), "run", e.getMessage());
					}
				}

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
}
