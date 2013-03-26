package de.evaluationtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class NameSource
{
	public String[][] prefixPropertyEndpoints; 
	
	public String getProperty(String resource)
	{
		for(String[] prefixPropertyEndpoint : prefixPropertyEndpoints)
		{
			if(resource.startsWith(prefixPropertyEndpoint[0]))
			{
				return prefixPropertyEndpoint[1];
			}
		}
		return null;
	}
	
	public String getEndpoint(String resource)
	{
		for(String[] prefixPropertyEndpoint : prefixPropertyEndpoints)
		{
			if(resource.startsWith(prefixPropertyEndpoint[0]))
			{
				return prefixPropertyEndpoint[2];
			}
		}
		return null;		
	}
	
	public NameSource(File f) throws FileNotFoundException
	{
		Scanner in = new Scanner(f);
		//ignore the first line
		in.nextLine();
		String line;
		List<String[]> prefixPropertyEndpointList = new LinkedList<String[]>();
		while(in.hasNext())
		{
			line=in.nextLine();
			prefixPropertyEndpointList.add(line.split("\t"));
		}
		prefixPropertyEndpoints = prefixPropertyEndpointList.toArray(new String[0][]);
		in.close();
	}
}