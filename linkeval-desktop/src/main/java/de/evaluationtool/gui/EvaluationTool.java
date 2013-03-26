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

import java.io.File;
import java.io.FileNotFoundException;


/**
 * Ideas for Version 0.3:
 *  <ul>
 * <li>Randomized samples -> now implemented, when load limit > 0 you always get a random sample</li>
 * <li>Hotkeys for correct and incorrect, marking the pairs sequentially with keyboard keys</li> 
 * <li>A different marking mode where the user gets displayed one matching pair at a time and responds with hotkeys.</li> 
 * <li>Loading of multiple titles at the same time via multithreading or combining multiple sparql queries together.</li> 
 * </ul> 
 * New in Version 0.2 (Beta):
 * <ul>
 * <li>The program does not crash anymore when a sparql connection fails. The titles will just display the class of the exception thrown.</li>
 * <li>Notable performance improvements have been achieved! This was done by carrying out the loading of the entities titles
 * in another thread which also uses a short delay (100 ms as of this writing, changable via the constant
 * in another thread which also uses a short delay (100 ms as of this writing, changable via the constant
 * EvaluationFrame.QUERY_DELAY). This means that you should be able to set the load limit to 0 (unlimited) in most cases.</li> 
 * <li>There is now an option to save as alignment format xml, in addition to saving as tsv.
 * Saving in alignment format will use the users input as correctness values whereas the correctness values are either 1.0 (marked as correct) or 0.0 (marked as incorrect).
 * As with the tsv save, only marked pairs will be saved.</li>
 * <li>The layout has been improved. The correctness buttons are now located to the left.
 * While this may be counterintuitive it makes sure that the layout of each column does not vary as wildly.
 * If you have a better idea for the layout please tell me.
 * Also, the two titles of each mapping pair are now located on top of each other to faciliate the human perception of the difference between the two.
 * <li> The load limit is now changable via the menu (0 means no load limit).
 * Also the load limit is now autosaved each time the load limit is changed and auto loaded each time the Evaluation Tool is started.</li>
 * <li>There is now a help menu which points to this documentation.</li>
 *  <!--<li></li>-->
 * </ul>
 *This GUI-based tool allows reading of files in the format of the alignment api which consists of 
 mappings of two entities each together with an estimated correctness value.
 The user then views the labels of those mappings and decides whether each mapping is correct or incorrect.
 Based on this input the subset of the input that is marked by the user can be saved as another file of either
 a tsv format or in the alignment format
 whereas the correctness values are either 1.0 (marked as correct) or 0.0 (marked as incorrect).
 This allows using the alignment api for evaluating the user-based correctness of the input mapping against correctness values from the input.  
 The alignment api homepage is <a href="http://alignapi.gforge.inria.fr">http://alignapi.gforge.inria.fr</a> at the moment of the writing of this comment.
 The alignment format is described in <a href="http://alignapi.gforge.inria.fr/format.html">http://alignapi.gforge.inria.fr/format.html</a> at the moment of the writing of this comment.
@author Konrad Höffner
@version 0.2
 */
public class EvaluationTool
{
	public static final File LOAD_LIMIT_FILE = new File("config/loadlimit.txt");
	public static final File AUTO_EVAL_DISTANCE_FILE = new File("config/autoevaldistance.txt");	
	public static final File NAMESOURCE_FILE = new File("config/namesources.tsv");
	public static final File README_TEMPLATE_FILE = new File("config/README.txt.template");

	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private static void createAndShowGUI() throws FileNotFoundException
	{		
		new EvaluationFrame(LOAD_LIMIT_FILE,AUTO_EVAL_DISTANCE_FILE,NAMESOURCE_FILE,README_TEMPLATE_FILE).setVisible(true);
	}

}
