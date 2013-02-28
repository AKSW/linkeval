package de.evaluationtool;

import java.util.Set;
import lombok.AllArgsConstructor;

// if you cant compile this class with eclipse or netbeans, you need to execute the jar from project lombok (http://projectlombok.org/)
@AllArgsConstructor
public class Reference
{
	public Set<Link> links;
	public String property;
} 