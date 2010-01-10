package urlFilter;

import java.util.Collection;

import dataType.Link;


public interface FilterInt
{
    public Collection<Link> filter(String domainName, Collection<Link> links);
}
