package de.evaluationtool;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.NonNull;

// if you cant compile this class with eclipse or netbeans, you need to execute the jar from project lombok (http://projectlombok.org/)
@AllArgsConstructor
public class Reference
{
	@NonNull public Set<Link> links;
	public String property;

	/*public Reference(Set<Link> l,String p)
	{
		links=l;
		property=p;
	}*/
}