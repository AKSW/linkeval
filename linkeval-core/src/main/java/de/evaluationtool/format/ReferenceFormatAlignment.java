package de.evaluationtool.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import de.evaluationtool.Link;
import de.evaluationtool.Reference;

/** n triples format
 * @author Konrad Höffner */
public class ReferenceFormatAlignment extends ReferenceFormat
{	
	@Override public String getFileExtension()	{return "xml";}
	@Override public String getDescription() {return "Alignment XML";}
	@Override public boolean hasWriteSupport() {return false;}
	@Override public boolean includesEvaluation() {return false;}
	
	@Override
	public Reference readReference(File f,boolean includeEvaluation, int loadLimit) throws IOException
	{
		Document doc = null;
		
		try {doc = new SAXBuilder().build(f);} catch (JDOMException e) {throw new IOException(e);}

		Namespace alignmentNS = Namespace.getNamespace("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#");
		Namespace rdfNS = Namespace.getNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		Element rootElement = doc.getRootElement();
		Element alignmentElement = (Element) rootElement.getChild("Alignment",alignmentNS);

		List<Element> mapElements = new ArrayList<Element>(alignmentElement.getChildren("map", alignmentNS));

		Set<Link> links = new HashSet<Link>();

		int n = mapElements.size();
		if (loadLimit > 0)
		{
			//Collections.shuffle(mapElements);
			n = Math.min(n, loadLimit);
		}
		String property = null;
		if(n>0)
		{
			property = mapElements.get(0).getChild("Cell", alignmentNS).getChildText("relation",alignmentNS);
			if(property.equals("=")) {property = "http://www.w3.org/2002/07/owl#sameAs";}
		}
		for (int i = 0; i < n; i++)
		{
			Element mapElement = mapElements.get(i);
			Element cellElement = mapElement.getChild("Cell", alignmentNS);
			String entity1 = cellElement.getChild("entity1", alignmentNS).getAttributeValue("resource", rdfNS);
			// System.out.println(cellElement.getChild("entity1",namespace).getAttributes());
			String entity2 = cellElement.getChild("entity2", alignmentNS).getAttributeValue("resource", rdfNS);
			double correctness = Double.valueOf(cellElement.getChildText(
					"measure", alignmentNS));
			Link link = new Link(entity1, entity2, correctness);

			links.add(link);
		}
		return new Reference(links,property);
	}

	@Override
	public void writeReference(Reference reference, File f,boolean includeEvaluation) throws FileNotFoundException
	{
		PrintWriter out = new PrintWriter(f);
		out.close();
	}

}