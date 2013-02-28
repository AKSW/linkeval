package de.evaluationtool;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.aksw.commons.collections.Pair;

@EqualsAndHashCode
@ToString
public class Link
{ 	
	public final Pair<String,String> uris;
	public Double confidence;
	public Correctness correctness = null;	
		
	public Link(String uri1, String uri2, Double confidence) {this(uri1, uri2, confidence, null);}

	public Link(String uri1, String uri2, Double confidence,Correctness correctness)
	{
		uris = new Pair<String, String>(uri1,uri2);
		if(confidence!=null&&(confidence<0||confidence>1)) { throw new IllegalArgumentException("confidence value must lie in [0,1].");}
		this.confidence = confidence;
		this.correctness = correctness;
	}
}