package logger;

public class Logger
{
	public static void log(int s, String obj, String meth, String msg)
	{
		String sev;
		
		if(s == 0) //warning 
			sev = "WARN  ::";
		else if(s == 1) //error
			sev = "ERROR ::";
		else //fatal error
			sev = "FATAL ::";
		

		if(s != 0)
		    System.err.println(sev + obj + " - " + meth + "() " + msg);
		else
		    System.out.println(sev + obj + " - " + meth + "() " + msg);
	}
	
	public static void logDebug(String msg)
	{
		//System.out.println(msg);
	}
}
