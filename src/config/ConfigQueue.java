package config;

public class ConfigQueue
{
	private int newLinkQueueSize;
	private int linkQueueSize;
	private int fileLinkQueueSize;
	private int linkRetrieveCount;
	private int fileRetrievalCount;
	private int audioLinkQueueCapacity;
	private int fileAnalysisQueueCapacity;
	
	public int getNewLinkQueueSize()
	{
		return newLinkQueueSize;
	}
	public void setNewLinkQueueSize(int newLinkQueueSize)
	{
		this.newLinkQueueSize = newLinkQueueSize;
	}
	public int getLinkQueueSize()
	{
		return linkQueueSize;
	}
	public void setLinkQueueSize(int linkQueueSize)
	{
		this.linkQueueSize = linkQueueSize;
	}

	public void setLinkRetrieveCount(int linkRetrieveCount)
	{
		this.linkRetrieveCount = linkRetrieveCount;
	}
	public int getLinkRetrieveCount()
	{
		return linkRetrieveCount;
	}
	public void setFileLinkQueueSize(int fileLinkQueueSize)
	{
		this.fileLinkQueueSize = fileLinkQueueSize;
	}
	public int getFileLinkQueueSize()
	{
		return fileLinkQueueSize;
	}
	public void setFileRetrievalCount(int fileRetrievalCount)
	{
		this.fileRetrievalCount = fileRetrievalCount;
	}
	public int getFileRetrievalCount()
	{
		return fileRetrievalCount;
	}
	public void setFileAnalysisQueueCapacity(int fileAnalysisQueueCapacity)
	{
		this.fileAnalysisQueueCapacity = fileAnalysisQueueCapacity;
	}
	public int getFileAnalysisQueueCapacity()
	{
		return fileAnalysisQueueCapacity;
	}
	public void setAudioLinkQueueCapacity(int audioLinkQueueCapacity)
	{
		this.audioLinkQueueCapacity = audioLinkQueueCapacity;
	}
	public int getAudioLinkQueueCapacity()
	{
		return audioLinkQueueCapacity;
	}
}
