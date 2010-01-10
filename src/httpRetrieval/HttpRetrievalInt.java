package httpRetrieval;

import java.io.File;
import java.util.Collection;

import parser.ParserInt;
import config.ConfigHttpRetrieval;
import dataType.Link;
import dataType.SpiderInfo;
import exception.SpiderDataException;

public interface HttpRetrievalInt
{
	public void init(ConfigHttpRetrieval configHttpRetrieval);
	public Collection<String> retrievePageAndParseLinks(Link link, ParserInt parser, SpiderInfo info) throws SpiderDataException;
	public void getFile(Link link, File fileToWriteTo, SpiderInfo info) throws SpiderDataException;
}
