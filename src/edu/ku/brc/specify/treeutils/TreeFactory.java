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
	@SuppressWarnings("unchecked")
	public static TreeDefinitionIface setupNewTreeDef( Class treeNodeClass, String defName )
	{
		TreeDefinitionIface def = null;
		TreeDefinitionItemIface defItem = null;
		Treeable rootNode = null;
		if( treeNodeClass.equals(Geography.class) )
		{
			def = setupNewGeographyTree(defName);
			defItem = createNewTreeDefinitionItem(GeographyTreeDefItem.class, "Root");
			rootNode = createNewTreeable(Geography.class, "Root");
		}
		else if( treeNodeClass.equals(GeologicTimePeriod.class) )
		{
			def = setupNewGeologicTimePeriodTree(defName);
			defItem = createNewTreeDefinitionItem(GeologicTimePeriodTreeDefItem.class, "Root");
			rootNode = createNewTreeable(Geography.class, "Root");
		}
		else if( treeNodeClass.equals(Location.class) )
		{
			def = setupNewLocationTree(defName);
			defItem = createNewTreeDefinitionItem(LocationTreeDefItem.class, "Root");
			rootNode = createNewTreeable(Geography.class, "Root");
		}
		else if( treeNodeClass.equals(Taxon.class) )
		{
			def = setupNewTaxonTree(defName);
			defItem = createNewTreeDefinitionItem(TaxonTreeDefItem.class, "Root");
			rootNode = createNewTreeable(Geography.class, "Root");
		}

		defItem.setTreeDefinition(def);
		defItem.getTreeEntries().add(rootNode);
		def.getTreeDefItems().add(defItem);
		def.getTreeEntries().add(rootNode);
		rootNode.setDefItem(defItem);
		rootNode.setTreeDef(def);
		return def;
	}
	
	protected static GeographyTreeDef setupNewGeographyTree( String defName )
	{
		GeographyTreeDef def = new GeographyTreeDef();
		def.initialize();
		def.setName(defName);
		return def;
	}

	protected static GeologicTimePeriodTreeDef setupNewGeologicTimePeriodTree( String defName )
	{
		GeologicTimePeriodTreeDef def = new GeologicTimePeriodTreeDef();
		def.initialize();
		def.setName(defName);
		return def;
	}

	protected static LocationTreeDef setupNewLocationTree( String defName )
	{
		LocationTreeDef def = new LocationTreeDef();
		def.initialize();
		def.setName(defName);
		return def;
	}

	protected static TaxonTreeDef setupNewTaxonTree( String defName )
	{
		TaxonTreeDef def = new TaxonTreeDef();
		def.initialize();
		def.setName(defName);
		return def;
	}

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
			((GeographyTreeDef)def).initialize();
		}
		else if( implementingClass.equals(GeologicTimePeriodTreeDef.class) )
		{
			def = new GeologicTimePeriodTreeDef();
			((GeologicTimePeriodTreeDef)def).initialize();
		}
		else if( implementingClass.equals(LocationTreeDef.class) )
		{
			def = new LocationTreeDef();			
			((LocationTreeDef)def).initialize();
		}
		else if( implementingClass.equals(TaxonTreeDef.class) )
		{
			def = new TaxonTreeDef();
			((TaxonTreeDef)def).initialize();
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

	public static boolean isNodeDeletable( Treeable node )
	{
		if( node.getParentNode() == null )
		{
			// this is a root node
			return false;
		}
		
		if( node instanceof Geography )
		{
			return isGeographyDeletable( (Geography)node );
		}

		if( node instanceof GeologicTimePeriod )
		{
			return isGeologicTimePeriodDeletable( (GeologicTimePeriod)node );
		}

		if( node instanceof Location )
		{
			return isLocationDeletable( (Location)node );
		}

		if( node instanceof Taxon )
		{
			return isTaxonDeletable( (Taxon)node );
		}
		
		return false;

	}
	
	protected static boolean isGeographyDeletable( Geography geo )
	{
		return geo.getLocalities().isEmpty();
	}

	protected static boolean isGeologicTimePeriodDeletable( GeologicTimePeriod geo )
	{
		return false;
	}

	protected static boolean isLocationDeletable( Location geo )
	{
		return false;
	}

	protected static boolean isTaxonDeletable( Taxon geo )
	{
		return false;
	}
}
