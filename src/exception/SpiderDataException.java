package exception;

public class SpiderDataException extends Exception
{
	private static final long serialVersionUID = 12L; // TODO
	private String clazz;
	private String meth;
	private String msg;
	
	public SpiderDataException(Exception e)
	{
		clazz = e.getClass().getSimpleName();
		msg = e.getCause() + " -> " + e.getMessage();
		meth = "";
	}
	
	public SpiderDataException(String clazz, String meth,String msg)
	{
		this.clazz = clazz;
		this.msg = msg;
		this.meth = meth;
	}
	
	public SpiderDataException(String clazz, String meth,String msg, String err)
	{
		this.clazz = clazz;
		this.msg = msg;
		this.meth = meth + " :: " + err;
	}

	public String getMeth()
	{
		return meth;
	}

	public void setMeth(String meth)
	{
		this.meth = meth;
	}

	public String getClazz()
	{
		return clazz;
	}

	public void setClazz(String clazz)
	{
		this.clazz = clazz;
	}

	@Override
	public String getMessage()
	{
		return msg;
	}

	public void setMessage(String msg)
	{
		this.msg = msg;
	}
	
}
