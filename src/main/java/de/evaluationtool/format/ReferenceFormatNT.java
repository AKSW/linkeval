package de.evaluationtool.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import de.evaluationtool.Link;
import de.evaluationtool.Reference;

/** n triples format
 * @author Konrad Höffner */
public class ReferenceFormatNT extends ReferenceFormat
{	
	@Override public String getFileExtension()	{return "nt";}
	@Override public String getDescription()	{return "N Triples";}
	@Override public boolean includesEvaluation() {return false;}

	@Override
	public Reference readReference(File f, boolean includeEvaluation, int loadLimit) throws FileNotFoundException
	{		
		Scanner in = new Scanner(f);
		Set<Link> links = new HashSet<Link>();
		String property = null;
		
		for(int i=0;(loadLimit==0||i<loadLimit)&&in.hasNextLine();i++)
		{
			String line = in.nextLine();
			String[] tokens = line.split("\\s+");
			// remove <> signs
			String entity1 = tokens[0].substring(1, tokens[0].length()-1);
			if(property==null) property=tokens[1].substring(1, tokens[1].length()-1);
			String entity2 = tokens[2].substring(1, tokens[2].length()-1);	
			links.add(new Link(entity1,entity2,null));
		}		
		in.close();
		return new Reference(links,property);
	}

	@Override
	public void writeReference(Reference reference, File f,boolean includeEvaluation) throws FileNotFoundException
	{
		PrintWriter out = new PrintWriter(f);
		for(Link link: reference.links)
		{
			out.println('<'+link.uris.first+">\t<"+reference.property+">\t<"+link.uris.second+">\t.");
		}
		out.close();
	}
}