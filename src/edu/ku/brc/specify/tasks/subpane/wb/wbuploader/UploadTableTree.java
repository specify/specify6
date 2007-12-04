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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.NonUniqueResultException;

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
@SuppressWarnings("unchecked")
public class UploadTableTree extends UploadTable
{
	protected final Table baseTable; 
	protected final UploadTableTree parent;
    protected UploadTableTree child;
    protected final Integer rank;
    protected final String wbLevelName;
    protected Treeable treeRoot;    
    protected SortedSet<Treeable> defaultParents;
    private static GeographyTreeDef geoTreeDef = null;
    private static TaxonTreeDef taxTreeDef = null;
    
    

	/**
	 * @param table
	 * @param baseTable
	 * @param parent
	 * @param required
	 * @param rank
	 */
	public UploadTableTree(Table table, Table baseTable, UploadTableTree parent, boolean required, Integer rank,
                           String wbLevelName) 
	{
		super(table, null);
        this.baseTable = baseTable;
		this.parent = parent;
        if (this.parent != null)
        {
            this.parent.child = this;
        }
		this.required = required;
        this.rank = rank;
        this.wbLevelName = wbLevelName;
        defaultParents = new TreeSet<Treeable>(new RankComparator());
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
    
    protected DataModelObjBase getParentRec(int recNum)
    {
        if (parent == null)
        {
            return null;
        }
        DataModelObjBase result = parent.getCurrentRecord(recNum);
        UploadTableTree grandParent = parent.parent;
        while (result == null && grandParent != null)
        {
            result = grandParent.getCurrentRecord(recNum);
            grandParent = grandParent.parent;
        }
        return result;
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
        DataModelObjBase parentRec = getParentRec(recNum);
        Treeable tRec = (Treeable)rec;
        tRec.setDefinition(getTreeDef());
        tRec.setDefinitionItem(getTreeDefItem());
        if (parentRec == null)
        {
            tRec.setParent(getDefaultParent2(getTreeDefItem()));
        }
        else
        {
            //this probably will already have been done in UploadTable.setParents, unless immediate parent id is null.
            tRec.setParent((Treeable)parentRec);
        }
    }

    protected Treeable getTreeRoot() throws UploaderException
    {
        if (treeRoot == null)
        {
            loadTreeRoot();
        }
        return treeRoot;
    }
    
    @SuppressWarnings("unchecked")
    protected Treeable getDefaultParent2(TreeDefItemIface defItem) throws UploaderException
    {
        TreeDefItemIface parentDefItem = defItem.getParent();
        while (parentDefItem != null && parentDefItem.getRankId() > 0)
        {
            if (parentDefItem.getIsEnforced() != null && parentDefItem.getIsEnforced())
            {
                break;
            }
            parentDefItem = parentDefItem.getParent();
        }
        if (parentDefItem == null)
        {
            throw new UploaderException("unable to find default parent for " + defItem.getName(), UploaderException.ABORT_ROW);
        }
        if (parentDefItem.getRankId() == 0)
        {
            return getTreeRoot();
        }
        return getDefaultParent(parentDefItem);
     }
        
    protected Treeable getDefaultParent(TreeDefItemIface parentDefItem) throws UploaderException
    {
        for (Treeable p : defaultParents)
        {
            if (p.getDefinitionItem().getTreeDefItemId().equals(parentDefItem.getTreeDefItemId()))
            {
                return p;
            }
        }
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        QueryIFace q = session.createQuery("from " + tblClass.getName() + " where rankId=" + parentDefItem.getRankId().toString()
                + " and name='" + getDefaultParentName() + "'");
        try
        {
            Treeable result = (Treeable)q.uniqueResult();
            if (result != null)
            {
                return result;
            }
        }
        catch (NonUniqueResultException hex)
        {
            throw new RuntimeException(hex);
        }
        finally
        {
            session.close();
        }
        
        return createDefaultParent(parentDefItem);
    }
    
    protected Treeable createDefaultParent(TreeDefItemIface defItem) throws UploaderException
    {
        try
        {
            Constructor<?> constructor = tblClass.getDeclaredConstructor();
            Treeable result = (Treeable)constructor.newInstance((Object[])null);
            result.initialize();
            result.setRankId(defItem.getRankId());
            result.setParent(getDefaultParent2(defItem)); //ouch
            result.setName(getDefaultParentName());
            result.setDefinition(getTreeDef());
            result.setDefinitionItem(defItem);
            doWrite((DataModelObjBase)result/*, 0*/);
            defaultParents.add(result);
            return result;            
        } 
        catch (NoSuchMethodException ex)
        {
            throw new RuntimeException(ex);
        } 
        catch (InvocationTargetException ex)
        {
            throw new RuntimeException(ex);
        } 
        catch (InstantiationException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    protected void loadTreeRoot() throws UploaderException
    {
        String hql = "from " + tblClass.getName() + " where " + getTreeDefFld() + "=" + getTreeDef().getTreeDefId() + " and " + getTreeDefItemFld() + "=" +
            getTreeDef().getDefItemByRank(0).getTreeDefItemId();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            QueryIFace q = session.createQuery(hql);
            treeRoot = (Treeable)q.list().get(0);
        }
        finally
        {
            session.close();
        }
    }
    
    protected String getTreeDefFld() throws UploaderException
    {
        if (tblClass == Taxon.class)
        {
            return "TaxonTreeDefId";
        }
        if (tblClass == Geography.class)
        {
            return "GeographyTreeDefId";
        }
        throw new UploaderException("unable to find TreeDef field for " + baseTable.getName(), UploaderException.ABORT_IMPORT);
    }

    protected String getTreeDefItemFld() throws UploaderException
    {
        if (tblClass == Taxon.class)
        {
            return "TaxonTreeDefItemId";
        }
        if (tblClass == Geography.class)
        {
            return "GeographyTreeDefItemId";
        }
        throw new UploaderException("unable to find TreeDefItem field for " + baseTable.getName(), UploaderException.ABORT_IMPORT);
    }

    protected String getTreeDefItemTbl() throws UploaderException
    {
        if (tblClass == Taxon.class)
        {
            return "TaxonTreeDefItem";
        }
        if (tblClass == Geography.class)
        {
            return "GeographyTreeDefItem";
        }
        throw new UploaderException("unable to find TreeDefItem table for " + baseTable.getName(), UploaderException.ABORT_IMPORT);
    }

    /**
     * @return a name for a parent added to hold taxa added during an upload
     */
    protected String getDefaultParentName()
    {
        return Uploader.currentUpload.getIdentifier();
    }
    
    /**
     * undoes the most recent upload.
     * 
     */
    @Override
    public void undoUpload()
    {
        super.undoUpload();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        String hql = "from " + getWriteTable().getName() + " where id =:theKey";
        QueryIFace q = session.createQuery(hql);
        try
        {
            for (Treeable defParent : defaultParents)
            {
                Object key = ((DataModelObjBase)defParent).getId();
                if (key != null)
                {
                    try
                    {
                        q.setParameter("theKey", key);
                        DataModelObjBase obj = (DataModelObjBase) q.uniqueResult();
                        session.beginTransaction();
                        session.delete(obj);
                        session.commit();
                    }
                    catch (Exception ex)
                    {
                        // the delete may fail if another user has used or deleted uploaded
                        // records...
                        // or if another UploadTreeTable has deleted the parent.
                        log.info(ex);
                    }
                }
            }
        }
        finally
        {
            session.close();
        }
    }

    /**
     * Gets ready for an upload.
     */
    @Override
    public void prepareToUpload() throws NoSuchMethodException, ClassNotFoundException
    {
        super.prepareToUpload();
        defaultParents.clear();
    }
    
    protected class RankComparator implements Comparator<Treeable>
    {
        //sorts in reverse order by rankId
        public int compare(Treeable t1, Treeable t2)
        {
            if (t1.getRankId() < t2.getRankId())
            {
                return 1;
            }
            if (t1.getRankId() == t2.getRankId())
            {
                return 0;
            }
            return -1;
        }
    }
    /**
     * @return a name for recordset of uploaded objects.
     */
    @Override
    protected String getRecordSetName()
    {
        try
        {
            return getTreeDefItem().getName() + "_" + Uploader.currentUpload.getIdentifier();
        }
        catch (UploaderException e)
        {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String toString()
    {
        try
        {
            return getTreeDefItem().getName();
        }
        catch (UploaderException ex)
        {
            return super.toString();
        }
    }

    @Override
    public Vector<InvalidStructure> validateStructure() throws UploaderException
    {
        Vector<InvalidStructure> result = super.validateStructure();
        if (getTreeDefItem() == null)
        {
            String msg = getResourceString("WB_UPLOAD_NO_DEFITEM") + " (" + wbLevelName + ")";
            result.add(new InvalidStructure(msg, this));            
        }
        if (parent == null)
        {
            Vector<TreeDefItemIface<?,?,?>> missingDefs = getMissingRequiredDefs();
            for (TreeDefItemIface<?,?,?> defItem : missingDefs)
            {
                String msg = getResourceString("WB_UPLOAD_MISSING_TREE_LEVEL") + " (" + defItem.getName() +")";
                result.add(new InvalidStructure(msg, this)); 
            }
        }
        return result;
    }
    
    protected Vector<TreeDefItemIface<?,?,?>> getMissingRequiredDefs() throws UploaderException
    {
        Vector<TreeDefItemIface<?,?,?>> result = new Vector<TreeDefItemIface<?,?,?>>();
        for (Object obj : getTreeDef().getTreeDefItems())
        {
            TreeDefItemIface<?,?,?> defItem = (TreeDefItemIface<?,?,?>)obj;
            if (defItem.getRankId() > rank && defItem.getIsEnforced() != null && defItem.getIsEnforced())
            {
                UploadTableTree currLevel = this;
                while (currLevel != null)
                {
                    if (!defItem.getRankId().equals(currLevel.rank))
                    {
                        currLevel = currLevel.child;
                    }
                    else
                    {
                        break;
                    }
                }
                if (currLevel == null)
                {
                    result.add(defItem);            
                }
            }
        }
        return result;
    }
    
    /**
     * @param fldName
     * @return true if a field named fldname is in the uploading dataset, or if it is set programmitically..
     */
    @Override
    protected boolean fldInDataset(final String fldName)
    {
        return super.fldInDataset(fldName) || fldName.equalsIgnoreCase("fullname");
    }

    /**
     * @param fld
     * @returns true if fld is empty and that is not OK.
     */
    @Override
    protected boolean invalidNull(final UploadField fld, final UploadData uploadData, int row, int seq) throws UploaderException
    {
        if (fld.isRequired() && getTreeDefItem().getIsEnforced() != null && getTreeDefItem().getIsEnforced())
        {
            if (fld.getValue() == null || fld.getValue().trim().equals(""))
            {
                //if no children in the treeable hierarchy are non-null then ignore the nullness at this level.
                UploadTableTree currentChild = child;
                while (currentChild != null)
                {
                    UploadField uf = currentChild.findUploadField(fld.getField().getName(), seq);
                    if (uf != null)
                    {
                        String val = uploadData.get(row, uf.getIndex());
                        if (val != null && !val.trim().equals(""))
                        {
                            return true;
                        }
                    }
                    currentChild = currentChild.child;
                }
            }
        }
        return false;
    }

    /**
     * @param recNum
     * @return true if there is some data in the current row dataset that needs to be written to this table in the database.
     */
    @Override
    protected boolean needToWrite(int recNum)
    {
        return dataToWrite(recNum);
    }

}
