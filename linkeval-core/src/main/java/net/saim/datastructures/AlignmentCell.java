package net.saim.datastructures;

import java.util.List;

import org.apache.commons.collections15.multimap.MultiHashMap;

// TODO: auf den enum Correctness umstellen
public class AlignmentCell
{
	public final String entity1;
	public final String entity2;
	public final double confidence;

	public AlignmentCell(String entity1, String entity2, double correctness)
	{	
		this.entity1 = entity1;
		this.entity2 = entity2;
		this.confidence = correctness;
	}
	
	public static MultiHashMap <String,String> entityMap(List<AlignmentCell> cells,boolean entity1ToEntity2)
	{
		MultiHashMap<String,String> entityMap = new MultiHashMap<String,String>();
		
		for(AlignmentCell cell: cells)
		{
			if(entity1ToEntity2)
			{
				entityMap.put(cell.entity1,cell.entity2);	
			}
			else
			{
				entityMap.put(cell.entity2,cell.entity1);				
			}			
		}
		return entityMap;
	}
}