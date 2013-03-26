package net.saim.util;

public class StringHelper
{
	public static String[] getCases(String s)
	{
		return new String[] {s.substring(0,1).toLowerCase()+s.substring(1),s.substring(0,1).toUpperCase()+s.substring(1)};
	}
}