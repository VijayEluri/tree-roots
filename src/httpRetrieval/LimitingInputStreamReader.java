package httpRetrieval;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//a thin wrapper that restricts a inputStreamReader from reading further then
//the configured limit
public class LimitingInputStreamReader extends InputStreamReader
{
	long roughMaxLength;
	int curPos;
	
	public LimitingInputStreamReader(long roughMaxLength, InputStream arg0)
	{
		super(arg0);
		this.roughMaxLength = roughMaxLength;
		curPos = 0;
	}
	
	public int read() throws IOException
	{
		//if curPos is over the max then act like the end of the stream has been reached
		if(curPos > roughMaxLength)
			return -1;
		
		curPos++;
		return super.read();
	}

	public int read(char[] buf) throws IOException
	{
		//if curPos is over the max then act like the end of the stream has been reached
		if(curPos > roughMaxLength)
			return -1;
		
		int charsRead = super.read(buf);
		curPos += charsRead;
		return charsRead;
	}
	
	
	/*
	//for version 6
	public int read(CharBuffer target) throws IOException
	{
		//if curPos is over the max then act like the end of the stream has been reached
		if(curPos > roughMaxLength)
			return -1;
		
		int charsRead = super.read(target);
		curPos += charsRead;
		return charsRead;
	}*/
	
	public int read(char[] cbuf, int off,int len) throws IOException
	{
		//if curPos is over the max then act like the end of the stream has been reached
		if(curPos > roughMaxLength)
			return -1;
		
		int charsRead = super.read(cbuf, off, len);
		curPos += charsRead;
		return charsRead;
	}
	
	public boolean ready() throws IOException
	{
		//if curPos is over the max then act like the end of the stream has been reached
		if(curPos > roughMaxLength)
			return false;
		
		return super.ready();
	}
	
	public boolean markSupported() { return false; }
	
	public void reset() throws IOException
	{
		curPos = 0;
		super.reset();
	}
}
