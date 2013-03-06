/**
 * Copyright (C) 2011, SAIM team at the MOLE research
 * group at AKSW / University of Leipzig
 *
 * This file is part of SAIM (Semi-Automatic Instance Matcher).
 *
 * SAIM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SAIM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.konrad.commons.sparql;

import static org.junit.Assert.assertTrue;

import java.util.Map;

//import net.saim.knowledgebase.Knowledgebases;

import org.junit.Test;

//import de.uni_leipzig.simba.data.Instance;

/** @author Konrad Höffner */
public class SPARQLHelperTest {

	static String[] testObjects = {"Comune di Marcedusa@en","556^^http://www.w3.org/2001/XMLSchema#integer","http://www4.wiwiss.fu-berlin.de/flickrwrappr/photos/Marcedusa"}; 
	
	@Test
	public void testDataType()
	{
		String[] dataTypes = {"","http://www.w3.org/2001/XMLSchema#integer",""};			
		for(int i=0;i<testObjects.length;i++)
		{
		assertTrue(SPARQLHelper.dataType(testObjects[i]).equals(dataTypes[i]));
		}
	}

	/** Test method for {@link de.konrad.commons.sparql.SPARQLHelper#languageTag(java.lang.String)}.*/
	@Test
	public void testLanguageTag()
	{
		String[] languageTags = {"en","",""};
		for(int i=0;i<testObjects.length;i++)
		{
		assertTrue(SPARQLHelper.languageTag(testObjects[i]).equals(languageTags[i]));
		}
	}

	/** Test method for {@link de.konrad.commons.sparql.SPARQLHelper#lexicalForm(java.lang.String)}. */
	@Test
	public void testLexicalForm()
	{
		String[] lexicalForms = {"Comune di Marcedusa","556","http://www4.wiwiss.fu-berlin.de/flickrwrappr/photos/Marcedusa"};
		for(int i=0;i<testObjects.length;i++)
		{
		assertTrue(SPARQLHelper.lexicalForm(testObjects[i]).equals(lexicalForms[i]));
		}
	}
	
//	@Test
//	public void getRandomSample() throws Exception
//	{
//		Instance[] instances = SPARQLHelper.getRandomSample(Knowledgebases.DBPEDIA_SETTLEMENT,100,0);
//		System.out.println(Arrays.toString(instances));
//	}

	static final Map<String,String> rdfsPrefix = SPARQLHelper.textToMap("rdfs	http://www.w3.org/2000/01/rdf-schema#");
	
	@Test
	public void testTextToMap()
	{
		assertTrue(rdfsPrefix.containsKey("rdfs")&&rdfsPrefix.get("rdfs").equals("http://www.w3.org/2000/01/rdf-schema#"));
	}

	@Test
	public void testGetDefaultPrefixes()
	{
		Map<String,String> defaultPrefixes = SPARQLHelper.getDefaultPrefixes();
		assertTrue(defaultPrefixes.containsKey("rdfs")&&defaultPrefixes.get("rdfs").equals("http://www.w3.org/2000/01/rdf-schema#"));
	}

	@Test
	public void testFormatPrefixes()
	{
		assertTrue(PrefixHelper.formatPrefixes(rdfsPrefix).equals("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"));
	}
}
