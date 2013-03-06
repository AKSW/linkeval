package de.evaluationtool.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import de.evaluationtool.Correctness;
import de.evaluationtool.Link;
import de.evaluationtool.Reference;

/** Tab separated reference format with the columns containing in this order: URI1 URI2 confidence correctness (correctness is optional) 
 * @author Konrad Höffner */
public class ReferenceFormatTSV extends ReferenceFormat
{
	@Override public String getFileExtension()	{return "tsv";}
	@Override public String getDescription() {return "TSV (Tab Separated Values URI1 URI2 confidence? correctness?)";}
	@Override public boolean includesEvaluation()	{return true;}


	@Override
	public Reference readReference(File f,boolean includeEvaluation,int loadLimit) throws FileNotFoundException
	{
		Scanner in = new Scanner(f);
		Set<Link> links = new HashSet<Link>();
		for(int i=0;(loadLimit==0||i<loadLimit)&&in.hasNextLine();i++)
		{
			String[] tokens = in.nextLine().split("\t");
			String uri1 = tokens[0];
			String uri2 = tokens[1];
			Double confidence;
			if(tokens[2].isEmpty())	{confidence=null;}
			else					{confidence = Double.valueOf(tokens[2]);}
			Correctness correctness = null;
			if(includeEvaluation&&tokens.length>3) {correctness = Correctness.valueOf(tokens[3]);}
			Link link = new Link(uri1, uri2, confidence,correctness);
			links.add(link);
			//Correctness.valueOf("correct");
		}
		in.close();
		return new Reference(links, null);
	}

	@Override
	public void writeReference(Reference reference, File f,boolean includeEvaluation) throws FileNotFoundException
	{
		PrintWriter out = new PrintWriter(f);
		for(Link link: reference.links)
		{
			String evaluationPart = (includeEvaluation==false)?"":(link.correctness!=null?("\t"+link.correctness.toString()):"");
			out.println(link.uris.first+"\t"+link.uris.second+"\t"+(link.confidence==null?"":link.confidence)+evaluationPart);
		}
		out.close();
	}

}