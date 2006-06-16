package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.Treeable;

public class NameBasedTreeableComparator implements Comparator<Treeable>
{
	public int compare(Treeable o1, Treeable o2)
	{
		return o1.getName().compareTo(o2.getName());
	}
}
