package dataType;

public class FileLink extends Link
{
	public FileLink(String fileName, Link l)
	{
		super(l);
		this.fileName = fileName;
	}
	
	private String fileName;

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFileName()
	{
		return fileName;
	}
}
