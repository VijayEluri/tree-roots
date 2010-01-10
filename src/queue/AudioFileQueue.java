package queue;

import java.util.ArrayList;

import config.ConfigQueue;
import dataType.AudioLink;
import db.DBAudio;
import db.DBAudioLink;
import exception.SpiderDataException;

public class AudioFileQueue
{
	private static final AudioFileQueue audioFileQueue = new AudioFileQueue();
	ArrayList<AudioLink> audioLinks;
	ConfigQueue conf;
	
	public void init(ConfigQueue conf)
	{
		this.conf = conf;
		
		//create the storage for the queue
		audioLinks = new ArrayList<AudioLink>();
	}
	
	public static AudioFileQueue getInstance() {return audioFileQueue;}
	
	public synchronized void addAudioLink(AudioLink audioLink) throws SpiderDataException
	{
		audioLinks.add(audioLink);
		
		if(audioLinks.size() >= conf.getAudioLinkQueueCapacity())
			flush();
	}
	
	public synchronized void flush() throws SpiderDataException
	{
		//write audio links to db
		DBAudioLink.getInstance().addAudioLinks(audioLinks);
		//increment the audio file counts
		DBAudio.getInstance().incrementAudioFileCount(audioLinks);
		//empty queue
		audioLinks.clear();
	}
}
