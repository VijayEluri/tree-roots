package fileFilter;

import java.io.File;
import java.io.FilenameFilter;

public class FilterPartialFiles implements FilenameFilter
{

	public boolean accept(File dir, String name)
	{
		if(name.endsWith(".part"))
			return false;
		else
			return true;
	}

}
