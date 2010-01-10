package config;

public class ConfigFilter
{
    private String [] filteredFileTypes;
    private int ratio;

    public void setFilteredFileTypes(String [] filteredFileTypes)
    {
	   this.filteredFileTypes = filteredFileTypes;
    }

    public String [] getFilteredFileTypes()
    {
	   return filteredFileTypes;
    }

    public void setRatio(int ratio)
    {
	   this.ratio = ratio;
    }

    public int getRatio()
    {
	   return ratio;
    }
}
