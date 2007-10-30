/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.List;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadTableTree extends UploadTable
{
	protected Table baseTable; 
	protected UploadTableTree parent;
    protected Integer rank;
    private static GeographyTreeDef geoTreeDef = null;
    private static TaxonTreeDef taxTreeDef = null;
    

	/**
	 * @param table
	 * @param baseTable
	 * @param parent
	 * @param required
	 * @param rank
	 */
	public UploadTableTree(Table table, Table baseTable, UploadTableTree parent, boolean required, Integer rank) 
	{
		super(table, null);
        this.baseTable = baseTable;
		this.parent = parent;
		this.required = required;
        this.rank = rank;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#determineTblClass()
     */
    @Override
    public void determineTblClass() throws UploaderException
    {
        try
        {
            tblClass = Class.forName("edu.ku.brc.specify.datamodel." + baseTable.getName());
        }
        catch (ClassNotFoundException cnfEx)
        {
            throw new UploaderException(cnfEx, UploaderException.ABORT_IMPORT);
        }
    }

	/**
	 * @return the baseTable
	 */
	public Table getBaseTable()
	{
		return baseTable;
	}
	/**
	 * @param baseTable the baseTable to set
	 */
	public void setBaseTable(Table baseTable)
	{
		this.baseTable = baseTable;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getWriteTable()
	 */
	@Override
	public Table getWriteTable()
	{
		return getBaseTable();
	}
	    
    /**
     * @return The TreeDef for the tblClass.
     * @throws UploaderException
     */
    @SuppressWarnings("unchecked")
    protected TreeDefIface getTreeDef() throws UploaderException
    {
        if (tblClass == Taxon.class)
        {
            return getTaxTreeDef();
        }
        if (tblClass == Geography.class)
        {
            return getGeoTreeDef();
        }
        throw new UploaderException("unable to find treedef for " + tblClass.getName(), UploaderException.ABORT_IMPORT);
    }
    
    /**
     * @return a TreeDef for class Taxon
     */
    protected TaxonTreeDef getTaxTreeDef()
    {
        if (taxTreeDef == null)
        {
            taxTreeDef = (TaxonTreeDef)getTreeDef("TaxonTreeDef");
        }
        return taxTreeDef;
    }
 
    /**
     * @return a TreeDef for class Geography
     */
    protected GeographyTreeDef getGeoTreeDef()
    {
        if (geoTreeDef == null)
        {
            geoTreeDef = (GeographyTreeDef)getTreeDef("GeographyTreeDef");
        }
        return geoTreeDef;
    }

    
    /**
     * @return TreeDefItem corresponding to tblClass
     * @throws UploaderException
     */
    @SuppressWarnings("unchecked")
    protected TreeDefItemIface getTreeDefItem() throws UploaderException
    {
        if (tblClass == Taxon.class)
        {
            return getTaxTreeDefItem();
        }
        if (tblClass == Geography.class)
        {
            return getGeoTreeDefItem();
        }
        throw new UploaderException("unable to find treedefitem for " + baseTable.getName(), UploaderException.ABORT_IMPORT);
    }
    

    /**
     * @return TreeDefItem for class Taxon
     */
    protected TaxonTreeDefItem getTaxTreeDefItem()
    {
        return getTaxTreeDef().getDefItemByRank(rank);
    }
 
    /**
     * @return TreeDefItem for class Geography
     */
    protected GeographyTreeDefItem getGeoTreeDefItem()
    {
        return getGeoTreeDef().getDefItemByRank(rank);
    }

    
    /**
     * @param defName
     * @return TreeDef loaded from table named defName.
     */
    @SuppressWarnings("unchecked")
    public DataModelObjBase getTreeDef(final String defName)
    {
        String hql = "from " + defName;
        //Session session = getNewSession();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();        
        try
        {
            QueryIFace q = session.createQuery(hql);
            List<?> matches = q.list();
            if (matches.size() == 0) { return null; }
                return (DataModelObjBase)matches.get(0);
        }
        finally
        {
            session.close();
        }
    }
    
    /**
     * @param jc the JoinColumn annotation for the relationship.
     * @return true if the related class needs to be added as a requirement.
     */
    @Override
    protected boolean addToReqRelClasses(Class<?> relatedClass)
    {
        return super.addToReqRelClasses(relatedClass) && relatedClass != GeographyTreeDef.class && relatedClass != GeographyTreeDefItem.class
          && relatedClass != TaxonTreeDef.class && relatedClass != TaxonTreeDefItem.class;
    }
    
    /**
     * @param rec
     * @param recNum
     * 
     * Performs extra tasks to get rec ready to be saved to the database.
     * The unchecked cast to Treeable is a little scary, but eliminates need to
     * update this method whenever a new treeable class is implemented.
     * (But case by case checking of tblClass is still performed in other methods of this class.)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void finalizeWrite(DataModelObjBase rec, int recNum) throws UploaderException
    {
        //assign treedef and treedefitem to rec
        //DUH. Treedef actually gets set in business rules for Treeable
        DataModelObjBase parentRec = parent == null ? null : parent.getCurrentRecord(recNum);
        Treeable tRec = (Treeable)rec;
        if (parentRec != null)
        {
            Treeable pRec = (Treeable)parentRec;
            tRec.setDefinition(pRec.getDefinition());
            tRec.setDefinitionItem(pRec.getDefinition().getDefItemByRank(rank));
        }
        else
        {
            tRec.setDefinition(getTreeDef());
            tRec.setDefinitionItem(getTreeDefItem());
            
        }
    }

}
