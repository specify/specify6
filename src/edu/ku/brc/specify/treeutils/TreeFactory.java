package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.util.Pair;

public class TreeFactory
{
	public static Treeable createNewTreeable( Class implementingClass, Treeable parent, String name, Integer rank )
	{
		Treeable t = null;
		// big switch statement on implementingClass
		if( implementingClass.equals(Geography.class) )
		{
			t = new Geography();
			((Geography)t).initialize();
		}
		else if( implementingClass.equals(GeologicTimePeriod.class) )
		{
			t = new GeologicTimePeriod();
			((GeologicTimePeriod)t).initialize();
		}
		else if( implementingClass.equals(Location.class) )
		{
			t = new Location();			
			((Location)t).initialize();
		}
		else if( implementingClass.equals(Taxon.class) )
		{
			t = new Taxon();
			((Taxon)t).initialize();
		}
		t.setName(name);

		if( parent != null )
		{
			parent.addChild(t);
			if( rank == null )
			{
				rank = TreeTableUtils.getRankOfChildren(parent);
			}
			t.setRankId(rank);
			t.setTreeDef(parent.getTreeDef());
		}
		return t;
	}
	
	public static Treeable createNewTreeable( Treeable parent, String name )
	{
		Integer rank = TreeTableUtils.getRankOfChildren(parent);
		return createNewTreeable( parent.getClass(), parent, name, rank );
	}
	
	public static Treeable createNewTreeable( Class implementingClass, String name )
	{
		return createNewTreeable(implementingClass,null,name,null);
	}

	public static Treeable createNewTreeable( Treeable parent, String name, Integer rankId )
	{
		return createNewTreeable( parent.getClass(), parent, name, rankId );
	}
	
	public static TreeDefinitionIface createNewTreeDef( Class implementingClass, String name, String remarks )
	{
		TreeDefinitionIface def = null;
		// big switch statement on implementingClass
		if( implementingClass.equals(GeographyTreeDef.class) )
		{
			def = new GeographyTreeDef();
		}
		else if( implementingClass.equals(GeologicTimePeriodTreeDef.class) )
		{
			def = new GeologicTimePeriodTreeDef();
		}
		else if( implementingClass.equals(LocationTreeDef.class) )
		{
			def = new LocationTreeDef();			
		}
		else if( implementingClass.equals(TaxonTreeDef.class) )
		{
			def = new TaxonTreeDef();
		}
		def.setName(name);
		def.setRemarks(remarks);
		return def;
	}
	
	public static TreeDefinitionItemIface createNewTreeDefItem( Class implementingClass, TreeDefinitionItemIface parent, String name )
	{
		TreeDefinitionItemIface t = null;
		// big switch statement on implementingClass
		if( implementingClass.equals(GeographyTreeDefItem.class) )
		{
			t = new GeographyTreeDefItem();
		}
		else if( implementingClass.equals(GeologicTimePeriodTreeDefItem.class) )
		{
			t = new GeologicTimePeriodTreeDefItem();
		}
		else if( implementingClass.equals(LocationTreeDefItem.class) )
		{
			t = new LocationTreeDefItem();			
		}
		else if( implementingClass.equals(TaxonTreeDefItem.class) )
		{
			t = new TaxonTreeDefItem();
		}
		if( parent != null )
		{
			t.setParentItem(parent);
		}
		if( name != null )
		{
			t.setName(name);
		}
		return t;
	}
	
	public static TreeDefinitionItemIface createNewTreeDefinitionItem( TreeDefinitionItemIface parent, String name )
	{
		return createNewTreeDefItem(parent.getClass(),parent,name );
	}
	
	public static TreeDefinitionItemIface createNewTreeDefinitionItem( Class implementingClass, String name )
	{
		return createNewTreeDefItem(implementingClass,null,name);
	}

	public static Comparator<Treeable> getAppropriateComparator( Treeable node )
	{
		Class nodeClass = node.getClass();
		if( nodeClass.equals(GeologicTimePeriod.class) )
		{
			return new GeologicTimePeriodComparator();
		}
		
		return new NameBasedTreeableComparator();
	}

	public static Pair<String,String> getAppropriateFormsetAndViewNames( Treeable node )
	{
		if( node instanceof Geography )
		{
			return new Pair<String,String>("Main Views","NewGeography");
		}

		if( node instanceof GeologicTimePeriod )
		{
			return new Pair<String,String>("Main Views","NewGeologicTimePeriod");
		}

		if( node instanceof Location )
		{
			return new Pair<String,String>("Main Views","NewLocation");
		}

		if( node instanceof Taxon )
		{
			return new Pair<String,String>("Main Views","NewTaxon");
		}
		
		return null;
	}
}
