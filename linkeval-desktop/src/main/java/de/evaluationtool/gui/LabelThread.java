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
package de.evaluationtool.gui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;



/** @author Konrad Höffner */
public class LabelThread extends Thread 
{
	volatile boolean running = true;

	final boolean reloadMode;
	final boolean nameToURI;
	final List<CellPanel> cellPanels;
	final String[][] nameSources;
	final EvaluationFrame frame;
	boolean doCalculateDistance = true;

	public void stopIt()
	{
		running = false;
	}

	public LabelThread(final boolean nameToURI, final boolean reloadMode, final List<CellPanel> cellPanels, final String[][] nameSources, final EvaluationFrame frame)
	{
		this.nameToURI = nameToURI;
		this.reloadMode = reloadMode;
		this.cellPanels = cellPanels;
		this.nameSources = nameSources;
		this.frame = frame;		
	}

	public void run()
	{
		System.out.println("starting label thread");
		final int MAXIMUM_SIMULTANEUS_QUERIES = reloadMode?1:5;

		String[] firstEntities = {cellPanels.get(0).link.uris.first,cellPanels.get(0).link.uris.second};
		List<String[]> usedNameSourcesList = new LinkedList<String[]>();

		for(String[] nameSource : nameSources)
		{
			for(int i=0;i<2;i++)
			{
				if(firstEntities[i].startsWith(nameSource[0]))
				{										
					if(usedNameSourcesList.contains(nameSource))
					{
						// both data sources use the same name source
						break;
					}
					usedNameSourcesList.add(nameSource);
					if(usedNameSourcesList.size()==2) {break;}
				}
			}
		}
		if(usedNameSourcesList.isEmpty()) {System.out.println("No name source found. Finishing label thread.");return;}
		String[][] usedNameSources = usedNameSourcesList.toArray(new String[0][]);

		for(String[] nameSource : usedNameSources)
		{
			// both data sources must supply geocoordinates for distance calculation
			doCalculateDistance = doCalculateDistance && new Boolean(nameSource[3]);
		}

		//synchronized(cellPanels)
		//{
		//	System.out.println(cellPanels.size());
		for(int block = 0; block < (int)Math.ceil(cellPanels.size()*1.0/MAXIMUM_SIMULTANEUS_QUERIES);block++)
		{
			int offset = block*MAXIMUM_SIMULTANEUS_QUERIES;
			int blockSize = Math.min(MAXIMUM_SIMULTANEUS_QUERIES,cellPanels.size()-offset);

			if(reloadMode)
			{
				// only reload faulty ones
				if(!(cellPanels.get(block).label1.getText().startsWith("error")||
						cellPanels.get(block).label2.getText().startsWith("error")||
						cellPanels.get(block).label1.getText().startsWith("java.lang.")||
						cellPanels.get(block).label2.getText().startsWith("java.lang.")||
						(this.doCalculateDistance&&cellPanels.get(block).getDistance()==null)))
				{continue;}
			}

			String[] entities1 = new String[blockSize]; 
			String[] entities2 = new String[blockSize];
			String[] names1 = new String[blockSize];
			String[] names2 = new String[blockSize];

			for(int i=0;i<blockSize;i++)
			{
				if(!running)
				{
					System.out.println("Querying aborted.");
					return;
				}
				entities1[i]=cellPanels.get(i+offset).link.uris.first;
				entities2[i]=cellPanels.get(i+offset).link.uris.second;
			}
			final int MAX_RETRIES = 10;
			int retries = 0;
			boolean ok = false;
			while(!ok&&retries<=MAX_RETRIES)
			{
				try
				{
					names1 = CellPanel.getNames(entities1,usedNameSources,nameToURI?CellPanel.LINKEDCT_MODE:CellPanel.NORMAL_MODE);
					ok = true;						
				} catch (Exception e)
				{
					e.printStackTrace();
					retries++;
					//if(DEBUG) e.printStackTrace();
					Arrays.fill(names1, e.getClass().toString().split(" ")[1]);
				}					
			}
			
			retries = 0;
			ok = false;
			//				while(!ok&&retries<=MAX_RETRIES)
			//				{
			try
			{
				names2 = CellPanel.getNames(entities2,usedNameSources,nameToURI?CellPanel.LINKEDCT_MODE:CellPanel.NORMAL_MODE);
				ok = true;
			} catch (Exception e)
			{
				e.printStackTrace();
				retries++;
				//if(DEBUG) e.printStackTrace();
				Arrays.fill(names2, e.getClass().toString().split(" ")[1]);
			}					
			//				}
			retries = 0;
			ok = false;

			LatLng[] latLongs1 = new LatLng[blockSize];
			LatLng[] latLongs2 = new LatLng[blockSize];		

			if(doCalculateDistance)
			{
				while(!ok&&retries<=MAX_RETRIES)
				{
					try
					{
						latLongs1 = CellPanel.getLatitudesLongitudes(entities1,usedNameSources);					
						ok = true;

					} catch (Exception e)
					{
						retries++;
						//if(DEBUG) e.printStackTrace();
						//					Arrays.fill(latLongs1, null);
						//					Arrays.fill(latLongs2, null);
					}				
				}
				retries = 0;
				ok = false;
				while(!ok&&retries<=MAX_RETRIES)
				{
					try
					{			
						latLongs2 = CellPanel.getLatitudesLongitudes(entities2,usedNameSources);
						ok = true;

					} catch (Exception e)
					{
						retries++;
						//if(DEBUG) e.printStackTrace();
						//					Arrays.fill(latLongs1, null);
						//					Arrays.fill(latLongs2, null);
					}				
				}
			}

			for(int i=0;i<blockSize;i++)
			{
				cellPanels.get(i+offset).label1.setText(names1[i]);
				cellPanels.get(i+offset).label2.setText(names2[i]);
				//					LatLng point1 = new LatLng(latitudesLongitudes1[0][i], latitudesLongitudes1[1][i]);
				//					LatLng point2 = new LatLng(33.321321, -127.321321);
				//					double distanceInMiles = LatLngTool.distance(point1, point2, LengthUnit.MILE);
				if(doCalculateDistance)
				{
				try
				{
					cellPanels.get(i+offset).position1 = latLongs1[i];
					cellPanels.get(i+offset).position2 = latLongs2[i];
					cellPanels.get(i+offset).setDistance(LatLngTool.distance(latLongs1[i],latLongs2[i],LengthUnit.METER),"m");
				}
				catch(Exception e)
				{
					//cellPanels.get(i+offset).distanceLabel.setText("");//distance calculation not possible");
				}
				}
				else
				{
					//cellPanels.get(i+offset).distanceLabel.setText("");//distance calculation not possible");
				}
			}
		}
		frame.mainPanel.updateUI();
		//	}
		//			for (CellPanel cellPanel : cellPanels)
		//			{
		//				try
		//				{
		//					// ensure that the program itself is not slowed down
		//					Thread.sleep(QUERY_DELAY);
		//				} catch (InterruptedException e)
		//				{
		//					return; // label thread is only interrupted to be shut down
		//				} 
		//				queryAllNames(false);
		//				if(!running) return;
		//			}
		System.out.println("finishing label thread");			
	}		

}