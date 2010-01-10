package db;

import java.util.ArrayList;

public class DBUtil
{
	public static String arrayListToSqlArray(ArrayList<Integer> in)
	{
		String out = "";
		boolean firstItteration = true;
		for(Integer id : in)
		{
			//create statement to lock domains
			if(firstItteration)
				firstItteration = false;
			else
				out += ", ";
			out += id;
		}
		return out;
	}

	public static String escapeSQLInsertStrings(String in)
	{
		in = in.replaceAll("'", "\'");
		in = in.replaceAll("\\", "\\\\");
		return in;
	}
}
