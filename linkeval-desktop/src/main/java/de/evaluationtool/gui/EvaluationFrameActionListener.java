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
 *
 */

package de.evaluationtool.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.jdom.JDOMException;
import org.jfree.ui.FilesystemFilter;

import de.evaluationtool.gui.EvaluationTool;
import de.evaluationtool.Link;
import de.evaluationtool.Reference;
import de.evaluationtool.format.ReferenceFormat;
import de.evaluationtool.format.ReferenceFormats;
import de.evaluationtool.gui.EvaluationFrame.SaveXMLMode;

/** @author Konrad Höffner */

class EvaluationFrameActionListener implements ActionListener
{
	final EvaluationFrame frame;
	private File geoFile = null;

	public EvaluationFrameActionListener(EvaluationFrame frame)
	{
		this.frame = frame;
	}

	//	private void loadReference()
	//	{
	//		JFileChooser chooser = new JFileChooser("Choose input alignment xml file");
	//		chooser.setCurrentDirectory(new File("input"));
	//		int returnVal = chooser.showOpenDialog(frame);
	//		if(returnVal == JFileChooser.APPROVE_OPTION)
	//		{
	//			try
	//			{	if(!frame.autoload.getParentFile().exists()) {frame.autoload.getParentFile().mkdir();}
	//			FileWriter out = new FileWriter(frame.autoload);
	//			out.write(chooser.getSelectedFile().getAbsolutePath());
	//			out.close();
	//			}
	//			//catch (FileNotFoundException e) {e.printStackTrace();}
	//			catch (IOException e){System.err.println("Warning: Saving the reference file name, so that it can be loaded automatically" +
	//			"at the next program start, not possible. However this is not critical, the program will still run.");	e.printStackTrace();}
	//			frame.loadReferenceXML(chooser.getSelectedFile());
	//		}
	//	}
	//
	//	private void loadEvaluationXML()
	//	{
	//		JFileChooser chooser = new JFileChooser("Choose input evaluation alignment xml file");
	//		chooser.setCurrentDirectory(new File("input"));
	//		int returnVal = chooser.showOpenDialog(frame);
	//		if(returnVal == JFileChooser.APPROVE_OPTION)
	//			try {	
	//				frame.loadEvaluationXML(chooser.getSelectedFile());
	//			}
	//		catch (FileNotFoundException e) {e.printStackTrace();} // won't happen
	//		//catch (IOException e){e.printStackTrace();}
	//	}
	//
	//	private void loadReferenceAlignmentTSV()
	//	{
	//		JFileChooser chooser = new JFileChooser("Choose input evaluation alignment tsv file");
	//		chooser.setCurrentDirectory(new File("input"));
	//		int returnVal = chooser.showOpenDialog(frame);
	//		if(returnVal == JFileChooser.APPROVE_OPTION)
	//			try
	//		{	
	//				frame.loadReferenceAlignmentTSV(chooser.getSelectedFile());
	//		}
	//		catch (FileNotFoundException e) {e.printStackTrace();} // won't happen
	//		//catch (IOException e){	e.printStackTrace();}		
	//	}
	//
	//	private void loadSameAsNT()
	//	{
	//		JFileChooser chooser = new JFileChooser("Choose input same as N Triples file");
	//		if(new File("/home/konrad/projekte/matching").exists())		
	//		{
	//			chooser.setCurrentDirectory(new File("/home/konrad/projekte/matching"));
	//		}
	//		else
	//		{
	//			chooser.setCurrentDirectory(new File("input"));			
	//		}
	//		int returnVal = chooser.showOpenDialog(frame);
	//		if(returnVal == JFileChooser.APPROVE_OPTION)
	//			try
	//		{	
	//				this.geoFile  = chooser.getSelectedFile();
	//				frame.loadGeographicalNT(chooser.getSelectedFile());
	//		}
	//		catch (FileNotFoundException e) {e.printStackTrace();} // won't happen
	//		//catch (IOException e){	e.printStackTrace();}		
	//	}

	//	private void convert()
	//	{
	//		JFileChooser inputChooser;
	//		JFileChooser outputChooser;
	//		{
	//			inputChooser = new JFileChooser("Choose input amrapalis format TSV file");
	//			inputChooser.setCurrentDirectory(new File("input"));
	//
	//			int returnVal = inputChooser.showOpenDialog(frame);
	//			if(!(returnVal == JFileChooser.APPROVE_OPTION))
	//			{
	//				return;
	//			}
	//		}
	//		boolean append = false;
	//		{
	//			outputChooser = new JFileChooser("Choose output reference alignment XML file");
	//			outputChooser.setCurrentDirectory(new File("input"));
	//			int returnVal = outputChooser.showSaveDialog(frame);
	//			if(!(returnVal == JFileChooser.APPROVE_OPTION))
	//			{
	//				return;
	//			}
	//			else
	//			{
	//				if(outputChooser.getSelectedFile().exists())
	//				{					
	//					int option = JOptionPane.showConfirmDialog(frame,
	//					"File already exists. Skip a number of lines equal to the entries already in the file and append the rest to it?"); 
	//					if(option == JOptionPane.CANCEL_OPTION) {return;}
	//					append = (option==JOptionPane.YES_OPTION);
	//				}
	//			}
	//		}
	//		try
	//		{	
	//			MedicalMatchingHelper.convert(inputChooser.getSelectedFile(), outputChooser.getSelectedFile(),append);
	//		}		
	//		catch (Exception e) {frame.showExceptionDialog(e);}
	//		//		catch (IOException e){	e.printStackTrace();}		
	//	}

	private void saveXML()
	{
		JFileChooser chooser = new JFileChooser("Save evaluation result as alignment XML");
		chooser.setCurrentDirectory(frame.defaultDirectory);
		int returnVal = chooser.showSaveDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			if(chooser.getSelectedFile().exists()&&
					(JOptionPane.showConfirmDialog(frame,"File already exists. Overwrite?")!= JOptionPane.YES_OPTION))
			{
				return;
			}
			try {frame.saveXML(chooser.getSelectedFile(),SaveXMLMode.SAVE_EVERYTHING);}
			catch (JDOMException | IOException e) {JOptionPane.showConfirmDialog(frame, e, "Error saving XML", JOptionPane.ERROR_MESSAGE);}
		}
	}     

	private void saveTSV()
	{
		JFileChooser chooser = new JFileChooser("Save evaluation result as TSV");
		chooser.setCurrentDirectory(frame.defaultDirectory);
		int returnVal = chooser.showSaveDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			if(chooser.getSelectedFile().exists()&&
					(JOptionPane.showConfirmDialog(frame,"File already exists. Overwrite?")!= JOptionPane.YES_OPTION))
			{
				return;
			}
			frame.saveTSV(chooser.getSelectedFile());
		}
	}     

	private void saveReferenceXML()
	{
		JFileChooser chooser = new JFileChooser
		("Save as alignment xml format. YOUR EVALUATION WILL NOT BE SAVED, ONLY A COPY OF THE INPUT.");
		chooser.setCurrentDirectory(new File("."));
		int returnVal = chooser.showSaveDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			if(chooser.getSelectedFile().exists()&&
					(JOptionPane.showConfirmDialog(frame,"File already exists. Overwrite?")!= JOptionPane.YES_OPTION))
			{
				return;
			}
			frame.saveReferenceXML(chooser.getSelectedFile(),true);
		}
	}

	private void savePositiveNegativeNT() throws IOException
	{
		JFileChooser chooser = new JFileChooser("Save as multiple nt files. Please choose a directory");
		chooser.setCurrentDirectory(frame.defaultDirectory);
		if(geoFile!=null) {chooser.setCurrentDirectory(geoFile);}
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		int returnVal = chooser.showSaveDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			frame.savePositiveNegativeNT(chooser.getSelectedFile());
		}		
	}

	private void loadPositiveNegativeNT() throws IOException
	{		
		JFileChooser chooser = new JFileChooser("Load multiple nt files. Please choose a directory");
		chooser.setCurrentDirectory(frame.defaultDirectory);
		if(geoFile!=null) {chooser.setCurrentDirectory(geoFile);}
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		int returnVal = chooser.showSaveDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			System.out.print("Loading...");
			frame.loadPositiveNegativeNT(chooser.getSelectedFile());
			System.out.println("loading finished.");
		}		
	}

	//	private void loadFormat(ReferenceFormat format) throws IOException
	//	{
	//		JFileChooser chooser = new JFileChooser("Load "+format.getDescription()+". Please choose a file.");
	//		chooser.setCurrentDirectory(frame.defaultDirectory);
	//		chooser.setFileSelectionMode(format.readsDirectory()?JFileChooser.DIRECTORIES_ONLY:JFileChooser.FILES_ONLY);
	//		chooser.setAcceptAllFileFilterUsed(true);
	//
	//		int returnVal = chooser.showOpenDialog(frame);
	//		if(returnVal == JFileChooser.APPROVE_OPTION)
	//		{
	//			System.out.print("Loading...");
	//			Reference reference = format.readReference(chooser.getSelectedFile(),frame.loadLimit);
	//			if(!reference.links.isEmpty())
	//			{
	//				Link firstLink = reference.links.iterator().next();
	//				frame.dataSourceName1 = EvaluationFrame.getProbableDatasourceName(firstLink.uris.first); 
	//				frame.dataSourceName2 = EvaluationFrame.getProbableDatasourceName(firstLink.uris.second);						
	//			}
	//			frame.setReference(reference);
	//
	//			//frame.loadPositiveNegativeNT(chooser.getSelectedFile());
	//			System.out.println("loading finished, "+reference.links.size()+" links loaded.");
	//		}				
	//	}

	private ReferenceFormat formatChooser(Collection<ReferenceFormat> formats)
	{
		ReferenceFormat[] formatsArray = formats.toArray(new ReferenceFormat[0]);
		String[] options = new String[formats.size()];
		for(int i=0;i<formats.size();i++)
		{
			options[i] = formatsArray[i].getDescription();
		}
		int result = JOptionPane.showOptionDialog(frame, "Please choose a reference format.",
				"Choose a reference format", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		if(result==JOptionPane.CLOSED_OPTION) return null;
		return formatsArray[result];
	}

	private void loadReference() throws Exception
	{
		JFileChooser chooser = new JFileChooser("Please choose a reference file or directory.");

		chooser.setCurrentDirectory(frame.defaultDirectory);
		// TODO: detect if all directory formats are not readable and in this case dont allow directory opening
		chooser.setFileSelectionMode(ReferenceFormats.REFERENCE_FORMATS.directoryFormats.isEmpty()?JFileChooser.FILES_ONLY:JFileChooser.FILES_AND_DIRECTORIES);
		for(ReferenceFormat format: ReferenceFormats.REFERENCE_FORMATS.readableFormats)
		{
			chooser.addChoosableFileFilter(new FilesystemFilter(format.getFileExtension(), format.getDescription()));
		}
		chooser.setAcceptAllFileFilterUsed(true);

		int returnVal = chooser.showOpenDialog(frame);
		if(returnVal!= JFileChooser.APPROVE_OPTION) {return;}

		ReferenceFormat format = null;
		System.out.print("Loading...");
		frame.setTitle("Loading...");
		File f = chooser.getSelectedFile();
		Collection<ReferenceFormat> formats; 

		if(f.isDirectory())	{formats = ReferenceFormats.REFERENCE_FORMATS.directoryFormats;}
		else				{formats = ReferenceFormats.REFERENCE_FORMATS.extensionToFormats.get(f.getName().substring(f.getName().lastIndexOf(".")+1));}

		if(formats==null||formats.isEmpty()) {throw new Exception
			("No format available that can read files with the "+f.getName().substring(f.getName().lastIndexOf(".")+1)+" extension.");}
		if(formats.size()==1)	{format = formats.iterator().next();}
		else 					{format = formatChooser(formats);}

		if(format==null) {return;}
		Reference reference = format.readReference(chooser.getSelectedFile(),true, frame.loadLimit);
		if(!reference.links.isEmpty())
		{
			Link firstLink = reference.links.iterator().next();
			frame.dataSourceName1 = EvaluationFrame.getProbableDatasourceName(firstLink.uris.first); 
			frame.dataSourceName2 = EvaluationFrame.getProbableDatasourceName(firstLink.uris.second);						
		}
		frame.setReference(reference);

		//frame.loadPositiveNegativeNT(chooser.getSelectedFile());
		System.out.println("loading finished, "+reference.links.size()+" links loaded.");

	}

	private void saveReference(boolean mustSupportEvaluation,boolean includeEvaluation) throws FileNotFoundException
	{
		Set<ReferenceFormat> formats = mustSupportEvaluation?ReferenceFormats.REFERENCE_FORMATS.evaluationIncludingFormats:ReferenceFormats.REFERENCE_FORMATS.formats;
		formats.retainAll(ReferenceFormats.REFERENCE_FORMATS.writeableFormats);

		ReferenceFormat format = formatChooser(formats);
		if(format==null) {return;}
		
		JFileChooser chooser = new JFileChooser("Save reference. Please choose a file.");
		chooser.setCurrentDirectory(frame.defaultDirectory);
		chooser.setFileSelectionMode(format.readsDirectory()?JFileChooser.DIRECTORIES_ONLY:JFileChooser.FILES_ONLY);
		if(format.getFileExtension()!=null) {chooser.addChoosableFileFilter(new FilesystemFilter(format.getFileExtension(), format.getDescription()));}
		else {chooser.setAcceptAllFileFilterUsed(true);}

		int returnVal = chooser.showSaveDialog(frame);
		if(returnVal != JFileChooser.APPROVE_OPTION) return;
		System.out.print("Saving...");
		format.writeReference(frame.reference,chooser.getSelectedFile(),includeEvaluation);
		//frame.loadPositiveNegativeNT(chooser.getSelectedFile());
		System.out.println("saving finished.");
	}

	//	private void saveFormat(ReferenceFormat format) throws FileNotFoundException
	//	{
	//		JFileChooser chooser = new JFileChooser("Save "+format.getDescription()+". Please choose a file.");
	//		chooser.setCurrentDirectory(frame.defaultDirectory);
	//		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	//		chooser.setAcceptAllFileFilterUsed(true);
	//
	//		int returnVal = chooser.showSaveDialog(frame);
	//		if(returnVal == JFileChooser.APPROVE_OPTION)
	//		{
	//			System.out.print("Saving...");
	//			format.writeReference(frame.reference,chooser.getSelectedFile());
	//			//frame.loadPositiveNegativeNT(chooser.getSelectedFile());
	//			System.out.println("saving finished.");
	//		}				
	//	}

	private void changeLoadLimit()
	{
		try
		{
			String input = JOptionPane.showInputDialog("Change the load limit to (0 means unlimited)", frame.getLoadLimit());
			if(input==null) return;
			frame.setLoadLimit(Integer.valueOf(input));

			FileUtils.writeStringToFile(frame.loadLimitFile,String.valueOf(frame.getLoadLimit()));
		}
		catch(NumberFormatException e)
		{
			changeLoadLimit();
		} catch (IOException e)
		{
			JOptionPane.showConfirmDialog(frame,e);
		}		
	}

	private void changeAutoEvalDistance()
	{
		try
		{
			String input = JOptionPane.showInputDialog("Change the auto eval distance to ", frame.getAutoEvalDistance());
			if(input==null) return;
			frame.setAutoEvalDistance(Integer.valueOf(input));			
			FileUtils.writeStringToFile(frame.autoEvalDistanceFile,String.valueOf(frame.getAutoEvalDistance()));
		}
		catch(NumberFormatException e)
		{
			changeLoadLimit();
		} catch (IOException e)
		{
			JOptionPane.showConfirmDialog(frame,e);
		}		
	}

	private void browse(String url) throws URISyntaxException, IOException
	{
		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
		java.net.URI uri = null;
		uri = new java.net.URI(url);
		desktop.browse( uri );
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		//if(!(event.getSource() instanceof JMenuItem)) return;
		//JMenuItem item = (JMenuItem) event.getSource();
		try
		{
			if(event.getSource()==frame.loadReferenceItem) {loadReference();}
			if(event.getSource()==frame.saveReferenceOnlyItem) {saveReference(false,false);}
			if(event.getSource()==frame.saveReferenceAndEvaluationItem) {saveReference(true,true);}		
			if(event.getSource()==frame.removeAllUnderAutoEvalDistanceItem) {frame.removeAllUnderAutoEvalDistance();}		

			//			// load
			//			for(ReferenceFormat format: ReferenceFormats.referenceFormats)
			//			{
			//				// TODO: maybe a bit unstable? if someone changes the menu item label then the action listener does not work anymore
			//				if(item.getText().equals("Load "+format.getDescription()))
			//				{
			//					loadFormat(format);
			//				};
			//				if(item.getText().equals("Save "+format.getDescription()))
			//				{
			//					saveFormat(format);
			//				};
			//			}

			//			if(event.getSource()==frame.loadSameAsNTItem) {loadSameAsNT();}
			//			if(event.getSource()==frame.loadReferenceItem) {loadReference();}
			//			if(event.getSource()==frame.loadReferenceNTItem) {loadReferenceNT();}		
			//			if(event.getSource()==frame.loadEvaluationXMLItem) {loadEvaluationXML();}
			//			//if(event.getSource()==frame.loadReferenceTSVItem) {loadReferenceTSV();}		
			//			if(event.getSource()==frame.loadPositiveNegativeNTItem) {loadPositiveNegativeNT();}	
			//			if(event.getSource()==frame.loadReferenceAlignmentTSVItem) {loadReferenceAlignmentTSV();}


			// save
			//			if(event.getSource()==frame.savePositiveNegativeNTItem)	{savePositiveNegativeNT();}
			//			if(event.getSource()==frame.saveXMLItem) {saveXML();}
			//			if(event.getSource()==frame.saveTSVItem) {saveTSV();}

			// options
			// operations
			if(event.getSource()==frame.reloadLabelsItem) {frame.startLabelThread(false,true);}
			if(event.getSource()==frame.autoEvalItem) {frame.autoEvaluate();}
			if(event.getSource()==frame.sortByCorrectnessItem) {frame.sortByCorrectness();}

			if(event.getSource()==frame.evaluateItem) {frame.evaluate();}
			if(event.getSource()==frame.evaluateAlignItem) {frame.evaluateAlign();}
			if(event.getSource()==frame.changeAutoEvalDistanceItem) {changeAutoEvalDistance();}
			if(event.getSource()==frame.changeLoadLimitItem) {changeLoadLimit();}
			if(event.getSource()==frame.shrinkToLoadLimitItem) {frame.shrinkToLoadLimit();}
			if(event.getSource()==frame.javadocMenuItem) {browse("file://"+System.getProperty("user.dir")+"/doc/"+EvaluationTool.class.toString().split(" ")[1].replace('.', '/')+".html");}
			if(event.getSource()==frame.manualMenuItem) {browse("file:///home/konrad/projekte/www/evaluationtool/index.html");}
			if(event.getSource()==frame.saveReferenceXMLItem) {saveReferenceXML();}
			//if(event.getSource()==frame.convertItem) {convert();}
			if(event.getSource()==frame.precisionButton) {frame.showPrecision();}
			if(event.getSource()==frame.editNameSourceFileItem) {editNameSourceFile();}
			if(event.getSource()==frame.reloadNameSourceFileItem) {reloadNameSourceFile();}
			//			if(event.getSource()==frame.editConfigurationFileItem) {editConfigurationFile();}
			//			if(event.getSource()==frame.reloadConfigurationFileItem) {reloadConfigurationFile();}

		} catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Exception: "+e);
		}
	}

	private void editNameSourceFile() throws IOException, InterruptedException
	{
		try
		{
			Desktop desktop = Desktop.getDesktop();
			desktop.edit(new File("config/namesources.tsv"));
		}
		catch(Exception e)
		{
			//Fallback
			final Process p = Runtime.getRuntime().exec("gedit config/namesources.tsv");
			if(p.waitFor()!=0)
			{
				final Process q = Runtime.getRuntime().exec("edit config/namesources.tsv");
			}

			//	(new Thread() {			
			//				@Override
			//				public void run()
			//				{
			//					try {
			//						// normal termination
			//						int returnCode = p.waitFor();
			//						System.out.println(returnCode);
			//						if(returnCode==0)
			//						{
			//							System.out.println("terminated!!");
			//							frame.reloadNamesources();
			//							frame.startLabelThread();
			//						}
			//					} catch (InterruptedException e) {}				
			//				}
			//			}).start();
		}
	}

	private void reloadNameSourceFile()
	{
		frame.stopLabelThread();
		frame.reloadNamesources();
		frame.startLabelThread();
	}

	//			private void reloadConfigurationFile()
	//			{
	//			}

	//	private void loadReferenceNT()
	//	{
	//		try
	//		{	
	//			JFileChooser chooser = new JFileChooser("Choose n triples reference file");
	//			chooser.setCurrentDirectory(new File("input"));
	//			int returnVal = chooser.showOpenDialog(frame);
	//			if(returnVal == JFileChooser.APPROVE_OPTION)
	//			{
	//				double confidence = 0;
	//				boolean ok;				
	//				do
	//				{
	//					try
	//					{
	//						String confidenceString = JOptionPane.showInputDialog("Please enter the assigned confidence value for all items (between 0 and 1).");
	//						confidence = Double.valueOf(confidenceString.replace(',', '.'));
	//						ok = (confidence>=0&&confidence<=1); 
	//					}
	//					catch(Exception e)
	//					{
	//						ok = false;
	//					}
	//				} while(!ok);
	//
	//				frame.loadReferenceNT(chooser.getSelectedFile(),confidence);
	//			}
	//		}
	//		catch(Exception e)
	//		{
	//			frame.showExceptionDialog(e);
	//		}
	//	}

}