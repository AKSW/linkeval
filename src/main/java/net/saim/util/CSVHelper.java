package net.saim.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CSVHelper
{

	public static String[][] csvToArray(File f, int columns) throws FileNotFoundException
	{
		return csvToArray(f,columns,true);
	}

	public static String[][] csvToArray(File f, int columns,boolean useFirstLine) throws FileNotFoundException
	{
		List<String[]> rows = new LinkedList<String[]>(); 
		Scanner in = new Scanner(f);
		if(!useFirstLine&&in.hasNextLine()) in.nextLine();

		while(in.hasNextLine())
		{
			String[] tokens = in.nextLine().split("\t");
			if(columns>0&&tokens.length<columns) continue;
			rows.add(tokens);
		}
		in.close();		
		return rows.toArray(new String[0][]);
	}

}