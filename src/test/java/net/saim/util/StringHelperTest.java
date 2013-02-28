package net.saim.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class StringHelperTest
{

	@Test
	public void testGetCases()
	{
		assertTrue(
				new HashSet<String>(Arrays.asList(StringHelper.getCases("bao"))).equals(
						new HashSet<String>(Arrays.asList(new String[] {"bao","Bao"}))));
	}

}
