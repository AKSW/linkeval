package net.saim.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.io.FileNotFoundException;

public class TSVHelperTest
{

	@Test
	public void testCsvToArray() throws FileNotFoundException
	{
		File f = new File("src/test/resources/tsvhelpertest.tsv");
		f.mkdirs();
		String[][] threeSquare = TSVHelper.tsvToArray(f, 3);
		assertTrue(threeSquare[0].length==3);
		assertTrue(threeSquare.length==3);
		assertTrue(threeSquare[0][0].equals("bla"));
	}

}
