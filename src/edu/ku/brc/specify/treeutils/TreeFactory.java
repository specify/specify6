package edu.ku.brc.specify.treeutils;

import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.busrules.GeographyBusRules;
import edu.ku.brc.specify.datamodel.busrules.GeologicTimePeriodBusRules;
import edu.ku.brc.specify.datamodel.busrules.LocationBusRules;
import edu.ku.brc.specify.datamodel.busrules.TaxonBusRules;
import edu.ku.brc.ui.forms.BusinessRulesIFace;
import edu.ku.brc.util.Pair;

/**
 * A factory class for creating instances of the Treeable, TreeDefIface, and TreeDefinitionItemIface interfaces.
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
	 * Creates a new Treeable instance of the given <code>implementingClass</code> having the given parent, name,
	 * and rank.
	 * 
	 * @param implementingClass the class of the node to be created
	 * @param parent the parent of the new node
	 * @param name the name of the new node
	 * @param rank the rank of the new node
	 * @return the new Treeable node instance
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Treeable<T,?,?>> T createNewTreeable( Class<? extends T> implementingClass, String name )
	{
		T t = null;
		// big switch statement on implementingClass
		if( implementingClass.equals(Geography.class) )
		{
			t = (T)new Geography();
			((Geography)t).initialize();
		}
		else if( implementingClass.equals(GeologicTimePeriod.class) )
		{
			t = (T)new GeologicTimePeriod();
			((GeologicTimePeriod)t).initialize();
		}
		else if( implementingClass.equals(Location.class) )
		{
			t = (T)new Location();			
			((Location)t).initialize();
		}
		else if( implementingClass.equals(Taxon.class) )
		{
			t = (T)new Taxon();
			((Taxon)t).initialize();
		}
		else
		{
			throw new IllegalArgumentException("Provided class must be one of Geography, GeologicTimePeriod, Location or Taxon");
		}

		t.setName(name);

		return t;
	}
    
    @SuppressWarnings("unchecked")
    public static <T extends Treeable<T,?,?>> T createNewTreeable( T nodeOfSameClass, String name )
    {
        if (nodeOfSameClass instanceof Taxon)
        {
            return (T)createNewTreeable(Taxon.class,name);
        }
        if (nodeOfSameClass instanceof Geography)
        {
            return (T)createNewTreeable(Geography.class,name);
        }
        if (nodeOfSameClass instanceof GeologicTimePeriod)
        {
            return (T)createNewTreeable(GeologicTimePeriod.class,name);
        }
        if (nodeOfSameClass instanceof Location)
        {
            return (T)createNewTreeable(Location.class,name);
        }
        throw new IllegalArgumentException("Provided node must be instance of Geography, GeologicTimePeriod, Location or Taxon");
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
	@SuppressWarnings("unchecked")
	public static <I extends TreeDefItemIface<?, ?, I>> I createNewTreeDefItem( Class<? extends I> implementingClass, I parent, String name )
	{
		I t = null;
		
		// big switch statement on implementingClass
		if( implementingClass.equals(GeographyTreeDefItem.class) )
		{
			t = (I)new GeographyTreeDefItem();
		}
		else if( implementingClass.equals(GeologicTimePeriodTreeDefItem.class) )
		{
			t = (I)new GeologicTimePeriodTreeDefItem();
		}
		else if( implementingClass.equals(LocationTreeDefItem.class) )
		{
			t = (I)new LocationTreeDefItem();			
		}
		else if( implementingClass.equals(TaxonTreeDefItem.class) )
		{
			t = (I)new TaxonTreeDefItem();
		}
		else
		{
			return null;
		}
		t.initialize();
		
		if( parent != null )
		{
			t.setParent(parent);
		}
		if( name != null )
		{
			t.setName(name);
		}
		return t;
	}

//	/**
//	 * Find and return a <code>java.util.Comparator</code> appropriate for comparing <code>Treeable</code> objects
//	 * having the same implementation class as the given node.
//	 * 
//	 * @param node a node of the class tobe compared
//	 * @return a <code>Comparator</code> capable of properly comparing nodes of the same class as <code>node</code>
//	 */
//	public static <T extends Treeable<T,?,?>> Comparator<? super T> getAppropriateComparator( T node )
//	{
//		if( (new Class<T>()).equals(GeologicTimePeriod.class) )
//		{
//			return new GeologicTimePeriodComparator();
//		}
//		else if( nodeClass.equals(Taxon.class) )
//		{
//			return new TreeOrderSiblingComparator();
//		}
//		
//		return new NameBasedComparator();
//	}

	/**
	 * Find and return the names of the formset and view for editing tree nodes of the same class
	 * as the given <code>Treeable</code>.
	 * 
	 * @param node a node of the class to be edited using the returned formset and view
	 * @return a {@link edu.ku.brc.util.Pair<String,String>} containing the formset and view names
	 */
	public static Pair<String,String> getAppropriateFormsetAndViewNames( Object node )
	{
		if( node instanceof Geography )
		{
			return new Pair<String,String>("SystemSetup","Geography");
		}

		if( node instanceof GeologicTimePeriod )
		{
			return new Pair<String,String>("SystemSetup","GeologicTimePeriod");
		}

		if( node instanceof Location )
		{
			return new Pair<String,String>("SystemSetup","Location");
		}

		if( node instanceof Taxon )
		{
			return new Pair<String,String>("SystemSetup","Taxon");
		}
		
		if( node instanceof TreeDefIface<?,?,?>)
		{
			return new Pair<String,String>("SystemSetup","TreeDefEditor");
		}
		
		if( node instanceof TreeDefItemIface<?,?,?>)
		{
			TreeDefItemIface<?,?,?> defItem = (TreeDefItemIface<?,?,?>)node;
			if (defItem.getParent() == null)
			{
				return new Pair<String, String>("SystemSetup","RootTreeDefItem");
			}
			return new Pair<String, String>("SystemSetup","TreeDefItem");
		}
		
		return null;
	}
	
	public static LocationTreeDef createStdLocationTreeDef(String defName,String remarks)
	{
		LocationTreeDef def = new LocationTreeDef();
		def.initialize();
		def.setName(defName);
		def.setRemarks(remarks);
		
		LocationTreeDefItem defItem = new LocationTreeDefItem();
		defItem.initialize();
		defItem.setName("Root");
		defItem.setRankId(0);
		defItem.setIsEnforced(true);
		
		Location rootNode = new Location();
		rootNode.initialize();
		rootNode.setName("Root");
        rootNode.setFullName("Root");
		rootNode.setRankId(0);
        rootNode.setNodeNumber(1);
        rootNode.setHighestChildNodeNumber(1);
		
		// tie everything together
		defItem.setTreeDef(def);
		defItem.getTreeEntries().add(rootNode);
		def.getTreeDefItems().add(defItem);
		def.getTreeEntries().add(rootNode);
		rootNode.setDefinitionItem(defItem);
		rootNode.setDefinition(def);
		
		return def;
	}
    
    public static BusinessRulesIFace createBusinessRules(Object node)
    {
        if( node instanceof Geography )
        {
            return new GeographyBusRules();
        }

        if( node instanceof GeologicTimePeriod )
        {
            return new GeologicTimePeriodBusRules();
        }

        if( node instanceof Location )
        {
            return new LocationBusRules();
        }

        if( node instanceof Taxon )
        {
            return new TaxonBusRules();
        }
        
        return null;
    }

	@SuppressWarnings("unused")
	private Object[][] stdLocItems = {
			{   0,"Location Root",true},
            { 200,"Building",false},
         	{ 400,"Floor",false},
         	{ 600,"Room",true},
         	{ 800,"Shelf/Freezer",true}, };
	
    @SuppressWarnings("unused")
	private Object[][] stdGeoItems = {
    		{ 0, "Geography Root", true },
			{ 200, "Continent/Ocean", true },
			{ 400, "Country", false },
			{ 600, "State", true },
			{ 800, "County", false }, };

	@SuppressWarnings("unused")
	private Object[][] stdGtpItems = {
			{ 0, "Time Root", true },
			{ 200, "Erathem", false },
			{ 400, "Period", false },
			{ 600, "Epoch", false },
			{ 800, "Age", false }, };

	@SuppressWarnings("unused")
	private Object[][] stdTaxonItems = {
			{ 0, "Taxonomy Root", true },
			{ 100, "Kingdom", true },
			{ 200, "Subkingdom", false },
			{ 300, "Phylum", true },
			//	{ 300,"Division",true}, // botanical collections
			{ 400, "Subphylum", false },
			//	{ 400,"Subdivision",false}, // botanical collections
			{ 500, "Superclass", false },
			{ 600, "Class", true },
			{ 700, "Subclass", false },
			{ 800, "Infraclass", false },
			{ 900, "Superorder", false },
			{ 1000, "Order", true },
			{ 1100, "Suborder", false },
			{ 1200, "Infraorder", false },
			{ 1300, "Superfamily", false },
			{ 1400, "Tribe", false },
			{ 1500, "Subtribe", false },
			{ 1600, "Genus", true },
			{ 1700, "Subgenus", false },
			{ 1800, "Section", false },
			{ 1900, "Subsection", false },
			{ 2000, "Species", false },
			{ 2100, "Subspecies", false },
			{ 2200, "Variety", false },
			{ 2300, "Subvariety", false },
			{ 2400, "Forma", false },
			{ 2500, "Subforma", false } };
}
