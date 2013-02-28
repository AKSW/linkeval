package net.saim.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.io.FileNotFoundException;

public class CSVHelperTest
{

	@Test
	public void testCsvToArray() throws FileNotFoundException
	{
		File f = new File("src/test/resources/csvhelpertest.csv");
		f.mkdirs();
		String[][] threeSquare = CSVHelper.csvToArray(f, 3);
		assertTrue(threeSquare[0].length==3);
		assertTrue(threeSquare.length==3);
		assertTrue(threeSquare[0][0].equals("bla"));
	}

}
