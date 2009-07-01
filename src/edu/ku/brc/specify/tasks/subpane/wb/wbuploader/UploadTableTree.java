/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.hibernate.NonUniqueResultException;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Extends UploadTable with functions necessary for Treeable data.
 * Each rank in a tree is represented by an individual UploadTableTree.
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
    protected TreeDefIface<?, ?, ?> treeDef;
    protected boolean incrementalNodeNumberUpdates = false;
    
    

	/**
	 * @param table
	 * @param baseTable
	 * @param parent
	 * @param required
	 * @param rank
	 */
	public UploadTableTree(Uploader uploader, Table table, Table baseTable, UploadTableTree parent, boolean required, Integer rank,
                           String wbLevelName) 
	{
		super(uploader, table, null);
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
    protected TreeDefIface getTreeDef() throws UploaderException 
    {
        if (treeDef == null)
        {
            treeDef = ((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass((Class<? extends Treeable<?,?,?>> )tblClass);
        }
        if (treeDef != null)
        {
            return treeDef;
        }
        throw new UploaderException(getResourceString("WB_UPLOAD_MISSING_TREE_DEF") + " (" + tblClass.getSimpleName() + ")", UploaderException.ABORT_IMPORT);
    }    
    
    /**
     * @return TreeDefItem corresponding to tblClass and rank.
     * @throws UploaderException
     */
    protected TreeDefItemIface getTreeDefItem() throws UploaderException
    {
        return getTreeDef().getDefItemByRank(rank);
    }
            
    /**
     * @param recNum
     * 
     * @return the nearest non-null parent, or null.
     * 
     * Example: if this object, represented Genus and the Family was not provided for the current row, the Order would be used
     * as the parent. (Validation would have already detected if Family was required and missing).
     */
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#finalizeWrite(edu.ku.brc.specify.datamodel.DataModelObjBase, int)
     */
    @Override
    protected void finalizeWrite(DataModelObjBase rec, int recNum) throws UploaderException
    {
        super.finalizeWrite(rec, recNum);
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

    /*
     * Climbs the tree looking for a non-null parent.
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getParentRecord(int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable)
     */
    @Override
    protected DataModelObjBase getParentRecord(final int recNum, UploadTable forChild) throws UploaderException
    {
        DataModelObjBase result = super.getParentRecord(recNum, forChild);
        if (result != null) 
        { 
            return result; 
        }
        result = getParentRec(recNum);
        if (result == null && (forChild instanceof UploadTableTree)) 
        { 
            return (DataModelObjBase) getDefaultParent2(getTreeDefItem()); 
        }
        return result;
    }

    /**
     * @return the root of the tree for this.tblClass.
     */
    protected Treeable getTreeRoot()  throws UploaderException
    {
        if (treeRoot == null)
        {
            loadTreeRoot();
        }
        return treeRoot;
    }
    
    /**
     * @param defItem
     * 
     * @return default, 'placeholder' parent for created objects at this.rank of this.tblClass.
     * 
     * See comments for createDefaultParent.
     * 
     * @throws UploaderException
     */
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
        
    /**
     * @param parentDefItem
     * @return default, 'placeholder' parent for created objects at this.rank of this.tblClass.
     *      Calls createDefaultParent if necessary.
     *      
     * See comments for createDefaultParent.
     * 
     *
     * @throws UploaderException
     */
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
                + " and name='" + getDefaultParentName().replace("'", "''") + "'", false);
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
    
    /**
     * @param defItem
     * @return An object of this.tblClass for nearest enforced TreeDefItem to be used as a parent.
     *   
     * If this level is the highest present in the dataset. Then for each higher level that is enforced,
     * a default, placeholder parent is created.
     * 
     * For example, if a dataset contained Genus and Species, and the TreeDef specified that Class and Family were required then
     * all the genera created during an upload would be placed in 'Tree Root' -> 'Default Class' -> 'Default Parent'. (The names of the placeholders 
     * are derived from the name of the dataset and the time of the upload). If no higher levels are enforced, then the new genera would be 
     * simply be added as children of 'Tree Root'.
     *   
     * @throws UploaderException
     */
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
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        } 
        catch (InvocationTargetException ex)
        {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        } 
        catch (InstantiationException ex)
        {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
        catch (IllegalAccessException ex)
        {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
    }
    
    /**
     * Loads the root of the tree for this.tblClass.
     */
    protected void loadTreeRoot() throws UploaderException
    {
        String hql = "from " + tblClass.getName() + " where " + getTreeDefFld() + "=" + getTreeDef().getTreeDefId() + " and " + getTreeDefItemFld() + "=" +
            getTreeDef().getDefItemByRank(0).getTreeDefItemId();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            QueryIFace q = session.createQuery(hql, false);
            treeRoot = (Treeable)q.list().get(0);
        }
        finally
        {
            session.close();
        }
    }
    
    /**
     * @return the name of the id field for the TreeDef table for this.tblClass
     */
    protected String getTreeDefFld()
    {
        return tblClass.getSimpleName() + "TreeDefId";
    }

    /**
     * @return the name of the id field for the TreeDefItem table for this.tblClass
     */
    protected String getTreeDefItemFld() 
    {
        return tblClass.getSimpleName() + "TreeDefItemId";
    }

    /**
     * @return a name for a parent added to hold treeables added during an upload
     */
    protected String getDefaultParentName()
    {
        return uploader.getIdentifier();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#undoUpload()
     */
    @Override
    public void undoUpload(final boolean showProgress) throws UploaderException
    {
        super.undoUpload(showProgress);
        List<UploadedRecordInfo> keys = new LinkedList<UploadedRecordInfo>();
        for (Treeable defParent : defaultParents)
        {
            keys.add(new UploadedRecordInfo(((DataModelObjBase)defParent).getId(), -1, 0, null));
        }
        deleteObjects(keys.iterator(), showProgress);
        if (parent == null && !this.incrementalNodeNumberUpdates)
        {
            try
            {
                getTreeDef().updateAllNodeNumbers((DataModelObjBase)getTreeRoot(), false);
            }
            catch (Exception ex)
            {
                throw new UploaderException(ex);
            }
        }
    }

    /**
     * Gets ready for an upload.
     */
    @Override
    public void prepareToUpload() throws UploaderException
    {
        super.prepareToUpload();
        defaultParents.clear();
        
        if (parent == null && !this.incrementalNodeNumberUpdates)
        {
        	getTreeDef().setDoNodeNumberUpdates(false);
            getTreeDef().setUploadInProgress(true);
        }
    }
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     * 
     *  sorts in reverse order by rankId
     */
    protected class RankComparator implements Comparator<Treeable>
    {
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getRecordSetName()
     */
    @Override
    protected String getRecordSetName() 
    {
        try
        {
            return getTreeDefItem().getName() + "_" + uploader.getIdentifier();
        }
        catch (UploaderException ux)
        {
            return tblClass.getSimpleName() + "_" + uploader.getIdentifier();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#toString()
     */
    @Override
    public String toString() 
    {
        try
        {
            TreeDefItemIface td = getTreeDefItem();
            if (td != null) return td.getName();
            return tblClass.getSimpleName();
        }
        catch (UploaderException ux)
        {
            return tblClass.getSimpleName();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#verifyUploadability()
     */
    @Override
    public Vector<InvalidStructure> verifyUploadability() throws UploaderException, ClassNotFoundException
    {
        Vector<InvalidStructure> result = super.verifyUploadability();
        if (getTreeDefItem() == null)
        {
            String msg = getResourceString("WB_UPLOAD_NO_DEFITEM") + " (" + wbLevelName + ")";
            result.add(new InvalidStructure(msg, this));            
        }
        
        //check for duplicate mappings. This happens if user manages to include both Division and Phylum levels
        //in the workbench, because they have identical numeric ranks.
        if (getTreeDefItem() != null)
        {
        	for (Vector<UploadField> flds : uploadFields)
        	{
        		Vector<Field> fields = new Vector<Field>();
        		Vector<UploadField> uploadFields = new Vector<UploadField>();
        		for (UploadField fld : flds)
        		{
        			int idx = fields.indexOf(fld.getField());
        			if (idx != -1)
        			{
        				String msg = String.format(getResourceString("WB_UPLOAD_EQUIV_RANKS"), fld.getWbFldName(), 
        						uploadFields.get(idx).getWbFldName(), getTreeDefItem().getName());
        				result.add(new InvalidStructure(msg, this));
        			}
        			else
        			{
        				fields.add(fld.getField());
        				uploadFields.add(fld);
        			}
        		}
        	}
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
    
    /**
     * @return Vector of TreeDefItems that are required (enforced), but whose levels are not
     * are not included in the dataset. 
     * Only levels that are 'skipped' are included. 
     * For example:
     * If Family is required then for a dataset with mappings for Class Order Genus Species, the Family TreeDefItem would be
     * considered missing. But for a dataset with mappings for Genus and Species, Family would not be considered missing.
     */
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#finishUpload()
     */
    @Override
    public void finishUpload(boolean cancelled) throws UploaderException
    {
        super.finishUpload(cancelled);
        if (this.parent == null  && !this.incrementalNodeNumberUpdates && !cancelled)
        {
            try
            {
                getTreeDef().updateAllNodeNumbers((DataModelObjBase)getTreeRoot(), false);
            }
            catch (Exception ex)
            {
                if (ex instanceof UploaderException) 
                { 
                    throw (UploaderException) ex; 
                }
                throw new UploaderException(ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#shutdown()
     */
    @Override
    public void shutdown() throws UploaderException
    {
        super.shutdown();
        if (parent == null  && !this.incrementalNodeNumberUpdates)
        {
            getTreeDef().setDoNodeNumberUpdates(true);
            getTreeDef().setUploadInProgress(false);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#needToRefreshAfterWrite()
     */
    @Override
    public boolean needToRefreshAfterWrite()
    {
        return incrementalNodeNumberUpdates;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#findValueForReqRelClass(edu.ku.brc.specify.tasks.subpane.wb.wbuploader.RelatedClassSetter)
     */
    @Override
    protected boolean findValueForReqRelClass(RelatedClassSetter rce) throws ClassNotFoundException, UploaderException
    {
        if (rce.setter.getName().equals("setDefinition"))
        {
            //strange code, the rce will be get added to the missingReqRelClass list, but
            //it will have a default id
            rce.setDefaultId(getTreeDef().getTreeDefId());
            return false;
        }
        if (rce.setter.getName().equals("setDefinitionItem"))
        {
            //the definitonItemId gets taken care of by the rankId
            return true; 
        }
        return super.findValueForReqRelClass(rce);
    }

    /**
     * @return the rank
     */
    public Integer getRank()
    {
        return rank;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#setParents(edu.ku.brc.specify.datamodel.DataModelObjBase, int)
     */
    @Override
    protected boolean setParents(DataModelObjBase rec, int recNum)
            throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, UploaderException
    {
        super.setParents(rec, recNum);
        return true; //don't worry. It will be OK in the end.
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#addDomainCriteria(edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace, int)
	 */
	@Override
	protected void addDomainCriteria(CriteriaIFace criteria)
			throws UploaderException {
		// all already taken care of.
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#isBlankVal(edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadField, int, int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData)
     */
    @Override
    protected boolean isBlankVal(UploadField fld, int seq, int row, UploadData uploadData)
    {
        boolean result = super.isBlankVal(fld, seq, row, uploadData);
        if (!result || uploadFields.size() == 1 || (parent != null && parent.uploadFields.size() > 1))
        {
            return false;
        }
        
        UploadTableTree kid = child;
        while (kid != null)
        {
            UploadField kidField = null;
            for (UploadField field : kid.uploadFields.get(seq))
            {
                if (field.getField().getName().equals(fld.getField().getName()))
                {
                    kidField = field; 
                    break;
                }
            }
            if (kidField != null)
            {
                kidField.setValue(uploadData.get(row, kidField.getIndex()));
                if (!super.isBlankVal(kidField, seq, row, uploadData))
                {
                    return false;
                }
            }
            else
            {
                //this should never happen
                log.error("Possibly invalid tree structure for " + tblClass.getSimpleName() + " (" + fld + ")");
            }
            kid = kid.child;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#addInvalidValueMsgForOneToManySkip(java.util.Vector, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadField, java.lang.String, int, int)
     */
    @Override
    protected void addInvalidValueMsgForOneToManySkip(Vector<UploadTableInvalidValue> msgs,
                                                      UploadField fld,
                                                      String name,
                                                      int row,
                                                      int seq)
    {
        super.addInvalidValueMsgForOneToManySkip(msgs, fld, name, row, seq);
        UploadTableTree kid = child;
        while (kid != null)
        {
            UploadField kidField = null;
            for (UploadField field : kid.uploadFields.get(seq))
            {
                if (field.getField().getName().equals(fld.getField().getName()))
                {
                    kidField = field; 
                    break;
                }
            }
            if (kidField != null)
            {
                super.addInvalidValueMsgForOneToManySkip(msgs, kidField, kid.toString(), row, seq);
            }
            else
            {
                //this should never happen
                log.error("Possibly invalid tree structure for " + tblClass.getSimpleName() + " (" + fld + ")");
            }
            kid = kid.child;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#validateRowValues(int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData, java.util.Vector)
     */
    @Override
    protected void validateRowValues(int row,
                                     UploadData uploadData,
                                     Vector<UploadTableInvalidValue> invalidValues)
    {
        super.validateRowValues(row, uploadData, invalidValues);
        //check that the "name" (currently the 'main' field for all specify trees) is not blank or that all other fields are.
        for (Vector<UploadField> flds : uploadFields)
        {
            boolean nameBlank = true;
            boolean allBlank = true;
            UploadField mainFld = null;
            for (UploadField fld : flds)
            {
                if (fld.getField().getName().equalsIgnoreCase("name"))
                {
                    mainFld = fld;
                }
                if (fld.getIndex() != -1)
                {
                    if (!StringUtils.isEmpty(uploadData.get(row, fld.getIndex())))
                    {
                        allBlank = false;
                        if (fld == mainFld)
                        {
                            nameBlank = false;
                        }
                    }
                }
            }
            if (nameBlank && !allBlank)
            {
                invalidValues.add(new UploadTableInvalidValue(null,
                        this, mainFld, row, new Exception(getResourceString("WB_UPLOAD_INVALID_EMPTY_CELL"))));
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#requiredLocalFldsArePresent()
     */
    @Override
    protected boolean requiredLocalFldsArePresent()
    {
        //apparently this is not necessary
        if (!super.requiredLocalFldsArePresent())
        {
            return false;
        }
        //this works because the 'main' content field for all Specify treeables is named "name".
        for (Vector<UploadField> flds : uploadFields)
        {
           boolean gotName = false;
           for (UploadField fld : flds)
            {
                if (fld.getField().getName().equalsIgnoreCase("name"))
                {
                    gotName = true;
                    break;
                }
            }
            if (!gotName)
            {
                return false;
            }
        }
        return true;
        //return super.requiredLocalFldsArePresent();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getMissingReqLocalFlds()
     */
    @Override
    protected Vector<Field> getMissingReqLocalFlds()
    {
        Vector<Field> result = super.getMissingReqLocalFlds();
        result.add(table.getField("name"));
        return result;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getMatchCriteria(edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace, int, java.util.Vector)
     */
    @Override
    protected boolean getMatchCriteria(CriteriaIFace critter,
                                       int recNum,
                                       Vector<UploadTable.MatchRestriction> restrictedVals)
            throws UploaderException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException
    {
        boolean result =  super.getMatchCriteria(critter, recNum, restrictedVals);
        //XXX It is possible for taxa (or other tree tables) to have null (interpreted as true) isAccepted
        //if they were entered outside of Specify or the Specify wizard. In that case this restriction
        //will fail and new tree nodes may be created unnecessarily.
        restrictedVals.add(new UploadTable.MatchRestriction("isAccepted", addRestriction(
                critter, "isAccepted", new Boolean(true), false), -1));
        return result;
    }

	/**
	 * @return the parent
	 */
	public UploadTableTree getParent()
	{
		return parent;
	}

	/**
	 * @return the child
	 */
	public UploadTableTree getChild()
	{
		return child;
	}

    
}
