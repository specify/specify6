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
import edu.ku.brc.util.NameBasedComparator;
import edu.ku.brc.util.Pair;

/**
 * A factory class for creating instances of the Treeable, TreeDefinitionIface, and TreeDefinitionItemIface interfaces.
 * The class also contains factory methods for creating appropriate comparators for comparing Treeables.  Also
 * included are some methods for determining if a given Treeable instance is deletable based on type-specific
 * business rules.
 * 
 * @author jstewart
 *
 */
public class TreeFactory
{
	/**
	 * Creates a new tree whos nodes will be of type <code>treeNodeClass</code>.  A new
	 * <code>TreeDefinitionIface</code>, <code>TreeDefinitionItemIface</code>, and <code>Treeable</code>
	 * node are all created.
	 * 
	 * @param treeNodeClass the node class of the new tree
	 * @param defName the name of the new tree definition
	 * @return the new <code>TreeDefinitionIface</code> instance
	 */
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
			rootNode = createNewTreeable(GeologicTimePeriod.class, "Root");
		}
		else if( treeNodeClass.equals(Location.class) )
		{
			def = setupNewLocationTree(defName);
			defItem = createNewTreeDefinitionItem(LocationTreeDefItem.class, "Root");
			rootNode = createNewTreeable(Location.class, "Root");
		}
		else if( treeNodeClass.equals(Taxon.class) )
		{
			def = setupNewTaxonTree(defName);
			defItem = createNewTreeDefinitionItem(TaxonTreeDefItem.class, "Root");
			rootNode = createNewTreeable(Taxon.class, "Root");
		}

		defItem.setTreeDefinition(def);
		defItem.getTreeEntries().add(rootNode);
		def.getTreeDefItems().add(defItem);
		def.getTreeEntries().add(rootNode);
		rootNode.setDefItem(defItem);
		rootNode.setTreeDef(def);
		return def;
	}
	
	public static TreeDefinitionIface setupNewTreeDef( TreeDefinitionIface def )
	{
		TreeDefinitionIface newDef = setupNewTreeDef(def.getNodeClass(),def.getName());
		newDef.setRemarks(def.getRemarks());
		return newDef;
	}
	
	/**
	 * Create, initialize, and name a new GeographyTreeDef instance.
	 * 
	 * @param defName the name to give the new instance
	 * @return the new definition instance
	 */
	protected static GeographyTreeDef setupNewGeographyTree( String defName )
	{
		GeographyTreeDef def = new GeographyTreeDef();
		def.initialize();
		def.setName(defName);
		return def;
	}

	/**
	 * Create, initialize, and name a new GeologicTimePeriodTreeDef instance.
	 * 
	 * @param defName the name to give the new instance
	 * @return the new definition instance
	 */
	protected static GeologicTimePeriodTreeDef setupNewGeologicTimePeriodTree( String defName )
	{
		GeologicTimePeriodTreeDef def = new GeologicTimePeriodTreeDef();
		def.initialize();
		def.setName(defName);
		return def;
	}

	/**
	 * Create, initialize, and name a new LocationTreeDef instance.
	 * 
	 * @param defName the name to give the new instance
	 * @return the new definition instance
	 */
	protected static LocationTreeDef setupNewLocationTree( String defName )
	{
		LocationTreeDef def = new LocationTreeDef();
		def.initialize();
		def.setName(defName);
		return def;
	}

	/**
	 * Create, initialize, and name a new TaxonTreeDef instance.
	 * 
	 * @param defName the name to give the new instance
	 * @return the new definition instance
	 */
	protected static TaxonTreeDef setupNewTaxonTree( String defName )
	{
		TaxonTreeDef def = new TaxonTreeDef();
		def.initialize();
		def.setName(defName);
		return def;
	}

	/**
	 * Creates a new Treeable instance of the given <code>implementingClass</code> having the given parent, name,
	 * and rank.
	 * 
	 * @param implementingClass the class of the node to be created
	 * @param parent the parent of the new node
	 * @param name the name of the new node
	 * @param rank the rank of the new node
	 * @return the new Treeable node instance
	 */
	public static Treeable createNewTreeable( Class implementingClass, String name )
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

		return t;
	}
		
	/**
	 * Creates a new tree definition instance having the given parent, name and rank.
	 * 
	 * @param implementingClass the implementation class of the nodes in the new tree
	 * @param name the name of the new tree
	 * @param remarks the remarks for the new definition
	 * @return the new definition
	 */
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
	
	/**
	 * Creates a new <code>TreeDefinitionItemIface</code> instance of the class <code>implementingClass</code>.
	 * The new item has the given parent and name.
	 * 
	 * @param implementingClass the implementation class of the item instance
	 * @param parent the items parent
	 * @param name the name of the item
	 * @return the new def item
	 */
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
		else
		{
			return null;
		}
		t.initialize();
		
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
	
	/**
	 * Creates a new <code>TreeDefinitionItemIface</code> instance.  The new item has the given parent and name.
	 * 
	 * @param parent the items parent
	 * @param name the name of the item
	 * @see TreeFactory#createNewTreeDefItem(Class, TreeDefinitionItemIface, String)
	 * @return the new def item
	 */
	public static TreeDefinitionItemIface createNewTreeDefinitionItem( TreeDefinitionItemIface parent, String name )
	{
		return createNewTreeDefItem(parent.getClass(),parent,name );
	}
	
	/**
	 * Creates a new <code>TreeDefinitionItemIface</code> instance.  The new item has the given parent and name.
	 * 
	 * @param implementingClass the implementation class of the item instance
	 * @param name the name of the item
	 * @see TreeFactory#createNewTreeDefItem(Class, TreeDefinitionItemIface, String)
	 * @return the new def item
	 */
	public static TreeDefinitionItemIface createNewTreeDefinitionItem( Class implementingClass, String name )
	{
		return createNewTreeDefItem(implementingClass,null,name);
	}

	/**
	 * Find and return a <code>java.util.Comparator</code> appropriate for comparing <code>Treeable</code> objects
	 * having the same implementation class as the given node.
	 * 
	 * @param node a node of the class tobe compared
	 * @return a <code>Comparator</code> capable of properly comparing nodes of the same class as <code>node</code>
	 */
	public static Comparator getAppropriateComparator( Treeable node )
	{
		Class nodeClass = node.getClass();
		if( nodeClass.equals(GeologicTimePeriod.class) )
		{
			return new GeologicTimePeriodComparator();
		}
		else if( nodeClass.equals(Taxon.class) )
		{
			return new TaxonComparator();
		}
		
		return new NameBasedComparator();
	}

	/**
	 * Find and return the names of the formset and view for editing tree nodes of the same class
	 * as the given <code>Treeable</code>.
	 * 
	 * @param node a node of the class to be edited using the returned formset and view
	 * @return a {@link edu.ku.brc.util.Pair<String,String>} containing the formset and view names
	 */
	public static Pair<String,String> getAppropriateFormsetAndViewNames( Treeable node )
	{
		if( node instanceof Geography )
		{
			return new Pair<String,String>("Fish Views","Geography");
		}

		if( node instanceof GeologicTimePeriod )
		{
			return new Pair<String,String>("Fish Views","GeologicTimePeriod");
		}

		if( node instanceof Location )
		{
			return new Pair<String,String>("Fish Views","Location");
		}

		if( node instanceof Taxon )
		{
			return new Pair<String,String>("Fish Views","Taxon");
		}
		
		return null;
	}

	/**
	 * Find and return the names of the formset and view for editing tree def items of the same class
	 * as the given <code>TreeDefinitionItemIface</code>.
	 * 
	 * @param defItem an instance of the class to be edited using the returned formset and view
	 * @return a {@link edu.ku.brc.util.Pair<String,String>} containing the formset and view names
	 */
	public static Pair<String,String> getAppropriateFormsetAndViewNames( TreeDefinitionItemIface defItem )
	{
		if( defItem instanceof GeographyTreeDefItem )
		{
			return new Pair<String,String>("Fish Views","GeographyTreeDefItem");
		}

		if( defItem instanceof GeologicTimePeriodTreeDefItem )
		{
			return new Pair<String,String>("Fish Views","GeologicTimePeriodTreeDefItem");
		}

		if( defItem instanceof LocationTreeDefItem )
		{
			return new Pair<String,String>("Fish Views","LocationTreeDefItem");
		}

		if( defItem instanceof TaxonTreeDefItem )
		{
			return new Pair<String,String>("Fish Views","TaxonTreeDefItem");
		}
		
		return null;
	}
	
	/**
	 * Find and return the names of the formset and view for editing tree def items of the same class
	 * as the given <code>TreeDefinitionItemIface</code>.
	 * 
	 * @param defItem an instance of the class to be edited using the returned formset and view
	 * @return a {@link edu.ku.brc.util.Pair<String,String>} containing the formset and view names
	 */
	public static Pair<String,String> getAppropriateFormsetAndViewNames( TreeDefinitionIface defItem )
	{
		return new Pair<String,String>("Fish Views","TreeDefEditor");
	}
	
	/**
	 * Find and return the names of the formset and view for editing objects of the same
	 * class as the provided obj.
	 * 
	 * @param obj an instance of the class to be edited using the returned formset and view
	 * @return a {@link edu.ku.brc.util.Pair<String,String>} containing the formset and view names
	 */
	public static Pair<String,String> getAppropriateFormsetAndViewNames( Object obj )
	{
		if(obj instanceof TreeDefinitionIface)
		{
			return getAppropriateFormsetAndViewNames((TreeDefinitionIface)obj);
		}
		else if(obj instanceof TreeDefinitionItemIface)
		{
			return getAppropriateFormsetAndViewNames((TreeDefinitionItemIface)obj);
		}
		else if(obj instanceof Treeable)
		{
			return getAppropriateFormsetAndViewNames((Treeable)obj);
		}
		else
		{
			return null;
		}
	}
}
