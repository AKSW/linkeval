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
package de.evaluationtool.format;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;

/** @author Konrad Höffner */
public class ReferenceFormats
{
	public static final ReferenceFormat[] formatArray = {new ReferenceFormatNT(), new ReferenceFormatTSV(),new ReferenceFormatAlignment()};
	public static final Set<ReferenceFormat> formats = new HashSet<ReferenceFormat>(Arrays.asList(formatArray));
	
	/** singleton for accessing the nonstatic fields*/
	public static ReferenceFormats REFERENCE_FORMATS = new ReferenceFormats();
	public MultiMap<String,ReferenceFormat> extensionToFormats = new MultiHashMap<String, ReferenceFormat>();
	public Set<ReferenceFormat> directoryFormats = new HashSet<ReferenceFormat>();
	public Set<ReferenceFormat> readableFormats = new HashSet<ReferenceFormat>();
	public Set<ReferenceFormat> writeableFormats = new HashSet<ReferenceFormat>();
	public Set<ReferenceFormat> evaluationIncludingFormats = new HashSet<ReferenceFormat>();	
	
	private ReferenceFormats()
	{
		for(ReferenceFormat format: formatArray)
		{
			if(format.hasReadSupport()) {readableFormats.add(format);}
			if(format.hasWriteSupport()) {writeableFormats.add(format);}
			if(format.includesEvaluation()) {evaluationIncludingFormats.add(format);}
			
			if(format.readsDirectory()) {directoryFormats.add(format);continue;}
			extensionToFormats.put(format.getFileExtension(),format);
		}
	}
}