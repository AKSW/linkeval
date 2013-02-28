package de.evaluationtool.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.evaluationtool.Reference;

public abstract class ReferenceFormat
{
	/** @return if the format needs a directory instead of a normal file as source (when the input consists of multiple files). */
	public boolean readsDirectory() {return false;}
	/** @return  if the format does not read a directory specifies the default file extension.*/
	public abstract String getFileExtension();
	/** @return if the format can be read */
	public boolean hasReadSupport() {return true;}
	/** @return if the format can be written */
	public boolean hasWriteSupport() {return true;}
	/** @return if the format includes (or may include) evaluation information (correctness values) */
	public abstract boolean includesEvaluation();

	/** performs a quick peek at a file and tries to see if it has the right format.
	 * @return true or false if the file seems to be valid/invalid, null if the format does not support quick peeks.*/
	public Boolean probablyValid(File f) {return null;}
	
	/**
	 * @param f a normal file or directory (depending on the reference format, most use normal file) that contains the reference data.
	 * @param loadLimit a positive integer denoting the number of links read from the beginning of the file, 0 if all links are to be read.
	 * @return a Reference object containing the first loadLimit links of the file
	 * @throws IOException 
	 */
	public abstract Reference readReference(File f, boolean includeEvaluation,int loadLimit) throws IOException;
	public abstract void writeReference(Reference reference, File f,boolean includeEvaluation) throws FileNotFoundException;
	public abstract String getDescription();
}