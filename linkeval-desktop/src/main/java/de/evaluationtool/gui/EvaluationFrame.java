/**
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
 *
 */

package de.evaluationtool.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lombok.Cleanup;
import net.saim.util.TSVHelper;

import org.aksw.commons.util.Statistic;
import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.semanticweb.owl.align.Alignment;

import uk.ac.shef.wit.simmetrics.metrichandlers.MetricHandler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import de.evaluationtool.Correctness;
import de.evaluationtool.gui.LabelThread;
import de.evaluationtool.Link;
import de.evaluationtool.NameSource;
import de.evaluationtool.Reference;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author Konrad Höffner 
 */
public class EvaluationFrame extends JFrame
{
	double fScoreBeta = 0.5;
	File defaultDirectory = new File(".");
	static final boolean shuffle = true;
	static final int CHART_WIDTH = 1024;
	static final int CHART_HEIGHT = 768;
	int sampleSize = 0;
	// the link property, e.g. owl:sameAs
	String property = null;
	Reference reference = null;	

	final static boolean DEBUG = true;
	private static final long serialVersionUID = -60983207336636791L;

	public  final File loadLimitFile;
	public final File autoEvalDistanceFile;
	public final File nameSourceFile;
	public final File readmeTemplateFile;

	final static boolean KEEP_EVALUTED_WHEN_SHRINKING = true;
	//private boolean isGeographic = false; 
	public final boolean LOADLIMIT_COUNT_ALL_LINES = true;

	private static final int QUERY_DELAY = 100;
	private static final String DEFAULT_TITLE = "Alignment Evaluation";
	int loadLimit = 100;
	public int getLoadLimit() {return loadLimit;}

	private int autoEvalDistance = 20;

	public int getAutoEvalDistance() {return autoEvalDistance;}

	private int originalSize = 0;
	private int shrinkedSize = 0;
	public final File autoload = new File("temp/autoload.txt");

	private  String[][] nameSources;
	public final NameSource nameSource;

	JButton precisionButton = new JButton("calculate precision for a specific threshold");
	// Menu
	private JMenuBar menuBar = new JMenuBar();

	private JMenu fileMenu = new JMenu("File");
	JMenuItem loadReferenceItem  = new JMenuItem("Load Reference");
	JMenuItem saveReferenceOnlyItem  = new JMenuItem("Save Reference only");
	JMenuItem saveReferenceAndEvaluationItem = new JMenuItem("Save Reference & Evaluation");	

	//	JMenuItem loadSameAsNTItem = new JMenuItem("Load N Triples sameAs File");
	//	JMenuItem loadReferenceItem = new JMenuItem("Load Reference XML File");
	//	JMenuItem loadReferenceNTItem = new JMenuItem("Load Reference N Triples File");
	//	JMenuItem loadEvaluationXMLItem = new JMenuItem("Load Evaluation XML File");
	//	//JMenuItem loadReferenceTSVItem = new JMenuItem("Load Reference File in Amrapalis External Format");	
	//	JMenuItem loadReferenceAlignmentTSVItem = new JMenuItem("Load Reference Alignment TSV File");
	//	JMenuItem loadPositiveNegativeNTItem = new JMenuItem("Load positive, negative, unsure and unchecked.nt files");
	//	JMenuItem[] loadMenuItems = {loadSameAsNTItem,loadReferenceItem,loadReferenceNTItem,loadEvaluationXMLItem,loadReferenceAlignmentTSVItem,loadPositiveNegativeNTItem};
	JMenuItem[] fileMenuItems = {loadReferenceItem,saveReferenceOnlyItem,saveReferenceAndEvaluationItem};

	//	private JMenu saveMenu = new JMenu("Save");
	////	JMenuItem savePositiveNegativeNTItem = new JMenuItem("Save as positive, negative, unsure and unchecked.nt files");
	////	JMenuItem saveXMLItem = new JMenuItem("Save Evaluation as XML");
	////	JMenuItem saveTSVItem = new JMenuItem("Save Evaluation as TSV");
	////	JMenuItem[] saveMenuItems = {savePositiveNegativeNTItem,saveXMLItem,saveTSVItem};
	//	JMenuItem[] saveMenuItems = {};

	private JMenu operationMenu = new JMenu("Operations");
	JMenuItem reloadLabelsItem = new JMenuItem("Reload faulty labels and distances");
	JMenuItem autoEvalItem = new JMenuItem("Autoevaluate obvious matches (if distance does not matter set autoeval distance to -1)");
	JMenuItem sortByCorrectnessItem = new JMenuItem("Sort by correctness");
	JMenuItem convertItem = new JMenuItem("Convert Amrapalis External Format to Alignment");	
	JMenuItem saveReferenceXMLItem = new JMenuItem("Save as Alignment Reference file");
	JMenuItem shrinkToLoadLimitItem = new JMenuItem("Shrink to load limit ("
			+ loadLimit + ")");
	JMenuItem evaluateItem = new JMenuItem("Evaluate");
	JMenuItem evaluateAlignItem = new JMenuItem("Evaluate with the Alignment API");
	JMenuItem removeAllUnderAutoEvalDistanceItem = new JMenuItem("Remove all entities under the autoeval distance");

	JMenuItem[] operationMenuItems = {reloadLabelsItem,autoEvalItem,sortByCorrectnessItem,convertItem,saveReferenceXMLItem,
			shrinkToLoadLimitItem,evaluateItem,evaluateAlignItem,removeAllUnderAutoEvalDistanceItem};

	private JMenu optionMenu = new JMenu("Options");
	JMenuItem changeLoadLimitItem = new JMenuItem("Change load limit ("+ loadLimit + ")");
	JMenuItem changeAutoEvalDistanceItem = new JMenuItem("Change auto eval distance ("+ autoEvalDistance + ")");
	//	JMenuItem editConfigurationFileItem = new JMenuItem("Edit the configuration file");
	//	JMenuItem reloadConfigurationFileItem = new JMenuItem("Reload the configuration file");

	JMenuItem editNameSourceFileItem = new JMenuItem("Edit the name source file");
	JMenuItem reloadNameSourceFileItem = new JMenuItem("Reload the name source file");
	JMenuItem[] optionMenuItems = {changeLoadLimitItem,changeAutoEvalDistanceItem,editNameSourceFileItem,reloadNameSourceFileItem};

	private JMenu helpMenu = new JMenu("Help");
	JMenuItem manualMenuItem = new JMenuItem("Manual");
	JMenuItem javadocMenuItem = new JMenuItem("Javadoc");
	JMenuItem[] helpMenuItems = {manualMenuItem,javadocMenuItem};

	private JMenu[] menues = {fileMenu,operationMenu,optionMenu,helpMenu};

	// Scrollpane
	public JPanel mainPanel = new JPanel();
	JScrollPane scrollPane = new JScrollPane(mainPanel);

	// Listener
	private ActionListener listener = new EvaluationFrameActionListener(this);
	private List<CellPanel> cellPanels = new ArrayList<CellPanel>();

	private LabelThread labelThread;
	String dataSourceName1;
	String dataSourceName2;

	private void setProperty(String property)
	{
		property = property.replace("<","");
		property = property.replace(">","");
		this.property = property;
	}

	private String getProperty()
	{
		if(property==null)
		{
			setProperty(JOptionPane.showInputDialog("Please type in the property."));			
		}
		return property;		
	}

	public void setLoadLimit(int n)
	{
		this.loadLimit = n;
		changeLoadLimitItem.setText("Change load limit (" + this.loadLimit+ ")");
		shrinkToLoadLimitItem.setText("Shrink to load limit ("+ loadLimit + ")");
	}

	public void setAutoEvalDistance(int n)
	{
		this.autoEvalDistance = n;
		changeAutoEvalDistanceItem.setText("Change auto eval distance (" + this.autoEvalDistance+ ")");		
	}

	public EvaluationFrame(File loadLimitFile,File autoEvalDistanceFile, File nameSourceFile, File readmeTemplateFile) throws FileNotFoundException
	{		
		super(DEFAULT_TITLE);
		this.nameSource = new NameSource(nameSourceFile);
		this.loadLimitFile = loadLimitFile;
		this.autoEvalDistanceFile = autoEvalDistanceFile;		
		this.nameSourceFile= nameSourceFile;
		this.readmeTemplateFile = readmeTemplateFile;
		this.setLocation(100, 100);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(800, 700));
		this.add(scrollPane);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));


		//		for(ReferenceFormat format: ReferenceFormats.referenceFormats)
		//		{
		//			if(format.hasReadSupport())
		//			{
		//				JMenuItem item = new JMenuItem("Load "+format.getDescription());
		//				fileMenu.add(item);			
		//			}
		//			if(format.hasWriteSupport())
		//			{
		//				JMenuItem item = new JMenuItem("Save "+format.getDescription());
		//				saveMenu.add(item);			
		//			}
		//		}

		for(JMenuItem item : fileMenuItems) {fileMenu.add(item);}
		//		for(JMenuItem item : saveMenuItems) {saveMenu.add(item);}
		for(JMenuItem item : operationMenuItems) {operationMenu.add(item);}
		for(JMenuItem item : optionMenuItems) {optionMenu.add(item);}
		for(JMenuItem item : helpMenuItems) {helpMenu.add(item);}

		for(JMenu menu: menues)
		{
			for(Component component:  menu.getMenuComponents())
			{
				if(component instanceof JMenuItem)
				{
					((JMenuItem)component).addActionListener(listener);
				}
			}
			menuBar.add(menu);
		}

		this.setJMenuBar(menuBar);
		this.pack();

		reloadNamesources();
		autoload();
	}

	//	private boolean checkForInternet()
	//	{
	//		try	{final URLConnection conn = new URL("http://www.google.de").openConnection();}
	//		catch (IOException e)	{return false;}
	//		return true;
	//	}

	public void reloadNamesources()
	{
		String[][] nameSourcesIntermediate = null;

		try
		{
			nameSourcesIntermediate = TSVHelper.tsvToArray(nameSourceFile, 4,false);
		} catch (FileNotFoundException e)
		{			
			JOptionPane.showMessageDialog(this,"Could not load name source file "+nameSourceFile.getPath()+". Exiting Evaluation Tool.");
			System.exit(1);
		}
		finally
		{
			nameSources = nameSourcesIntermediate;
			if(nameSources.length==0)
			{
				JOptionPane.showMessageDialog(this,"Name source file "+nameSourceFile.getPath()+" is empty (or every line invalid). Exiting Evaluation Tool.");
				System.exit(1);				
			}
		}			
	}

	private void autoload()
	{
		try
		{
			setLoadLimit(Integer.valueOf(FileUtils
					.readFileToString(loadLimitFile)));

		} catch (FileNotFoundException e)
		{
			System.out.println("load limit file " + loadLimitFile
					+ " not found, using default value (" + loadLimit + ").");
		} catch (NumberFormatException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			setAutoEvalDistance(Integer.valueOf(FileUtils
					.readFileToString(autoEvalDistanceFile)));

		} catch (FileNotFoundException e)
		{
			System.out.println("auto eval distance file " + autoEvalDistanceFile
					+ " not found, using default value (" + autoEvalDistance + ").");
		} catch (NumberFormatException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		//		try
		//		{
		//			Scanner in = new Scanner(autoload);
		//			File xml = new File(in.nextLine());
		//			loadReferenceXML(xml);
		//			in.close();
		//		} catch (FileNotFoundException e)
		//		{
		//			System.out.println("last used alignment file " + loadLimitFile
		//					+ " not found, starting empty.");
		//		} catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}
	}

	void startLabelThread() {startLabelThread(false,false);}

	void startLabelThread(boolean nameToURI, boolean reloadMode)
	{
		stopLabelThread();
		labelThread = new LabelThread(nameToURI,reloadMode, cellPanels, nameSources, this);
		labelThread.start();	
	}

	void stopLabelThread()
	{
		if(labelThread!=null) {labelThread.stopIt();}
	}

	public void queryAllNames()
	{

	}

	private void clearCellPanels()
	{
		if(labelThread!=null&&labelThread.isAlive())
		{
			labelThread.interrupt();
			labelThread.stopIt();
			while(labelThread.isAlive()) {/*active waiting*/}
		}
		synchronized(cellPanels)
		{
			this.cellPanels.clear();
			mainPanel.removeAll();
			mainPanel.updateUI();				
		}
	}

	//	public void loadReferenceXML(File file)
	//	{
	//		List<link> links = new LinkedList<link>();
	//		try
	//		{
	//			links = AlignmentHelper.loadlinks(file,loadLimit);
	//			clearCellPanels();
	//
	//			for(link link: links)
	//			{
	//				CellPanel cellPanel = new CellPanel(this,link);
	//				this.cellPanels.add(cellPanel);
	//				this.mainPanel.add(cellPanel);
	//			}		
	//			mainPanel.updateUI();
	//			startLabelThread(false,false);		
	//		}
	//		catch (Exception e)
	//		{
	//			SwingHelper.showExceptionDialog(this,e,"Loading failed.");			
	//		}
	//	}

	private String codeEntityPair(String entity1, String entity2)
	{
		return entity1+'\t'+entity2;
	}

	//	@SuppressWarnings("unchecked")
	//	public void loadEvaluationXML(File file) throws FileNotFoundException
	//	{
	//		int numberOfCorrectEntries = 0;
	//		int numberOfInCorrectEntries = 0;
	//		if (!file.exists()) throw new FileNotFoundException(file.toString());
	//
	//		Document doc = null;
	//		try	{doc = new SAXBuilder().build(file);}
	//		catch (Exception e) {showExceptionDialog(e);}
	//
	//		Namespace alignmentNS =
	//			Namespace.getNamespace("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#");
	//		Namespace rdfNS =
	//			Namespace.getNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	//
	//		Element rootElement = doc.getRootElement();
	//		Element alignmentElement = (Element) rootElement.getChild("Alignment",
	//				alignmentNS);
	//
	//		List<Element> mapElements = new ArrayList<Element>(alignmentElement
	//				.getChildren("map", alignmentNS));
	//
	//		if(mapElements.size()>cellPanels.size())
	//		{
	//			JOptionPane.showMessageDialog(this,"Evaluation file contains more items than the reference alignment and so can not possibly fit.");
	//			return;
	//		}
	//
	//		// index reference alignment
	//		Map<String,CellPanel> cellPanelMap = new HashMap<String,CellPanel>();
	//		for(CellPanel cellPanel : cellPanels)
	//		{
	//			cellPanelMap.put(codeEntityPair(cellPanel.link.uris.first,cellPanel.link.uris.second), cellPanel);
	//		}
	//		// try to associate all map elements with their partner panels and throw an error if it fails for any one element
	//		for (Element mapElement : mapElements)
	//		{
	//			Element cellElement = mapElement.getChild("Cell", alignmentNS);
	//			String entity1 = cellElement.getChild("entity1", alignmentNS)
	//			.getAttributeValue("resource", rdfNS);
	//			String entity2 = cellElement.getChild("entity2", alignmentNS)
	//			.getAttributeValue("resource", rdfNS);
	//
	//			CellPanel cellPanel = cellPanelMap.get(codeEntityPair(entity1,entity2));
	//			if(cellPanel==null)
	//			{
	//				JOptionPane.showMessageDialog(this,"Loading failed! Evaluation file does not fit with reference alignment.");
	//				return;
	//			}
	//			double correctness = Double.valueOf(cellElement.getChildText(
	//					"measure", alignmentNS));
	//			synchronized(cellPanels)
	//			{
	//				if(cellPanel.link.correctness==null) {sampleSize++;}
	//				if(correctness==1)
	//				{
	//					cellPanel.link.correctness = Correctness.correct;
	//					numberOfCorrectEntries++;
	//
	//					cellPanel.correctButton.doClick();
	//					continue;
	//				}
	//				if(correctness==0)
	//				{
	//					cellPanel.link.correctness = Correctness.incorrect;
	//					numberOfInCorrectEntries++;
	//					cellPanel.incorrectButton.doClick();
	//					continue;
	//				}
	//			}
	//			JOptionPane.showMessageDialog(this,"Loading failed! Correctness values have to be either 1.0 or 0.0.");
	//			return;
	//		}		
	//		mainPanel.updateUI();
	//		System.out.println("Loading of evaluation as xml finished with a total of "+(numberOfCorrectEntries+numberOfInCorrectEntries)+" items marked. "+
	//				"number of correct matches: "+numberOfCorrectEntries+" number of incorrect matches: "+numberOfInCorrectEntries);
	//		updateTitle();
	//	}

	//	public void loadReferenceAlignmentTSV(File selectedFile) throws FileNotFoundException
	//	{
	//		Scanner in = new Scanner(selectedFile);
	//		String line;
	//		clearCellPanels();
	//		List<CellPanel> newCellPanels = new LinkedList<CellPanel>();
	//		int count = 0;
	//		String entity1 = null;
	//		String entity2 = null;
	//		while(in.hasNextLine())
	//		{
	//			count++;
	//			if(loadLimit>0&&count>loadLimit) {break;}
	//			if((count==1)||(count%1000==0))
	//			{
	//				this.setTitle("loading "+selectedFile.getName()+"... line "+count);
	//			}					
	//			line = in.nextLine();
	//			String[] tokens = line.split("\t");
	//			if(tokens.length!=3)
	//			{
	//				JOptionPane.showConfirmDialog(this,"wrong tsv. expected 3 columns in each line, tab separated. offending line:\""+line+"\"");
	//				return;
	//			}
	//			entity1 = tokens[0];
	//			entity2 = tokens[1];
	//			double confidence = Double.valueOf(tokens[2]);
	//
	//			CellPanel cellPanel = new CellPanel(this,new link(entity1, entity2, confidence));
	//			newCellPanels.add(cellPanel);
	//		}
	//		this.dataSourceName1 = EvaluationFrame.getProbableDatasourceName(entity1); 
	//		this.dataSourceName2 = EvaluationFrame.getProbableDatasourceName(entity2);
	//		updateTitle();
	//		this.cellPanels.addAll(newCellPanels);
	//		for(CellPanel cellPanel: newCellPanels)
	//		{
	//			mainPanel.add(cellPanel);
	//		}
	//		//		queryAllNames(true);//TODO wait for this one to finsh before doing it
	//		//queryAllNames(false);
	//		updateTitle();
	//		mainPanel.updateUI();
	//		in.close();
	//		startLabelThread();
	//	}

	//	public void loadReferenceAlignmentTSV(File selectedFile) throws FileNotFoundException
	//	{
	//		JOptionPane.showMessageDialog(this, "under construction");
	//		//		Scanner in = new Scanner(selectedFile);
	//		//		String line;
	//		//		// first line contains the table column labels
	//		//		in.nextLine();
	//		//		clearCellPanels();
	//		//		List<CellPanel> newCellPanels = new LinkedList<CellPanel>();
	//		//		int count = 0;
	//		//		while(in.hasNextLine()&&count<loadLimit)
	//		//		{
	//		//			count++;
	//		//			System.out.println("importing external format, line "+count);
	//		//			line = in.nextLine();
	//		//			// openoffice (and excel?) does this to rows if you dont change it
	//		//			//line.replaceAll("\"","");
	//		//			String[] tokens = line.split("\t");
	//		//			if(tokens.length<6) continue;
	//		//			String whoID = tokens[0];
	//		//			String cause = tokens[1];
	//		//			String causeSynonyms = tokens[2];
	//		//			String nctID = tokens[3];
	//		//			String condition = tokens[4];
	//		//			String conditionSynonyms = tokens[5];
	//		//			// german to english format which java needs
	//		//			String matchingPercentage = tokens[6].replace(',', '.');
	//		//
	//		//			String entity1 = MedicalMatchingHelper.getLinkedCTConditionURI(condition);
	//		//			String entity2 = MedicalMatchingHelper.getGHOCauseURI(cause);
	//		//
	//		//			CellPanel cellPanel = new CellPanel(entity1, entity2, Double.valueOf(matchingPercentage)/100);
	//		//			newCellPanels.add(cellPanel);
	//		//		}
	//		//		this.cellPanels.addAll(newCellPanels);
	//		//		for(CellPanel cellPanel: newCellPanels)
	//		//		{
	//		//			mainPanel.add(cellPanel);
	//		//		}
	//		////		queryAllNames(true);//TODO wait for this one to finsh before doing it
	//		//		//queryAllNames(false);
	//		//		mainPanel.updateUI();
	//		//		in.close();
	//		//		startLabelThread();
	//	}

	public void showExceptionDialog(Exception e)
	{
		JOptionPane.showMessageDialog(this,e);
	}

	public enum SaveXMLMode
	{
		SAVE_EVERYTHING, SAVE_CORRECT_ONLY, SAVE_INCORRECT_AS_SOMETHING_ELSE  
	}

	void saveXML(File file,SaveXMLMode mode) throws JDOMException, IOException
	{
		File ALIGNMENT_TEMPLATE = new File("config/alignment_template.xml");
		Document doc = new SAXBuilder().build(ALIGNMENT_TEMPLATE);
		
		Namespace alignmentNS = Namespace
		.getNamespace("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#");
		Namespace rdfNS = Namespace.getNamespace("rdf",
		"http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		Element rootElement = doc.getRootElement();
		Element alignmentElement = (Element) rootElement.getChild("Alignment",
				alignmentNS);

		for (CellPanel cellPanel : cellPanels)
		{
			if (cellPanel.link.correctness!=null)
			{
				if(mode==SaveXMLMode.SAVE_EVERYTHING||mode==SaveXMLMode.SAVE_INCORRECT_AS_SOMETHING_ELSE||cellPanel.link.correctness==Correctness.correct)
				{
					// example:
					// -----------------------------------------------------------------------------------------------------
					// <map>
					// <Cell>
					// <entity1
					// rdf:resource="http://data.linkedct.org/resource/condition/333"></entity1>
					// <entity2
					// rdf:resource="http://bio2rdf.org/mesh:D050659"></entity2>
					// <relation>=</relation>
					// <measure
					// rdf:datatype="http://www.w3.org/2001/XMLSchema#float">0.8034105534105536</measure>
					// </Cell>
					// </map>
					// -----------------------------------------------------------------------------------------------------
					Element mapElement = new Element("map", alignmentNS);
					alignmentElement.addContent(mapElement);
					Element cellElement = new Element("Cell", alignmentNS);
					mapElement.addContent(cellElement);

					Element entity1Element = new Element("entity1", alignmentNS);					
					Element entity2Element = new Element("entity2", alignmentNS);
					cellElement.addContent(entity1Element);
					cellElement.addContent(entity2Element);

					if(mode==SaveXMLMode.SAVE_INCORRECT_AS_SOMETHING_ELSE&&(cellPanel.link.correctness==Correctness.incorrect))
					{
						// not really elegant: modify the entries of cells marked as incorrect, so that the evaluator, who does not 
						// take into account the confidence values, does not find them in the reference 
						// alignment and recognizes the cells as incorrectly matched 
						entity1Element.setAttribute("resource", cellPanel.link.uris.first+System.currentTimeMillis(), rdfNS);
						entity2Element.setAttribute("resource", cellPanel.link.uris.second+System.currentTimeMillis(), rdfNS);						
					}
					else
					{
						entity1Element.setAttribute("resource", cellPanel.link.uris.first, rdfNS);
						entity2Element.setAttribute("resource", cellPanel.link.uris.second, rdfNS);
					}
					cellElement.addContent(new Element("relation", alignmentNS)
					.setText("="));
					cellElement
					.addContent(new Element("measure", alignmentNS)
					.setAttribute(
							"datatype",
							"http://www.w3.org/2001/XMLSchema#float",
							rdfNS).setText(
									cellPanel.link.correctness==Correctness.correct ? "1.00" : "0.00"));
				}
			}
		}
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		try
		{
			FileWriter out = new FileWriter(file);
			outputter.output(doc, out);
			out.close();
		} catch (Exception e)
		{
			System.out.println("Error saving to XML.");
			e.printStackTrace();
		}
	}

	void saveTSV(File file)
	{
		try
		{
			PrintWriter out = new PrintWriter(file);
			System.out.println(cellPanels.size());
			for (CellPanel efiPanel : cellPanels)
			{
				if (efiPanel.link.correctness!=null)
				{
					out.println(efiPanel.label1.getText() + '\t'
							+ efiPanel.label2.getText() + '\t'
							+ efiPanel.link.confidence + '\t' + efiPanel.link.correctness);
				}
			}
			out.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public void evaluate()
	{
		XYSeries precisionSeries = new XYSeries("Precision");
		XYSeries recallSeries = new XYSeries("Recall");
		XYSeries fScoreSeries = new XYSeries("F0.5-Score");

		final int SPACING = 1;
		Map<Integer, Integer> correctMatches = new HashMap<Integer, Integer>();
		Map<Integer, Integer> incorrectMatches = new HashMap<Integer, Integer>();
		Map<Integer, Double> precisions = new HashMap<Integer, Double>();

		double minCorrectness = Double.MAX_VALUE;

		int values = 0;

		for (CellPanel cellPanel : cellPanels)
		{
			if (cellPanel.link.correctness!=null)
			{
				values++;
				minCorrectness = Math.min(minCorrectness, cellPanel.link.confidence);
				for (int c = 0; c <= cellPanel.link.confidence * 100; c++)
				{
					if (cellPanel.link.correctness==Correctness.correct)
					{
						Integer i = correctMatches.get(c);
						if (i == null)
							i = 0;
						correctMatches.put(c, i + 1);
					} else
					{
						Integer i = incorrectMatches.get(c);
						if (i == null)
							i = 0;
						incorrectMatches.put(c, i + 1);
					}
				}
			}
		}

		for (int c = 0; c <= 100; c++)
		{
			Integer numberOfCorrectMatches = correctMatches.get(c);
			Integer numberOfIncorrectMatches = incorrectMatches.get(c);
			Double precision;
			Double recall;
			if (numberOfCorrectMatches == null
					&& numberOfIncorrectMatches == null)
			{
				//precision = null;
				precision = 0.0;
				recall = 0.0;
			} else
			{
				if (numberOfCorrectMatches == null)
					numberOfCorrectMatches = 0;
				if (numberOfIncorrectMatches == null)
					numberOfIncorrectMatches = 0;
				precision = 1.0 * numberOfCorrectMatches
				/ (numberOfCorrectMatches + numberOfIncorrectMatches);
				recall = (double)numberOfCorrectMatches/correctMatches.get(0);
			}
			precisions.put(c, precision);
			precisionSeries.add(new Double(c/100.0),precision);
			recallSeries.add(new Double(c/100.0),recall);
			fScoreSeries.add(new Double(c/100.0),new Double(Statistic.fScore(precision,recall,fScoreBeta)));
		}

		//		final double[] column = ArrayUtils.toPrimitive(precisions.values().toArray(new Double[0]));
		//		final double[][] data = new double[][] {
		//				column
		//		};
		


		//		DefaultKeyedValues keyedValues = new DefaultKeyedValues();
		//		DefaultKeyedValues keyedValuesRecall = new DefaultKeyedValues();
		System.out.println("minimum correctness is "+minCorrectness);//(int)(minCorrectness*100)
		//		for(int i=0;i<precisions.size();i+=SPACING)
		//		{
		//			precisionSeries.add(new Double(i/100.0),new Double(i/100.0));//precisions.get(i)
		////			keyedValues.addValue(new Double(i/100.0),precisions.get(i));
		////			keyedValues.addValue(new Double(i/100.0-0.1),precisions.get(i));
		//		}

		//		keyedValues.addValue(new Double(0),0.5);
		//		keyedValues.addValue(new Double(0.5),1);
		//		keyedValues.addValue(new Double(1),0.5);
		//final CategoryDataset dataset = DatasetUtilities.createCategoryDataset(99,keyedValues);
		//		JFreeChart chart = ChartFactory.createLineChart(
		//				"Evaluation", "confidence cutoff", "precision",
		//				dataset,PlotOrientation.VERTICAL,false,false,false);
		//
		//		ChartPanel chartPanel = new ChartPanel(chart,CHART_WIDTH,CHART_HEIGHT,CHART_WIDTH,CHART_HEIGHT,CHART_WIDTH,CHART_HEIGHT,true,false,false,false,false,false);
		//ChartFrame chartFrame = new ChartFrame("Evaluation",chart);
		//		JFrame chartFrame = new JFrame();
		//		chartFrame.add(chartPanel);
		precisionButton.addActionListener(listener);
		//chartPanel.add(precisionButton);
		final XYDataset precisionDataset = new XYSeriesCollection(precisionSeries);
		final XYDataset recallDataset = new XYSeriesCollection(recallSeries);
		final XYDataset fScoreDataset = new XYSeriesCollection(fScoreSeries);

		final EvaluationChartFrame chartFrame = new EvaluationChartFrame(this,"Evaluation Chart",precisionDataset,recallDataset,fScoreDataset);
		//chartFrame.pack();
		RefineryUtilities.centerFrameOnScreen(chartFrame);
		//chartFrame.setVisible(true);

		chartFrame.setVisible(true);
		chartFrame.pack();
		chartFrame.setLocationRelativeTo(null);
		System.out.println("Overall Precision: " + 1.0 * precisions.get(0));
		//		try {
		//			chartPanel.doSaveAs();
		//		} catch (IOException e)
		//		{
		//			SwingHelper.showExceptionDialog(this,e,"Error saving image of the chart.");
		//		}
	}

	public void saveReferenceXML(File selectedFile,boolean saveUnmarked)
	{
		try {EvaluationFrame.saveReferenceXML(selectedFile,cellPanels,saveUnmarked,false);}
		catch (JDOMException | IOException e) {JOptionPane.showConfirmDialog(this, e, "Error saving XML", JOptionPane.ERROR_MESSAGE);}
	}

	/**
	 * @param selectedFile
	 * @param cellPanels
	 * @param saveUnmarked If set to true, all cell panels contents will be saved, else only cell panels marked "correct" or "incorrect" will be saved. 
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public static void saveReferenceXML(File selectedFile,List<CellPanel> cellPanels,boolean saveUnmarked,boolean append) throws JDOMException, IOException
	{
		System.out.println("Saving reference xml");
		final File alignmentTemplate = append?selectedFile:new File("config/alignment_template.xml");
		Document doc = new SAXBuilder().build(alignmentTemplate);

		Namespace alignmentNS = Namespace
		.getNamespace("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#");
		Namespace rdfNS = Namespace.getNamespace("rdf",
		"http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		Element rootElement = doc.getRootElement();
		Element alignmentElement = (Element) rootElement.getChild("Alignment",
				alignmentNS);

		for (CellPanel cellPanel : cellPanels)
		{
			if(saveUnmarked||cellPanel.link.correctness!=null)
			{
				// example:
				// -----------------------------------------------------------------------------------------------------
				// <map>
				// <Cell>
				// <entity1
				// rdf:resource="http://data.linkedct.org/resource/condition/333"></entity1>
				// <entity2
				// rdf:resource="http://bio2rdf.org/mesh:D050659"></entity2>
				// <relation>=</relation>
				// <measure
				// rdf:datatype="http://www.w3.org/2001/XMLSchema#float">0.8034105534105536</measure>
				// </Cell>
				// </map>
				// -----------------------------------------------------------------------------------------------------
				Element mapElement = new Element("map", alignmentNS);
				alignmentElement.addContent(mapElement);
				Element cellElement = new Element("Cell", alignmentNS);
				mapElement.addContent(cellElement);
				Element entity1Element = new Element("entity1", alignmentNS);
				entity1Element
				.setAttribute("resource", cellPanel.link.uris.first, rdfNS);
				cellElement.addContent(entity1Element);
				Element entity2Element = new Element("entity2", alignmentNS);
				entity2Element
				.setAttribute("resource", cellPanel.link.uris.second, rdfNS);
				cellElement.addContent(entity2Element);
				cellElement.addContent(new Element("relation", alignmentNS)
				.setText("="));
				cellElement
				.addContent(new Element("measure", alignmentNS)
				.setAttribute(
						"datatype",
						"http://www.w3.org/2001/XMLSchema#float",
						rdfNS).setText(
								Double.toString(cellPanel.link.confidence)));
			}
		}
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		try
		{
			FileWriter out = new FileWriter(selectedFile);
			outputter.output(doc, out);
			out.close();
		} catch (Exception e)
		{
			System.out.println("Error saving to XML.");
			e.printStackTrace();
		}
	}

	//	public void loadReferenceNT(File selectedFile, double confidence) throws FileNotFoundException
	//	{
	//		this.clearCellPanels();
	//		Scanner in = new Scanner(selectedFile);		
	//		while(in.hasNextLine())
	//		{
	//			String line = in.nextLine();
	//			String[] tokens = line.split("\\s");
	//			// remove <> signs
	//			String entity1 = tokens[0].substring(1, tokens[0].length()-1);
	//			setProperty(tokens[1]);
	//			String entity2 = tokens[2].substring(1, tokens[2].length()-1);
	//			CellPanel cellPanel = new CellPanel(this,new link(entity1, entity2, confidence));
	//
	//			this.cellPanels.add(cellPanel);
	//			this.mainPanel.add(cellPanel);
	//		}
	//		mainPanel.updateUI();
	//		in.close();
	//		startLabelThread(false,false);
	//	}

	public void showPrecision()
	{	
		double threshold = 0;
		boolean ok;				
		do
		{
			try
			{
				String thresholdString = JOptionPane.showInputDialog("Threshold between 0.0 and 1.0: ");
				threshold = Double.valueOf(thresholdString.replace(',', '.'));
				ok = (threshold>=0&&threshold<=1); 
			}
			catch(Exception e)
			{
				ok = false;
			}
		} while(!ok);

		int correctMatches = 0;
		int incorrectMatches = 0;

		for (CellPanel cellPanel : cellPanels)
		{
			if (cellPanel.link.correctness!=null)
			{				
				if(cellPanel.link.confidence>=threshold)
				{
					if (cellPanel.link.correctness==Correctness.correct)
					{
						correctMatches++;
					} else
					{
						incorrectMatches++;
					}
				}
			}
		}
		JOptionPane.showMessageDialog(this,"The precision for threshold "+threshold+" is "
				+(correctMatches*1.0/(correctMatches+incorrectMatches)));
		//frame.loadReferenceNT(chooser.getSelectedFile(),confidence);
	}

	void evaluateAlign()
	{
		if(!autoload.exists())
		{
			JOptionPane.showMessageDialog(this,"You first have to load something to evaluate.");
			return;
		}
		try
		{
			final AlignmentParser aparser = new AlignmentParser(1);

			final File referenceFile = new File("temp/reference.xml");
			saveReferenceXML(referenceFile,true);
			Alignment referenceAlignment = aparser.parse(referenceFile.toURI());

			final File evaluationFile = new File("temp/evaluation.xml");
			saveXML(evaluationFile,SaveXMLMode.SAVE_INCORRECT_AS_SOMETHING_ELSE);
			Alignment evaluationAlignment = aparser.parse(evaluationFile.toURI());

			//evaluationAlignment.getElements()
			System.out.println(evaluationAlignment);

			//PRecEvaluator eval = new PRecEvaluator(evaluationAlignment,originalAlignment);
			PRecEvaluator eval = new PRecEvaluator(referenceAlignment,evaluationAlignment);
			Properties p = new BasicParameters();
			eval.eval(p);
			JOptionPane.showMessageDialog(this, eval.getResults()+"\n see console if you want to copy & paste this.");
			System.out.println(eval.getResults());
		}
		catch (Exception e){showExceptionDialog(e);}
	}

	public void updateTitle()
	{
		int mb = 1024*1024;
		Runtime runtime = Runtime.getRuntime();

		int used =  (int)((runtime.totalMemory() - runtime.freeMemory()) / mb);
		int max =  (int)((runtime.maxMemory() - runtime.freeMemory()) / mb);

		this.setTitle(DEFAULT_TITLE+" ("+sampleSize+'/'+cellPanels.size()+") "+dataSourceName1+'-'+dataSourceName2+ " RAM usage ("+used+"/"+max+") MB");
	}

	// TODO: later maybe more fast, now brute force	
	//	public Map<Pair<String>,Correctness> ntMap(String[][] rows)
	//	{		
	//		Map<String,String> map = new HashMap<String,String>();
	//		for(String[] row : rows)
	//		{
	//			map.put(row[0],row[1]);
	//		}
	//		return null;
	//	}

	public void loadPositiveNegativeNT(File selectedDirectory) throws FileNotFoundException
	{
		if(!selectedDirectory.isDirectory())
		{
			JOptionPane.showConfirmDialog(this,"File "+selectedDirectory+"is not a directory.  Aborting loading of evaluation");
			return;
		}
		File fPositive = new File(selectedDirectory.getAbsolutePath()+"/positive.nt"); 
		File fNegative = new File(selectedDirectory.getAbsolutePath()+"/negative.nt");
		File fUnsure = new File(selectedDirectory.getAbsolutePath()+"/unsure.nt");
		if(!(fPositive.isFile()&&fNegative.isFile()&&fUnsure.isFile()))
		{
			JOptionPane.showConfirmDialog(this,"One of positive.nt, negative.nt and unsure.nt does not exist. Aborting loading of evaluation.");
			return;			
		}
		String[][] positive = loadNT(fPositive);
		String[][] negative = loadNT(fNegative);
		String[][] unsure 	= loadNT(fUnsure);

		// brute force, slow but don't have much time now cause of paper deadline
		// if you use this often and its slow tell me and i optimize it
		for(CellPanel cellPanel: cellPanels)
		{
			for(String[] row : positive)
			{
				if(cellPanel.link.uris.first.equals(row[0])&&cellPanel.link.uris.second.equals(row[2]))
				{
					//System.out.println("bingo");
					cellPanel.correctButton.doClick();
				}
			}
			for(String[] row : negative)
			{
				if(cellPanel.link.uris.first.equals(row[0])&&cellPanel.link.uris.second.equals(row[2]))
				{
					cellPanel.incorrectButton.doClick();
				}
			}
			for(String[] row : unsure)
			{
				if(cellPanel.link.uris.first.equals(row[0])&&cellPanel.link.uris.second.equals(row[2]))
				{
					cellPanel.incorrectButton.doClick();
				}
			}
			mainPanel.updateUI();
		}
	}

	public String[][] loadNT(File f) throws FileNotFoundException
	{
		List<String[]> rows = new LinkedList<String[]>();
		Scanner in = new Scanner(f);
		while(in.hasNextLine())
		{
			String line = in.nextLine();
			String[] tokens = line.split("\\s");
			// remove <> signs
			String entity1 = tokens[0].substring(1, tokens[0].length()-1);
			setProperty(tokens[1]);
			String entity2 = tokens[2].substring(1, tokens[2].length()-1);
			String[] row = new String[] {entity1,getProperty(),entity2};
			rows.add(row);
		}
		in.close();
		return rows.toArray(new String[0][]);
	}


	//	public void loadGeographicalNT(final File selectedFile) throws FileNotFoundException
	//	{
	//		sampleSize = 0;
	//		isGeographic = true;
	//		this.clearCellPanels();
	//		final Scanner in = new Scanner(selectedFile);		
	//		// declare outside the loop because the last ones are needed for the datasource names
	//		String entity1 = null;
	//		String entity2 = null;
	//		int count = 0;
	//		while(in.hasNextLine())
	//		{
	//			count++;
	//			if(loadLimit>0&&count>loadLimit) {break;}
	//			if((count==1)||(count%1000==0))
	//			{
	//				this.setTitle("loading "+selectedFile.getName()+"... line "+count);
	//			}
	//			String line = in.nextLine();
	//			line = line.replaceAll("\\s+"," ");
	//			try
	//			{
	//				String[] tokens = line.split("\\s");
	//				// remove <> signs
	//				entity1 = tokens[0].substring(1, tokens[0].length()-1);
	//				setProperty(tokens[1]);
	//				entity2 = tokens[2].substring(1, tokens[2].length()-1);
	//				CellPanel cellPanel = new CellPanel(this,new link(entity1, entity2,1));
	//				this.cellPanels.add(cellPanel);			
	//			}
	//			catch(RuntimeException e)
	//			{
	//				System.err.println("problem with line "+count+":"+line);			
	//				throw e;
	//			}			
	//		}
	//		if(!LOADLIMIT_COUNT_ALL_LINES||loadLimit==0)
	//		{
	//			this.originalSize = count;
	//			in.close();
	//		} else
	//		{
	//			final int startCount = count;
	//			final EvaluationFrame frame = this;
	//			new Thread()
	//			{								
	//				@Override
	//				public void run()
	//				{
	//					int count = startCount;
	//					while(in.hasNextLine())
	//					{
	//						in.nextLine();
	//						count++;
	//						if((count==1)||(count%1000==0))
	//						{
	//							frame.setTitle("counting line numbers of "+selectedFile.getName()+"... line "+count);
	//						}
	//					}
	//					frame.originalSize = count;
	//					in.close();
	//					frame.updateTitle();
	//				}
	//			}.start();
	//		}
	//		// try find out data source names from the entities of the last line
	//		this.dataSourceName1 = EvaluationFrame.getProbableDatasourceName(entity1); 
	//		this.dataSourceName2 = EvaluationFrame.getProbableDatasourceName(entity2);
	//		this.updateTitle();
	//		System.out.println(cellPanels.size()+" entities loaded.");
	//		if(shuffle)
	//		{
	//			System.out.println("Shuffling the enties.");
	//			Collections.shuffle(cellPanels);
	//		}
	//		for(CellPanel cellPanel : cellPanels) {this.mainPanel.add(cellPanel);}
	//		this.shrinkedSize = cellPanels.size();
	//		mainPanel.updateUI();
	//		startLabelThread(false,false);
	//	}

	static String getProbableDatasourceName(String url)
	{
		if(url==null) return null;
		return url.replace("http://","").replace("www.","").split("\\.")[0];
	}

	public void savePositiveNegativeNT(File selectedFile) throws IOException
	{
		int numberOfLinks = originalSize;
		int sampleSize = 0;
		int numberOfPositives = 0;
		int numberOfNegatives = 0;
		// <3 Project Lombok Edit: does not seem to work, call close anyhow
		@Cleanup PrintWriter outPositive = new PrintWriter(selectedFile.getAbsolutePath()+"/positive.nt");
		@Cleanup PrintWriter outNegative = new PrintWriter(selectedFile.getAbsolutePath()+"/negative.nt");
		@Cleanup PrintWriter outUnsure = new PrintWriter(selectedFile.getAbsolutePath()+"/unsure.nt");
		for(CellPanel cellPanel: cellPanels)
		{
			if(cellPanel.link.correctness!=null)
			{
				sampleSize++;
				String link = "<"+cellPanel.link.uris.first+">\t<"+ getProperty()+">\t<"+cellPanel.link.uris.second+"> .";
				switch(cellPanel.link.correctness)
				{
				case correct:
				{
					outPositive.println(link);					 
					numberOfPositives++;
					break;					
				}
				case incorrect:
				{
					outNegative.println(link);
					numberOfNegatives++;
					break;
				}
				case unsure:
				{
					outUnsure.println(link);
					break;
				}
				}
			}
		}
		outPositive.close();
		outNegative.close();
		outUnsure.close();
		saveReadme(new File(selectedFile.getAbsolutePath()+"/README.txt"),numberOfLinks,sampleSize,numberOfPositives,numberOfNegatives);				
	}

	private void saveReadme(File file,int numberOfLinks, int sampleSize, int numberOfPositives, int numberOfNegatives) throws IOException
	{
		int numberOfUnsures = sampleSize-numberOfPositives-numberOfNegatives;
		String readme = FileUtils.readFileToString(readmeTemplateFile);
		readme = readme.replace("##date##",new Date().toString());
		readme = readme.replace("##number_of_links##",String.valueOf(numberOfLinks));
		readme = readme.replace("##sample_size##",String.valueOf(sampleSize));
		readme = readme.replace("##positive##",String.valueOf(numberOfPositives));
		readme = readme.replace("##negative##",String.valueOf(numberOfNegatives));
		readme = readme.replace("##unsure##",String.valueOf(numberOfUnsures));
		if(numberOfUnsures==0)
		{
			readme = readme.replace("##precision##",String.valueOf((double)numberOfPositives/(numberOfPositives+numberOfNegatives)));
		} else
		{
			double precisionLowestEstimate = (double)numberOfPositives/(sampleSize);
			double precisionHighestEstimate = (double)(numberOfPositives+numberOfUnsures)/(sampleSize);
			readme = readme.replace("##precision##",precisionLowestEstimate+"-"+precisionHighestEstimate+" (depending on the unsure links).");
		}
		FileUtils.writeStringToFile(file, readme);
	}

	public void autoEvaluate()
	{
		//TODO: function is quite slow, why is that? to much label updating?

		for(CellPanel cellPanel:  cellPanels)
		{
			// don't override manually evaluated items
			if(cellPanel.link.correctness!=null) {continue;}
			if
			(
					// in case of error, the labels either contain some message with an error or the name of the exception thrown
					cellPanel.label1.getText().contains("error")||
					cellPanel.label2.getText().contains("error")||
					cellPanel.label1.getText().contains("java.lang.")||
					cellPanel.label2.getText().contains("java.lang.")
			) {continue;}
			// ignore language tag
			if(cellPanel.label1.getText().split("@")[0].equals(cellPanel.label2.getText().split("@")[0]))
			{
				if(autoEvalDistance==-1||(cellPanel.getDistance()!=null&&cellPanel.getDistance()<=autoEvalDistance))
				{
					cellPanel.correctButton.doClick();
					cellPanel.link.correctness = Correctness.correct;
				}
			}
		}	
	}

	public void sortByCorrectness()
	{
		List<CellPanel> cellPanels = new LinkedList<CellPanel>(this.cellPanels);
		clearCellPanels();		
		Collections.sort(cellPanels, new Comparator<CellPanel>()
				{
			@Override
			public int compare(CellPanel panel1, CellPanel panel2)
			{
				if(panel1.link.correctness==null) return 1;
				if(panel2.link.correctness==null) return 0;
				return(Integer.valueOf(panel1.link.correctness.ordinal()).compareTo(Integer.valueOf(panel2.link.correctness.ordinal())));
			}});
		this.cellPanels.addAll(cellPanels);
		for(CellPanel cellPanel : cellPanels) {this.mainPanel.add(cellPanel);}
		mainPanel.updateUI();
	}

	/**
	 * 
	 */
	public void shrinkToLoadLimit()
	{	
		this.originalSize = cellPanels.size();
		List<CellPanel> shrinkedPanels = new LinkedList<CellPanel>();
		int count = 0;
		if(KEEP_EVALUTED_WHEN_SHRINKING)
		{
			for(CellPanel cellPanel: cellPanels)
			{
				if(cellPanel.link.correctness!=null)
				{
					shrinkedPanels.add(cellPanel);
					count++;
					if(count>=loadLimit) {break;}
				}
			}
		}
		cellPanels.removeAll(shrinkedPanels);
		for(int i=count;i<Math.min(loadLimit,cellPanels.size());i++)
		{
			shrinkedPanels.add(cellPanels.get(i));
		}		
		this.shrinkedSize = shrinkedPanels.size();
		clearCellPanels();
		cellPanels.addAll(shrinkedPanels);
		for(CellPanel shrinkedPanel: shrinkedPanels) {mainPanel.add(shrinkedPanel);}
		startLabelThread(false,false);
	}

	/**
	 * @param reference
	 */
	public void setReference(Reference reference)
	{
		this.reference = reference;
		stopLabelThread();
		if(reference.property!=null) {setProperty(reference.property);}
		updateTitle();
		clearCellPanels();
		for(Link link : reference.links)
		{
			cellPanels.add(new CellPanel(this, link));
		}
		updateCellPanels();
		mainPanel.updateUI();
		startLabelThread(false,false);
	}

	private void updateCellPanels()
	{
		mainPanel.removeAll();				
		for(CellPanel panel: cellPanels) {mainPanel.add(panel);}
		mainPanel.updateUI();
		updateTitle();
	}

	public void applyMetric(String name)
	{
		AbstractStringMetric metric = MetricHandler.createMetric(name);
		for(CellPanel cellPanel : cellPanels)
		{
			System.out.println(name);
			double score = metric.getSimilarity(cellPanel.label1.getText(), cellPanel.label2.getText());
			cellPanel.link.confidence = score;
		}
	}

	public void removeAllUnderAutoEvalDistance()
	{
		stopLabelThread();
		this.setTitle("Please wait, removing items...");
		List<CellPanel> newCellPanels = new LinkedList<CellPanel>();
		for(CellPanel cellPanel: cellPanels)
		{
			Double distance = cellPanel.getDistance();
			if(distance!=null&&distance<=autoEvalDistance)
			{
				newCellPanels.add(cellPanel);
			}
			else
			{
				this.reference.links.remove(cellPanel.link);
			}
		}
		
		this.cellPanels = newCellPanels;
		updateCellPanels();
		this.setTitle("Finished removing items");
	}
}