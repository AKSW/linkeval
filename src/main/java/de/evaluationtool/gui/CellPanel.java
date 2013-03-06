/**
 * Copyright (C) 2010, SAIM team at the MOLE research
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
package de.evaluationtool.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.saim.sparql.MedicalMatchingHelper;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.javadocmd.simplelatlng.LatLng;

import de.evaluationtool.Correctness;
import de.evaluationtool.Link;
import de.konrad.commons.sparql.PrefixHelper;
import de.konrad.commons.sparql.SPARQLHelper;

/**
 * @author konrad
 *
 */
@SuppressWarnings("serial")
public class CellPanel extends JPanel
{
	final boolean DEBUG = true;
	final boolean BROWSE_GOOGLE_MAPS = true;
		
	public final JLabel label1 = new JLabel("entity1");
	public final JLabel label2 = new JLabel("entity2");
	private final JLabel distanceLabel = new JLabel("");
	private Double distance = null;
	public final Link link;

	ButtonGroup buttonGroup = new ButtonGroup();
	JToggleButton correctButton = new JToggleButton("correct");
	JToggleButton incorrectButton = new JToggleButton("incorrect");
	JToggleButton unsureButton = new JToggleButton("unsure");

	JButton openURLsButton = new JButton("URLs");

	final EvaluationFrame frame;

	ActionListener cpListener = new CellPanelActionListener(this);
	Map<String,String> entityToEndpoint = new HashMap<String,String>();

	public LatLng position1 = null;
	public LatLng position2 = null;
	
	public static final int NORMAL_MODE = 0;
	public static final int LINKEDCT_MODE = 1;
	static final int GHO_MODE = 2;

	public void setDistance(Double d,String unit)
	{
		this.distance = d;
		if(d!=null) {distanceLabel.setText(d+' '+unit);}
	}
	
	public Double getDistance() {return this.distance;}
	
	public static LatLng[] getLatitudesLongitudes(String[] entities,String[][] nameSources)
	{
		String[][] latitudesLongitudes = new String[2][entities.length];
		LatLng[] latLongs = new LatLng[entities.length];
		String sparqlEndpoint = null;

		boolean successful = false;
		for(String[] nameSource: nameSources)
		{
			if(entities[0].startsWith(nameSource[0]))
			{					
				sparqlEndpoint = nameSource[2];
				successful=true;
				break;
			}
		}

		if(!successful)
		{
			for(int i=0;i<entities.length;i++)
			{
				latLongs[i] = null;
			}
			return latLongs;
		}

		StringBuffer latitudeQueryBuffer = new StringBuffer();
		StringBuffer longitudeQueryBuffer = new StringBuffer();
		StringBuffer[] queryBuffers = {latitudeQueryBuffer,longitudeQueryBuffer};

//		latitudeQueryBuffer.append(PrefixHelper.formatPrefixes(SPARQLHelper.getDefaultPrefixes())+"\n");
		latitudeQueryBuffer.append(
				" select * where {?entity <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?value. filter ("
		);					
//		longitudeQueryBuffer.append(PrefixHelper.formatPrefixes(SPARQLHelper.getDefaultPrefixes())+"\n");
		longitudeQueryBuffer.append(
				" select * where {?entity <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?value. filter ("
		);					

		for(int i = 0;i < 2; i++)
		{
			StringBuffer queryBuffer = queryBuffers[i];
			for(String entity:entities)
			{
				queryBuffer.append("?entity = <"+entity+">||");
			}
			queryBuffer.delete(queryBuffer.length()-2,queryBuffer.length()); // remove the last "||" character			
			queryBuffer.append(")}");		
			// virtuoso sparql only:
			//			queryBuffer.append("?entity in (");					
			//			for(String entity:entities)
			//			{
			//				queryBuffer.append("<"+entity+">,");
			//			}
			//			queryBuffer.deleteCharAt(queryBuffer.length()-1); // remove the last comma (',') character
			//			queryBuffer.append("))}");
			String query = queryBuffer.toString();
			// System.out.println(predicate);
			// String query =
			// "select * where {<"+entity+"> <"+predicate+"> ?name}";
			// System.out.println(query);

			ResultSet rs = null;

			Map<String,String> entityToValue = new HashMap<String,String>();

			try
			{
				rs = SPARQLHelper.query(sparqlEndpoint, null, query);
				while(rs.hasNext())
				{
					QuerySolution solution = rs.next();
					String typedValue = solution.getLiteral("value").getValue().toString();						
					// sometimes the datatype is included in the literal value of the typed literal 
					// occurs when using the local geonames endpoint
					// (jena problem or more possibly my fault when I did the SPARQL upload or so...) 
					entityToValue.put(solution.get("entity").toString(),typedValue.replace("^^xsd:double",""));				 
					//System.out.println(solution);
				}
				for(int j=0;j<entities.length;j++)
				{					
					latitudesLongitudes[i][j] = entityToValue.get(entities[j]);					
				}
			}
			catch(NoSuchElementException e)
			{
				System.err.println("Problem with query \""+query+"\" at endpoint \""+sparqlEndpoint+"\"");
				System.err.println("Resultset: "+rs);
				throw e;
			}
			catch(Exception e)
			{
				System.err.println("Problem with query \""+query+"\" at endpoint \""+sparqlEndpoint+"\"");
				System.err.println("Resultset: "+rs);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		for(int i=0;i<entities.length;i++)
		{

			try
			{
				latLongs[i] = new LatLng(Double.valueOf(latitudesLongitudes[0][i]),Double.valueOf(latitudesLongitudes[1][i]));
			}
			catch(Exception e)
			{
				System.out.println("Warning: could not create point for entity "+entities[i]+" latituide "+latitudesLongitudes[0][i]+" longitude "+latitudesLongitudes[1][i]);
				//e.printStackTrace();
				latLongs[i] = null;
			}
		}
		return latLongs;
	}

	public static String[] getNames(String[] entities,String[][] nameSources, int mode) throws Exception
	{
		int nameErrors = 0;
		Map<String,Integer> languagePriorities = new HashMap<String,Integer>();
		languagePriorities.put("de",1);
		languagePriorities.put("en",2);
		languagePriorities.put("",3);
		String[] names = new String[entities.length];
		String sparqlEndpoint = null;
		String predicate = null;
		
		
		switch(mode)
		{
		case NORMAL_MODE:
		{
			boolean successful = false;
			for(String[] nameSource: nameSources)
			{
				if(entities[0].startsWith(nameSource[0]))
				{
					predicate = SPARQLHelper.wrapIfNecessary(nameSource[1]);
					sparqlEndpoint = nameSource[2];					
					successful=true;
					break;
				}
			}

			if(!successful)
			{
				for(int i=0;i<names.length;i++)
					names[i] = "name source not found for "+entities[i]+"."; 
				return names;				
			}
			break;
		}
		case LINKEDCT_MODE:
		{
			sparqlEndpoint = MedicalMatchingHelper.LINKEDCT_SPARQL_ENDPOINT;
			predicate = "linkedct:condition_name";
			break;
		}
		case GHO_MODE:
		{
			sparqlEndpoint = MedicalMatchingHelper.WHOGHOCOUNTRY_SPARQL_ENDPOINT;
			predicate = "linkedct:condition_name";
			break;
		}
		}

		StringBuffer queryBuffer = new StringBuffer();
		// optimize for runtime by determining the prefix string only once
		String prefixString = PrefixHelper.formatPrefixes(PrefixHelper.restrictPrefixes(SPARQLHelper.getDefaultPrefixes(), predicate));
		queryBuffer.append(prefixString);
		queryBuffer.append(" select * where {?entity " + predicate + " ?name. filter (");					
		for(String entity:entities)
		{
			queryBuffer.append("?entity = <"+entity+">||");
		}
		queryBuffer.delete(queryBuffer.length()-2,queryBuffer.length()); // remove the last "||" character			
		queryBuffer.append(")}");		
		// virtuoso sparql only:
		//			queryBuffer.append("?entity in (");					
		//			for(String entity:entities)
		//			{
		//				queryBuffer.append("<"+entity+">,");
		//			}
		//			queryBuffer.deleteCharAt(queryBuffer.length()-1); // remove the last comma (',') character
		//			queryBuffer.append("))}");
		String query = queryBuffer.toString();
		// System.out.println(predicate);
		// String query =
		// "select * where {<"+entity+"> <"+predicate+"> ?name}";
		// System.out.println(query);

		ResultSet rs = null;

		Map<String,String> entityToName = new HashMap<String,String>();
		Map<String,String> entityToLanguage = new HashMap<String,String>();
		// TODO: höchstmögliches mit dem gleichen anzeigen
		try
		{
			rs = SPARQLHelper.query(sparqlEndpoint, null, query);
			while(rs.hasNext())
			{
				QuerySolution solution = rs.next();
				String entity = solution.get("entity").toString();
				String language =  solution.getLiteral("name").getLanguage();
				String existingLanguage = entityToLanguage.get(entity);
				
				// only update if lower priority
				if(existingLanguage==null||!languagePriorities.containsKey(existingLanguage)||languagePriorities.containsKey(language)&&(languagePriorities.get(language)<languagePriorities.get(existingLanguage)))
				{					
					entityToName.put(entity,solution.get("name").asLiteral().getLexicalForm());
					entityToLanguage.put(entity,language);
				}				 
				//System.out.println(solution);
			}
			for(int i=0;i<entities.length;i++)
			{
				names[i] = entityToName.get(entities[i]);
				if(names[i]==null)
				{
					nameErrors++;
					names[i] = "error getting the names";
					throw new Exception("no result");												
				}
			}
		}
		catch(NoSuchElementException e)
		{
			System.err.println("Problem with query \""+query+"\" at endpoint \""+sparqlEndpoint+"\"");
			System.err.println("Resultset: "+rs);
			throw e;
		}
		catch(Exception e)
		{
			System.err.println("Problem with query \""+query+"\" at endpoint \""+sparqlEndpoint+"\"");
			System.err.println("Resultset: "+rs);
			e.printStackTrace();
			//System.err.println(e.getMessage());
			throw e;
		}
		if(nameErrors > 0 ) System.err.println(nameErrors+" name errors.");
		return names;
	}

//	public CellPanel(EvaluationFrame frame, AlignmentCell alignmentCell)
//	{
//		this(frame);
//		this.alignmentCell = alignmentCell;
//		label1.setText(alignmentCell.entity1);
//		label2.setText(alignmentCell.entity2);
//	}
	
	public CellPanel(EvaluationFrame frame, Link link)
	{
		super();
		init();
		this.frame = frame;
		this.link = link;
		label1.setText(link.uris.first);
		label2.setText(link.uris.second);	
		if(link.correctness!=null)
		{
		correctButton.setSelected(link.correctness==Correctness.correct);
		incorrectButton.setSelected(link.correctness==Correctness.incorrect);
		unsureButton.setSelected(link.correctness==Correctness.unsure);
		frame.sampleSize++;
		}
	}


	private void init()
	{		
		// this.setBorder();
		this.setMaximumSize(new Dimension(2000, this.getFontMetrics(
				this.getFont()).getHeight() * 3 + 5));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		label1.setPreferredSize(new Dimension(400, 60));
		label2.setPreferredSize(new Dimension(400, 60));
		
		buttonGroup.add(correctButton);
		buttonGroup.add(incorrectButton);
		buttonGroup.add(unsureButton);		
		this.add(correctButton);
		this.add(incorrectButton);
		this.add(unsureButton);
		this.add(openURLsButton);
		JPanel labelPanel = new JPanel();
		this.add(labelPanel);
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
		labelPanel.add(label1);
		labelPanel.add(label2);
		labelPanel.add(distanceLabel);
		correctButton.addActionListener(cpListener);
		incorrectButton.addActionListener(cpListener);
		unsureButton.addActionListener(cpListener);
		openURLsButton.addActionListener(cpListener);
	}
	

	private class CellPanelActionListener implements ActionListener
	{
		final CellPanel cellPanel;
		
		public CellPanelActionListener(CellPanel cellPanel)
		{
			this.cellPanel = cellPanel;
		}
		
		public void actionPerformed(ActionEvent e)
		{			
			if(e.getSource()==openURLsButton)
			{
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {

					JOptionPane.showConfirmDialog(frame,"Desktop doesn't support the browse action. Can't browse web pages." );					
				}

				try
				{
					// make it thread safe
					LatLng position1 = cellPanel.position1;
					LatLng position2 = cellPanel.position2;
					if(BROWSE_GOOGLE_MAPS&&position1!=null&&position2!=null)
					{
						URI googleURL = new URI("http://maps.google.de/maps?f=d&source=s_d"
								+"&saddr="+position1.getLatitude()+"+"+position1.getLongitude()
								+"&daddr="+position2.getLatitude()+"+"+position2.getLongitude()+"&hl=de");
						desktop.browse(googleURL);
					}
					else
					{
						String[] uriStrings = {link.uris.first,link.uris.second};
						for(String uriString: uriStrings)
						{						
							// some uris can't be resolved directly but have to be transformed 
							String uriDisplayString = uriString.replace("sws.geonames.", "geonames.");
							desktop.browse(new URI(uriDisplayString));
							String endpoint = frame.nameSource.getEndpoint(uriString);							
							// diseasome shows rdf as an xml file which gets in the way and it shows normal uris fine so no need anyways
							if(endpoint!=null&&!endpoint.contains("diseasome"))
							{
								desktop.browse(new URI(endpoint+"?query="+java.net.URLEncoder.encode("select * where {<"+uriString+"> ?p ?o.}")));
							}
						}
					}

				} catch (URISyntaxException e1)
				{
					e1.printStackTrace();
				} catch (IOException e2)
				{				
					e2.printStackTrace();
				}



				return;
			}
			if(link.correctness==null)
			{
				frame.sampleSize++;
				frame.updateTitle();
			}
			link.correctness = e.getSource()==correctButton?Correctness.correct
					:e.getSource()==incorrectButton?Correctness.incorrect
							:Correctness.unsure;
			//			if (e.getSource() == correctButton)
			//			{
			//				correctness = Correctness.correct;				
			//				// correctButton.setSelected(true);
			//				// incorrectButton.setSelected(false);
			//				return;
			//			}
			//			if (e.getSource() == incorrectButton)
			//			{
			//				correctnessIsSet = true;
			//				// correctButton.setSelected(false);
			//				// incorrectButton.setSelected(true);
			//				return;
			//			}
			//			if (e.getSource() == incorrectButton)
			//			{
			//				correctness = false;
			//				correctnessIsSet = true;
			//				// correctButton.setSelected(false);
			//				// incorrectButton.setSelected(true);
			//				return;
			//			}

		}
	}

	//	// all entities have to have the same name source
	//	public void queryNames(String[][] nameSources)
	//	{
	//		try
	//		{
	//			this.label1.setText(getName(entity1,nameSources));
	//		} catch (Exception e)
	//		{
	//			if(DEBUG) e.printStackTrace();
	//			this.label1.setText(e.getClass().toString().split(" ")[1]);
	//		}
	//		try
	//		{
	//			this.label2.setText(getName(entity2,nameSources));
	//		} catch (Exception e)
	//		{
	//			if(DEBUG) e.printStackTrace();
	//			this.label2.setText(e.getClass().toString().split(" ")[1]);
	//		}
	//	}
	//
	//	private static String getName(String entity,String[][] nameSources,boolean specialMode)
	//	{	
	//		return getNames(new String[] {entity},nameSources, specialMode)[0];
	//	}

}