package net.saim.sparql;

import java.util.Random;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.konrad.commons.sparql.PrefixHelper;
import de.konrad.commons.sparql.SPARQLHelper;

public class MedicalMatchingHelper
{
	public static final String LINKEDCT_SPARQL_ENDPOINT = "http://db0.aksw.org:8895/sparql";
	//public static final String LINKEDCT_SPARQL_ENDPOINT = "http://data.linkedct.org/sparql";
	// "http://data.linkedct.org/resource/linkedct/"
	public static final String MESH_SPARQL_ENDPOINT = "http://db0.aksw.org:8895/sparql";
	//public static final String WHOGHOCOUNTRY_SPARQL_ENDPOINT = "http://localhost:8890/sparql";
	public static final String WHOGHOCOUNTRY_SPARQL_ENDPOINT = "http://db0.aksw.org:8895/sparql";

	// timeout for sparql queries in milliseconds
	private static final int timeout = 1000; 	

	public static String getLinkedCTConditionURIs(String[] conditionName)
	{
		return null;
	}

	public static String getLinkedCTConditionURI(String conditionName) throws Exception
	{		
		String query = 
			PrefixHelper.formatPrefixes(SPARQLHelper.getDefaultPrefixes())+
			" SELECT ?condition WHERE " +
			"{?trial linkedct:condition ?condition. " +
			"?condition linkedct:condition_name \""+conditionName+"\".}";
		try
		{
			ResultSet rs = SPARQLHelper.query(LINKEDCT_SPARQL_ENDPOINT, null, query,timeout+new Random().nextInt(timeout/2));

			QuerySolution solution = rs.nextSolution();
			//		//System.out.println(solution.getResource("?disease").toString());
			return solution.getResource("condition").toString();
		}
		catch(Exception e)
		{
			throw e;
		}
		//return "somethingelse";
	}

	public static String getGHOCauseURI(String diseaseName) throws Exception
	{
		String query = PrefixHelper.formatPrefixes(SPARQLHelper.getDefaultPrefixes())+
		" SELECT ?disease "+
		//"FROM <http://localhost/ghocountrydeath> "+
		"WHERE "+
		"{?disease rdf:type gho:Disease. "+
		"?disease rdfs:label \""+diseaseName+"\".} ";

		//			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		//			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
		//			"SELECT ?disease ?diseasename" +
		//			"FROM <http://localhost/ghocountrydeath>" +
		//			"WHERE" +
		//			"{?disease rdf:type ?dimensions." +
		//			"?dimensions rdfs:label \"Disease\"." +
		//			"?disease rdfs:label ?diseasename.}";


		try
		{
			QuerySolution solution = SPARQLHelper.query(WHOGHOCOUNTRY_SPARQL_ENDPOINT, null, query,timeout+new Random().nextInt(timeout/2)).nextSolution();
			return solution.getResource("?disease").toString();
			//			return "something";
		}
		catch(Exception e)
		{
			throw e;
		}

	}

//	public static void convert(File input, File output,boolean append) throws JDOMException, IOException
//	{
//		int numberOfAlreadyConvertedLines = 0;
//		if(append)
//		{
//		//	 alignmentCells = new LinkedList<AlignmentCell>();			
//			System.out.print("loading alignment cells for appending...");
//			List<AlignmentCell> alignmentCells = AlignmentHelper.loadAlignmentCells(output,0);
//			System.out.println("finished");
//			numberOfAlreadyConvertedLines = alignmentCells.size();			
//		}		
//		LinkedList<CellPanel> newCellPanels = new LinkedList<CellPanel>();
//		{
//			Scanner in = new Scanner(input);
//			String line=null;
//			// first line contains the table column labels
//			in.nextLine();
//			int count = 0;
//			boolean repetition = false;
//			while(in.hasNextLine()||repetition)
//			{					
//				if(repetition)
//				{
//					repetition = false;
//					System.out.println("repeating: reading external format, line "+count);
//				}
//				else
//				{
//					line = in.nextLine();
//					count++;
//					if(count<=numberOfAlreadyConvertedLines) continue;
//					System.out.println("reading external format, line "+count);
//				}			
//
//				String[] tokens = line.split("\t");
//				if(tokens.length<6) continue;
//				//String whoID = tokens[0];
//				String cause = tokens[1];
//				//String causeSynonyms = tokens[2];
//				//String nctID = tokens[3];
//				String condition = tokens[4];
//				//String conditionSynonyms = tokens[5];
//
//				// german to english format which java needs
//				String matchingPercentage = tokens[6].replace(',', '.');
//
//				try
//				{
//					String entity1 = MedicalMatchingHelper.getLinkedCTConditionURI(condition);
//					String entity2 = MedicalMatchingHelper.getGHOCauseURI(cause);
//					// TODO: this is bad: using a uri element for getting the data
//					CellPanel cellPanel = new CellPanel(null,new AlignmentCell(entity1, entity2, Double.valueOf(matchingPercentage)/100));
//					newCellPanels.add(cellPanel);
//				}
//				//				catch(TimeoutException e)
//				//				{
//				//					repetition = true;
//				//				}
//				catch(Exception e)
//				{	
//					System.out.println("Error converting (see stack trace below).");
//					e.printStackTrace(System.out);
//
//					String userInput;
//					do
//					{
//						System.out.println("Do you want to t)ry again, i)gnore and continue, ,c)ancel or s)save everything until now?");
//						userInput = new Scanner(System.in).nextLine();
//					} while(!(userInput.equals("i")||userInput.equals("c")||userInput.equals("s")||userInput.equals("t")));
//					if(userInput.equals("i")) {System.out.println("skipping this entry");continue;}
//					if(userInput.equals("c")) {System.out.println("Cancelling conversion.");in.close();return;}
//					if(userInput.equals("s")) {System.out.print("Saving progress until now...");break;}
//					if(userInput.equals("t")) {System.out.println("Querying the same entry again.");repetition = true;}
//				}
//			}
//			in.close();
//		}
//		EvaluationFrame.saveReferenceXML(output, newCellPanels,true,append);
//		System.out.println("finished saving");
//	}


}
