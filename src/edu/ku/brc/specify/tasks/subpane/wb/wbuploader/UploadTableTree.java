/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import java.util.*;

import edu.ku.brc.af.core.db.DBInfoBase;
import org.apache.commons.lang.StringUtils;
import org.hibernate.NonUniqueResultException;

import edu.ku.brc.af.core.AppContextMgr;
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
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader.ParentTableEntry;
import edu.ku.brc.util.Pair;

import javax.xml.crypto.Data;

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
    protected Boolean isLowerSubTree; //true for levels derived from tax fields within determination table
    protected Treeable<?,?,?> treeRoot;    
    protected SortedSet<Treeable<?,?,?>> defaultParents;
    protected TreeDefIface<?, ?, ?> treeDef;
    protected boolean incrementalNodeNumberUpdates = false;
    protected boolean allowUnacceptedMatches = true;
    protected List<UploadField> nameFields = null;
    protected Integer actualExportedRecordId = null;
    protected DataModelObjBase actualExportedRecord = null;
    protected boolean actualExportedRecordIdHasBeenSet = false;
    protected int depth = 0; //used for updates when dealing with uploadtable-less tree nodes
    protected List<DataModelObjBase> depthRecords = new ArrayList<>();

    //protected Integer originalExportedRecordId = null;
    //protected DataModelObjBase originalExportedRecord = null;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
//json stuff for sp7 uploader experimentation...
    
@Override
protected List<java.lang.reflect.Field> getFldsForJSON() {
	List<java.lang.reflect.Field> result = super.getFldsForJSON();
	java.lang.reflect.Field[] flds = UploadTableTree.class.getDeclaredFields();
//	Arrays.sort(flds, new Comparator<java.lang.reflect.Field>(){
//		public int compare(java.lang.reflect.Field f1, java.lang.reflect.Field f2) {
//			return f1.getName().compareTo(f2.getName());
//		}
//	});
	String[] skippers = {"incrementalNodeNumberUpdates","treeRoot"};
	for (java.lang.reflect.Field fld : flds) {
		if (0 > Arrays.binarySearch(skippers, fld.getName())) {
			result.add(fld);
		}
	}
	return result;
}


//...json stuff for sp7 uploader experimentation    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @param table
	 * @param baseTable
	 * @param parent
	 * @param required
	 * @param rank
	 */
	public UploadTableTree(Uploader uploader, Table table, Table baseTable, UploadTableTree parent, boolean required, Integer rank,
                           String wbLevelName, Boolean isLowerSubTree) 
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
        defaultParents = new TreeSet<Treeable<?,?,?>>(new RankComparator());
        this.isLowerSubTree = isLowerSubTree;
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
    protected TreeDefIface<?,?,?> getTreeDef() throws UploaderException 
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
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#shouldEnforceNonNullConstraint(int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData, int)
     */
    @Override
	protected boolean shouldEnforceNonNullConstraint(int row,
			UploadData uploadData, int seq) {
		boolean result = super.shouldEnforceNonNullConstraint(row, uploadData, seq);
		if (!result) 
		{
			result = parentsAreBlank(row, uploadData) && !childrenAreBlank(row, uploadData);
		}
		return result;
	}

    /**
     * @param row
     * @param uploadData
     * @return
     */
    protected boolean childrenAreBlank(int row, UploadData uploadData)
    {
    	boolean result = false;
    	if (!child.isAllBlank(row, uploadData))
    	{
    		result = false;
    	} else
    	{
    		result = child.childrenAreBlank(row, uploadData);
    	}
    	return result;
    }
    
    /**
     * @param row
     * @param uploadData
     * @return
     */
    protected boolean parentsAreBlank(int row, UploadData uploadData)
    {
    	boolean result = getParent() == null;
    	if (!result)
    	{
    		if (!getParent().isAllBlank(row, uploadData))
    		{
    			result = false;
    		} else
    		{
    			result = getParent().parentsAreBlank(row, uploadData);
    		}
    	}
    	return result;
    }
    
    /**
     * @param row
     * @param uploadData
     * @return
     */
    protected boolean isAllBlank(int row, UploadData uploadData)
    {
    	for (Pair<Boolean, Boolean> b : blankness(row, uploadData))
    	{
    		if (!b.getFirst()) 
    		{
    			return false;
    		}
    	}
    	return true;
    }
	/**
     * @return TreeDefItem corresponding to tblClass and rank.
     * @throws UploaderException
     */
    public TreeDefItemIface<?,?,?> getTreeDefItem() throws UploaderException
    {
        return getTreeDef().getDefItemByRank(rank);
    }
            
    
    /**
     * @return the level name from the wb template
     */
    public String getWbLevelName() 
    {
		return wbLevelName;
	}

	/**
     * @param parent
     * @param currentRec
     * @param recNum
     * @return parent or its 'AcceptedParent'.
     */
    protected DataModelObjBase getAcceptedParent(DataModelObjBase parent, Treeable<?,?,?> currentRec, int recNum, boolean notify)
    	throws UploaderException
    {
        if (parent == null || ((Treeable<?,?,?> )parent).getIsAccepted() || currentRec == null)
        {
        	return parent;
        } else if (!((Treeable<?,?,?> )parent).getIsAccepted()) {
        	Treeable<?,?,?> childRec = currentRec == null ?
        			(Treeable<?,?,?>) getCurrentRecord(recNum) :
        			currentRec;	
        	String childName = childRec == null ? null : childRec.getName();	
        	TreeDefItemIface<?,?,?> childDefItem = childRec == null ? null : getTreeDef().getDefItemByRank(childRec.getRankId());
        	String childRank = childDefItem == null ? null : childDefItem.getDisplayText();
			String parentName = ((Treeable<?,?,?>) parent).getFullName();
			String parentRank = getTreeDef().getDefItemByRank(((Treeable<?,?,?>) parent).getRankId()).getDisplayText();
			String msg = childRank + " '" + childName + "' cannot be a child of unaccepted " + parentRank + " '" + parentName + "'";
			throw new UploaderException(msg, UploaderException.ABORT_ROW);
        } else {
        	return null;
        }
//        DataModelObjBase newResult = (DataModelObjBase )((Treeable<?,?,?> )parent).getAcceptedParent();
//        if (notify)
//		{
//			String name = currentRec == null ? null : currentRec.getName();
//			if (name == null)
//			{
//				Treeable<?,?,?> tRec = (Treeable<?,?,?>) getCurrentRecord(recNum);
//				name = tRec == null ? getResourceString("UploadTableTree.CurrentNode")
//						: tRec.getName();
//			}
//			String parentName = ((Treeable<?,?,?>) parent).getName();
//			String newParentName = ((Treeable<?,?,?>) newResult).getName();
//			String msg = String
//					.format(
//							getResourceString("UploadTableTree.UnacceptedParentSwitch"),
//							wbCurrentRow+1, name, parentName, newParentName);
//			uploader.addMsg(new AcceptedParentSwitchMessage(msg, wbCurrentRow+1));
//		}
//        return (DataModelObjBase )((Treeable<?,?,?> )parent).getAcceptedParent();
    }
    
    /**
     * @param recNum
     * 
     * @return the nearest non-null parent, or null.
     * 
     * Example: if this object, represented Genus and the Family was not provided for the current row, the Order would be used
     * as the parent. (Validation would have already detected if Family was required and missing).
     */
    protected DataModelObjBase getParentRec(Treeable<?,?,?> currentRec, int recNum) throws UploaderException {
        return depth > 0 ? depthRecords.get(depth - 1) : getParentRec(currentRec, recNum, false);
    }
    
    /**
     * @param recNum
     * 
     * @return the nearest non-null parent, or null.
     * 
     * Example: if this object, represented Genus and the Family was not provided for the current row, the Order would be used
     * as the parent. (Validation would have already detected if Family was required and missing).
     */
    protected DataModelObjBase getParentRec(Treeable<?,?,?> currentRec, int recNum, boolean checkSubTree) throws UploaderException {
        if (updateMatches && parent == null) {
            if (currentRec != null) {
                return (DataModelObjBase)currentRec.getParent();
            } else {
                //get the parent of the node at this level in the actualExportedRecord's parentage
                Treeable leaf = (Treeable)findActualExportedRecord();
                while (leaf != null && leaf.getRankId() > this.getRank()) {
                    leaf = leaf.getParent();
                }
                if (leaf != null) {
                    return (DataModelObjBase)leaf.getParent();
                }
            }
        }
        if (parent == null || (checkSubTree && parent.isLowerSubTree != this.isLowerSubTree)) {
            return null;
        }
        DataModelObjBase result = parent.getCurrentRecord(recNum);
        UploadTableTree grandParent = parent.parent;
        while (result == null && grandParent != null && (!checkSubTree ||grandParent.isLowerSubTree == this.isLowerSubTree))
        {
            result = grandParent.getCurrentRecord(recNum);
            grandParent = grandParent.parent;
        }
        DataModelObjBase finalResult = getAcceptedParent(result, currentRec, recNum, true);
        //Since the uploader throws exceptions when a parent is unaccepted, the following 
        //condition should never be true; getAccepted will have thrown an exception if result was unaccepted
        if (finalResult != null && !Treeable.class.cast(result).getIsAccepted()) {
        	int finalRankId = Treeable.class.cast(finalResult).getRankId();
        	int currRankId = this.rank;
    		TreeDefItemIface<?,?,?> resDef = getTreeDef().getDefItemByRank(currRankId);
    		TreeDefItemIface<?,?,?> currDef = resDef;
    		TreeDefItemIface<?,?,?> parentDef = getTreeDef().getDefItemByRank(finalRankId);
        	boolean badParentRank = false;
        	if (finalRankId >= currRankId) {
        		badParentRank = true;
        	} else {
        		currDef = currDef.getParent();
        		while (currDef.getRankId() > parentDef.getRankId()) {
        			if (currDef.getIsEnforced()) {
        				badParentRank = true;
        				break;
        			} else {
        				currDef = currDef.getParent();
        			}
        		}
        	}        		
        	if (badParentRank) {
    			String msg = parentDef.getName() + " " + Treeable.class.cast(finalResult).getFullName() +
    					" is not a valid parent for " + resDef.getName() + " " + currentRec.getName();
            	throw new UploaderException(msg, UploaderException.ABORT_ROW);
    		}
        }
        return finalResult;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#finalizeWrite(edu.ku.brc.specify.datamodel.DataModelObjBase, int)
     */
    @Override
    protected boolean finalizeWrite(DataModelObjBase rec, int recNum) throws UploaderException
    {
        super.finalizeWrite(rec, recNum);
        //assign treedef and treedefitem to rec
        Treeable tRec = (Treeable)rec;
        DataModelObjBase parentRec = getParentRec(tRec, recNum);
        if (depth == 0) {
            tRec.setDefinition(getTreeDef());
            tRec.setDefinitionItem(getTreeDefItem());
        }
        if (parentRec == null) {
            tRec.setParent(getDefaultParent2(getTreeDefItem()));
            return true;
        }
        else {
            //this probably will already have been done in UploadTable.setParents, unless immediate parent id is null.
            if (tRec.getParent() != parentRec) {
                tRec.setParent((Treeable<?, ?, ?>) parentRec);
                return true;
            }
        }
        return false;
    }

    /*
     * Climbs the tree looking for a non-null parent.
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getParentRecord(int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable)
     */
    @Override
    protected DataModelObjBase getParentRecord(final int recNum, UploadTable forChild) throws UploaderException {
        DataModelObjBase result = super.getParentRecord(recNum, forChild);
        if (result != null) {
        	return result;
        }
        result = getParentRec(null, recNum, !(forChild instanceof UploadTableTree));
        if (result == null && (forChild instanceof UploadTableTree)) {
            return (DataModelObjBase) getDefaultParent2(getTreeDefItem()); 
        }
        return result;
    }

    /**
     * @return the root of the tree for this.tblClass.
     */
    protected Treeable<?,?,?> getTreeRoot()  throws UploaderException {
        if (treeRoot == null) {
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
    protected Treeable<?,?,?> getDefaultParent2(TreeDefItemIface<?,?,?> defItem) throws UploaderException {
        TreeDefItemIface<?,?,?> parentDefItem = defItem.getParent();
        while (parentDefItem != null && parentDefItem.getRankId() > 0) {
            if (parentDefItem.getIsEnforced() != null && parentDefItem.getIsEnforced()) {
                break;
            }
            parentDefItem = parentDefItem.getParent();
        }
        if (parentDefItem == null) {
            throw new UploaderException("unable to find default parent for " + defItem.getName(), UploaderException.ABORT_ROW);
        }
        if (parentDefItem.getRankId() == 0) {
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
    protected Treeable<?,?,?> getDefaultParent(TreeDefItemIface<?,?,?> parentDefItem) throws UploaderException {
        for (Treeable<?,?,?> p : defaultParents) {
            if (p.getDefinitionItem().getTreeDefItemId().equals(parentDefItem.getTreeDefItemId())) {
                return p;
            }
        }
        
    	Pair<DataProviderSessionIFace,Boolean> sessObj = getSession();
    	DataProviderSessionIFace session = sessObj.getFirst();
        QueryIFace q = session.createQuery("from " + tblClass.getName() + " where rankId=" + parentDefItem.getRankId().toString()
                + " and name='" + getDefaultParentName().replace("'", "''") + "'", false);
        try {
            Treeable<?,?,?> result = (Treeable<?,?,?>)q.uniqueResult();
            if (result != null) {
                return result;
            }
        } catch (NonUniqueResultException hex) {
            throw new RuntimeException(hex);
        } finally {
            getRidOfSession(sessObj);
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
    protected Treeable<?,?,?> createDefaultParent(TreeDefItemIface<?,?,?> defItem) throws UploaderException {
        try {
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
        } catch (NoSuchMethodException ex) {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        } catch (InvocationTargetException ex) {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        } catch (InstantiationException ex) {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        } catch (IllegalAccessException ex) {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
    }

    @Override
    protected DataModelObjBase getExportedRecIdForParent(final ParentTableEntry pte, final DataModelObjBase rec, boolean idWasSet) throws Exception {
        if (idWasSet) {
            return super.getExportedRecIdForParent(pte, rec, idWasSet);
        } else {
            return rec;
        }
    }

    /**
     * Loads the root of the tree for this.tblClass.
     */
    protected void loadTreeRoot() throws UploaderException {
        String hql = "from " + tblClass.getName() + " where " + getTreeDefFld() + "=" + getTreeDef().getTreeDefId() + " and " + getTreeDefItemFld() + "=" +
            getTreeDef().getDefItemByRank(0).getTreeDefItemId();
        Pair<DataProviderSessionIFace,Boolean> sessObj = getSession();
        DataProviderSessionIFace session = sessObj.getFirst();
        try {
            QueryIFace q = session.createQuery(hql, false);
            treeRoot = (Treeable<?,?,?>)q.list().get(0);
        } finally {
            getRidOfSession(sessObj);
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
        for (Treeable<?,?,?> defParent : defaultParents)
        {
            keys.add(new UploadedRecordInfo(((DataModelObjBase)defParent).getId(), -1, 0, null));
        }
        deleteObjects(keys.iterator(), showProgress);
    }

    /**
     * Gets ready for an upload.
     */
    @Override
    public void prepareToUpload(boolean inTransaction) throws UploaderException {
        super.prepareToUpload(inTransaction);
        defaultParents.clear();
        
        if (parent == null && !this.incrementalNodeNumberUpdates && !inTransaction) {
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
    protected class RankComparator implements Comparator<Treeable<?,?,?>>
    {
        public int compare(Treeable<?,?,?> t1, Treeable<?,?,?> t2)
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
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getFullRecordSetName()
     */
    @Override
    protected String getFullRecordSetName(boolean showRecordSetInUI, int maxNameLength)
    {
        if (showRecordSetInUI)
        {
        	return super.getFullRecordSetName(showRecordSetInUI, maxNameLength);
        }
        
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
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getShortRecordSetName()
     */
    @Override
    protected String getShortRecordSetName()
    {
        try
        {
            return getTreeDefItem().getName();
        }
        catch (UploaderException ux)
        {
            return tblClass.getSimpleName();
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
            TreeDefItemIface<?,?,?> td = getTreeDefItem();
            if (td != null) return td.getDisplayText();
            return tblClass.getSimpleName();
        }
        catch (UploaderException ux)
        {
            return tblClass.getSimpleName();
        }
    }

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getTblTitle()
	 */
	@Override
	public String getTblTitle()
	{
		return toString();
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
        			if (fld.getIndex() != -1)
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
        if (child != null)
        {
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
        					if (currLevel.child == null && defItem.getRankId() > currLevel.rank)
        					{
        						//don't complain about missing required levels outside the range of ranks of tables being uploaded
        						break;
        					}
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
        }
        return result;
    }
    
    /**
     * @param fldName
     * @return true if a field named fldname is in the uploading dataset, or if it is set programmitically.
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
    protected boolean invalidNull(final UploadField fld, final UploadData uploadData, int row, int seq) throws UploaderException {
        if (fld.isRequired() && getTreeDefItem().getIsEnforced() != null && getTreeDefItem().getIsEnforced()) {
            if (fld.getValue() == null || fld.getValue().trim().equals("")) {
                //if no children in the treeable hierarchy are non-null then ignore the nullness at this level.
                return aChildHasData(seq, row, uploadData);
            }
        }
        return false;
    }

    /**
     *
     * @param seq
     * @param row
     * @param uploadData
     * @return
     */
    private boolean aChildHasData(int seq, int row, final UploadData uploadData) {
        UploadTableTree currentChild = child;
        while (currentChild != null) {
            UploadField uf = currentChild.findUploadField("name", seq);
            if (uf != null) {
                String val = uploadData.get(row, uf.getIndex());
                if (val != null && !val.trim().equals("")) {
                    return true;
                }
            }
            currentChild = currentChild.child;
        }
        return false;
    }

    @Override
	protected boolean ignoreFieldData(UploadField f) {
		boolean result = super.ignoreFieldData(f);
		if (!result) {
			result = f.getField().getName().equalsIgnoreCase("rankid");
		}
		return result;
	}

	/**
     * @param recNum
     * @return true if there is some data in the current row dataset that needs to be written to this table in the database.
     */
    @Override
    protected boolean needToWrite(int recNum) {
        return dataToWrite(recNum);
    }

    /**
     *
     * @param recNum
     * @return
     * @throws UploaderException
     */
    @Override
    protected boolean needToCreateRecordIfParentChanged(int recNum) throws UploaderException {
        if (getParent() != null && getParent().getCurrentRecord(recNum) != null
                && getTreeDefItem() != null && getTreeDefItem().getIsEnforced()
                && getChild() != null) {
            return aChildHasData(recNum, wbCurrentRow, uploader.getUploadData());
        } else {
            return false;
        }
    }

    /**
     * @return true if changes that require a tree update have occurred.
     * 
     * This method should be called by the highest level in the tree.
     */
    protected boolean needToUpdateTree(boolean isCancelled)
    {
    	//XXX may need to check other things when 'update' uploads are implemented
        if (isCancelled && uploader.theUploadBatchEditSession != null) {
    	    return false;
        }

        if (uploadedRecs.getFirst().size() > 0 || uploadedRecs.getSecond().size() > 0) {
    		return true;
    	}
    	
    	if (child != null) {
    		return child.needToUpdateTree(isCancelled);
    	}
    	
    	return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#finishUpload()
     */
    @Override
    public void finishUpload(boolean cancelled, DataProviderSessionIFace theSession) throws UploaderException {
        super.finishUpload(cancelled, theSession);
        if (this.parent == null  && !this.incrementalNodeNumberUpdates && !cancelled) {
        	if (needToUpdateTree(cancelled)) {
        		try {
        			if (theSession == null) {
        				getTreeDef().updateAllNodes((DataModelObjBase)getTreeRoot(), true, false);
        			} else {
        				getTreeDef().updateAllNodes((DataModelObjBase)getTreeRoot(), shouldShowProgressForNodeUpdate(), true, true, false, theSession);
        			}
        		} catch (Exception ex) {
        			if (ex instanceof UploaderException) 
        			{ 
        				throw (UploaderException) ex; 
        			}
        			throw new UploaderException(ex);
        		}
        	}
        }
    }

    /**
     *
     * @return
     */
    private boolean shouldShowProgressForNodeUpdate() {
        //this may need to be changed if seven uses sp6 for update uploads
        //return updateMatches;
        return false;
    }
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#finishUndoUpload()
	 */
	@Override
	public void finishUndoUpload() throws UploaderException
	{
		super.finishUndoUpload();
		finishUpload(false, null);
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#shutdown()
     */
    @Override
    public void shutdown() throws UploaderException
    {
        super.shutdown();
        if (parent == null  && !this.incrementalNodeNumberUpdates && tblSession == null)
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
        //return incrementalNodeNumberUpdates;
        /* calling refresh seems to ruin uploads/batch-updates when a single transaction is used for the entire process
        * I don't think the NodeNumbers are relevant to uploading, and I don't really think refresh is necessary anyway
        * because the current rec will contain the changes made by the business rules.
         */
        return false;
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

    @Override
    protected boolean addToReqRelClasses(Class<?> relatedClass) {
        return super.addToReqRelClasses(relatedClass) && !relatedClass.isInterface();
    }

    /**
     * @return the rank
     */
    public Integer getRank()
    {
        return rank;
    }

    /**
     *
     * @param rec
     * @param flds
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws UploaderException
     */
    @Override
    protected boolean setFields(DataModelObjBase rec, Vector<UploadField> flds) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, UploaderException {
        if (depth > 0) {
            return false;
        } else {
            return super.setFields(rec, flds);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#setParents(edu.ku.brc.specify.datamodel.DataModelObjBase, int)
     */
    @Override
    protected boolean setParents(DataModelObjBase rec, int recNum, boolean isForWrite)
            throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, UploaderException
    {
        boolean result = super.setParents(rec, recNum, isForWrite);
        //return true; //don't worry. It will be OK in the end.
        return result;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#addDomainCriteria(edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace, int)
	 */
	@Override
	protected void addDomainCriteria(CriteriaIFace criteria)
			throws UploaderException 
	{
		// all already taken care of.
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#shouldLoadParentTbl(edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable)
	 */
	@Override
	protected boolean shouldLoadParentTbl(UploadTable pt)
	{
		return super.shouldLoadParentTbl(pt) && pt != parent;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#loadMyRecord(edu.ku.brc.specify.datamodel.DataModelObjBase, int)
	 */
	@Override
	protected void loadMyRecord(DataModelObjBase rec, int seq) {
		DataModelObjBase parentRec = rec;
		if (rec != null && ((Treeable )rec).getRankId().equals(getRank())) {
			super.loadMyRecord(rec, seq);
			parentRec = (DataModelObjBase )((Treeable )rec).getParent();
		} else if (rec != null && ((Treeable )rec).getRankId() > getRank()) {
			loadMyRecord((DataModelObjBase )((Treeable )rec).getParent(), seq);
			return;
		} else {
			super.loadMyRecord(null, seq);
		}
		if (parent != null) {
			parent.loadMyRecord(parentRec, seq);		
		}
	}

	@Override
    protected void finishDepth(final DataModelObjBase rec, int seq) throws UploaderException {
        super.finishDepth(rec, seq);
        if (isUpdateMatches() && seq > 0) {
            throw new UploaderException("FinishDepth does not support seq > 0", UploaderException.ABORT_IMPORT);
        }
        depthRecords.add(rec);
    }

    /**
     *
     */
    @Override
    protected void finishRow() {
        super.finishRow();
        actualExportedRecordIdHasBeenSet = false;
        depth = 0;
        depthRecords.clear();
    }
    /**
     *
     * @return false if rock bottom else true
     */
    @Override
    protected boolean fallDown() {
        if (!updateMatches) {
            return false;
        } else {
            return fallFarther();
        }
    }

    /**
     *
     * @return
     */
    private boolean fallFarther() {
        if (exportedRecordId == actualExportedRecordId) {
           return false;
        } else {
            Integer currentRank = exportedRecordId == null ? getRank() :((Treeable)exportedRecord).getRankId();
            DataModelObjBase rec;
            if (getChild() != null) {
                rec = getChild().exportedRecord;
                if (rec != null && !(((Treeable)rec).getParent().getRankId() > currentRank)) {
                    return false;
                }
            } else {
                rec = actualExportedRecord;
            }
            if (rec == null || ((Treeable) rec).getParent().getRankId() <= currentRank) {
                return false;
            } else {
                while (((Treeable) rec).getParent().getRankId() > currentRank) {
                    rec = (DataModelObjBase) ((Treeable) rec).getParent();
                }
                exportedRecord = rec;
                exportedRecordId = rec.getId();
                depth++;
                return true;
            }
        }
    }

    /**
     *
     * @param recNum
     * @param restrictedVals
     * @return
     * @throws UploaderException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Override
    protected Map<DBInfoBase, Object> getOverridesForExportedRecMatching(int recNum, final List<MatchRestriction> restrictedVals)
            throws UploaderException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (depth == 0) {
            return super.getOverridesForExportedRecMatching(recNum, restrictedVals);
        } else {
            return getParentOverridesForExportedRecMatching(recNum);
        }
    }

    /**
     *
     * @param recNum
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws UploaderException
     */
    protected Map<DBInfoBase, Object> getParentOverridesForExportedRecMatching(int recNum) throws InvocationTargetException,
            IllegalArgumentException, IllegalAccessException, UploaderException {
        HashMap<DBInfoBase, Object> result = new HashMap<>();
        if (depth == 0) {
            result.putAll(super.getParentOverridesForExportedRecMatching(recNum));
            if (updateMatches && getParent() == null && exportedRecord != null) {
                result.put(getTable().getTableInfo().getRelationshipByName("parent"), ((Treeable)exportedRecord).getParent());
            }
        } else {
            DataModelObjBase parentRec = depthRecords.get(depth - 1);
            result.put(getTable().getTableInfo().getRelationshipByName("parent"), parentRec);
        }
        return result;
    }

    @Override
    protected boolean shouldSetExportedRec(DataModelObjBase rec) {
	    return false;
    }

    /**
     *
     * @return
     */
    protected DataModelObjBase findActualExportedRecord() {
        UploadTableTree bottom = this;
        while (bottom.getChild() != null) {
            bottom = bottom.getChild();
        }
        return bottom.actualExportedRecord;
    }


    @Override
    public void setExportedRecordId(DataModelObjBase rec) throws Exception {
	    if (getChild() == null && !actualExportedRecordIdHasBeenSet) {
	        actualExportedRecord = rec;
	        actualExportedRecordId = rec != null ? rec.getId() : null;
	        actualExportedRecordIdHasBeenSet = true;
        }
        super.setExportedRecordId(rec);
        DataModelObjBase parentRec = rec;
        if (rec != null && ((Treeable )rec).getRankId().equals(getRank())) {
            exportedRecordId = rec.getId();
            exportedRecord = rec;
            parentRec = (DataModelObjBase )((Treeable )rec).getParent();
        } else if (rec != null && ((Treeable )rec).getRankId() > getRank()) {
            setExportedRecordId((DataModelObjBase) ((Treeable) rec).getParent());
            return;
        }
        if (parent != null) {
            parent.setExportedRecordId(parentRec);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#isBlankVal(edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadField, int, int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData)
     */
    @Override
    protected boolean isBlankVal(UploadField fld, int seq, int row, UploadData uploadData) {
        boolean result = super.isBlankVal(fld, seq, row, uploadData);
        if (!result || uploadFields.size() == 1
                || (parent != null && !parent.blankSeqs.get(seq >= parent.getUploadFields().size() ? 0: seq))) {
            return false;
        }
        UploadTableTree kid = child;
        while (kid != null) {
            UploadField kidField = null;
            for (UploadField field : kid.uploadFields.get(seq)) {
                if (field.getField().getName().equals(fld.getField().getName())) {
                    kidField = field; 
                    break;
                }
            }
            if (kidField != null) {
                kidField.setValue(uploadData.get(row, kidField.getIndex()));
                if (!super.isBlankVal(kidField, seq, row, uploadData)) {
                    return false;
                }
            } else {
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
    protected void addInvalidValueMsgForOneToManySkip(List<UploadTableInvalidValue> msgs,
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

    List<Pair<Boolean, Boolean>> blankness(int row, UploadData uploadData)
    {
    	List<Pair<Boolean, Boolean>> result = new ArrayList<Pair<Boolean,Boolean>>();
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
            result.add(new Pair<Boolean,Boolean>(allBlank, nameBlank));
        }
    	return result;
    }
    
    protected UploadField getNameField(int seq)
    {
    	if (nameFields == null) 
    	{
    		nameFields = new ArrayList<UploadField>();
            for (Vector<UploadField> flds : uploadFields)
            {
                for (UploadField fld : flds)
                {
                    if (fld.getField().getName().equalsIgnoreCase("name"))
                    {
                        nameFields.add(fld);
                        break;
                    }
                }
            }
    	}
    	if (seq >= nameFields.size())
    	{
    		return null;
    	}
    	return nameFields.get(seq);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#validateRowValues(int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData, java.util.Vector)
     */
    @Override
    public void validateRowValues(int row,
                                     UploadData uploadData,
                                     List<UploadTableInvalidValue> invalidValues)
    {
        super.validateRowValues(row, uploadData, invalidValues);
        //check that the "name" (currently the 'main' field for all specify trees) is not blank or that all other fields are.
        List<Pair<Boolean, Boolean>> blankity = blankness(row, uploadData);
        int seq = 0;
        for (Pair<Boolean, Boolean> b : blankity)
        {
        	boolean allBlank = b.getFirst();
        	boolean nameBlank = b.getSecond();
            if (nameBlank && !allBlank)
            {
            	invalidValues.add(new UploadTableInvalidValue(null,
                        this, getNameField(seq), row, new Exception(getResourceString("WB_UPLOAD_INVALID_EMPTY_CELL"))));
            }        	
            seq++;
        }
//        for (Vector<UploadField> flds : uploadFields)
//        {
//            boolean nameBlank = true;
//            boolean allBlank = true;
//            UploadField mainFld = null;
//            for (UploadField fld : flds)
//            {
//                if (fld.getField().getName().equalsIgnoreCase("name"))
//                {
//                    mainFld = fld;
//                }
//                if (fld.getIndex() != -1)
//                {
//                    if (!StringUtils.isEmpty(uploadData.get(row, fld.getIndex())))
//                    {
//                        allBlank = false;
//                        if (fld == mainFld)
//                        {
//                            nameBlank = false;
//                        }
//                    }
//                }
//            }
//            if (nameBlank && !allBlank)
//            {
//                invalidValues.add(new UploadTableInvalidValue(null,
//                        this, mainFld, row, new Exception(getResourceString("WB_UPLOAD_INVALID_EMPTY_CELL"))));
//            }
//        }
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
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getParentParam(edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader.ParentTableEntry, int, java.util.HashMap)
	 */
	@Override
	protected DataModelObjBase getParentParam(ParentTableEntry pte, int recNum,
			HashMap<UploadTable, DataModelObjBase> overrideParentParams)
			throws UploaderException {
      	DataModelObjBase result =  overrideParentParams != null ? overrideParentParams.get(pte.getImportTable())
      			: pte.getImportTable().getParentRecord(recNum, this);
      	if (result == null)
      	{
      		UploadTableTree p = parent;
      		while (result == null && p != null) {
      			result = overrideParentParams.get(p);
      			p = p.parent;
      		}
      	}
      	return result;
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getMatchCriteria(edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace, int, java.util.Vector, java.util.HashMap)
     */
	protected Pair<Boolean, CriteriaIFace> getMatchCriteria(final DataProviderSessionIFace session, final int recNum,
			Vector<MatchRestriction> restrictedVals, 
			HashMap<UploadTable, DataModelObjBase> overrideParentParams) throws UploaderException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {
		Pair<Boolean, CriteriaIFace>  result =  super.getMatchCriteria(session, recNum, restrictedVals, overrideParentParams);
        if (!allowUnacceptedMatches) {
        //XXX It is possible for taxa (or other tree tables) to have null (interpreted as true) isAccepted
        //if they were entered outside of Specify or the Specify wizard. In that case this restriction
        //will fail and new tree nodes may be created unnecessarily.
        	if (result.getSecond() == null) {
        		result.setSecond(session.createCriteria(tblClass));
        	}
        	restrictedVals.add(new UploadTable.MatchRestriction("isAccepted", addRestriction(
                result.getSecond(), "isAccepted", new Boolean(true), false), -1));
        }
        return result;
	}
    
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#isMatchable(java.util.Set, int)
	 */
	@Override
	protected boolean isMatchable(Set<Integer> unmatchableCols, int seq) 
	{
		boolean result = parent != null ? parent.isMatchable(unmatchableCols, seq) : true;
		if (result)
		{
			result = super.isMatchable(unmatchableCols, seq);
		}
		return result;
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#shouldAddMissingReqFldToMatchCriteria(edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DefaultFieldEntry)
	 */
	@Override
	protected boolean shouldAddMissingReqFldToMatchCriteria(
			DefaultFieldEntry dfe) {
		boolean result = super.shouldAddMissingReqFldToMatchCriteria(dfe);
		if (result) {
			result = !"isaccepted".equalsIgnoreCase(dfe.getFldName());
		}
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

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#shouldClearParent(edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader.ParentTableEntry)
	 */
	@Override
	protected boolean shouldClearParent(ParentTableEntry pte) 
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable#getAdjustedSeqForBlankRowCheck(int)
	 */
	@Override
	protected int getAdjustedSeqForBlankRowCheck(int seq)
	{
		if (uploadFields.size() == 1 && seq > 0)
		{
			return 0;
		}
		return seq;
	}
    /**
     * @author timo
     * 
     * Message for non-accepted to accepted parent switches.
     *
     */
    public class AcceptedParentSwitchMessage extends BaseUploadMessage
    {
    	protected final int row;
    	
    	/**
    	 * @param msg
    	 * @param row
    	 */
    	public AcceptedParentSwitchMessage(String msg, int row)
    	{
    		super(msg);
    		this.row = row;
    	}

		/* (non-Javadoc)
		 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.BaseUploadMessage#getRow()
		 */
		@Override
		public int getRow()
		{
			return row;
		}
    	
    	
    }
}
