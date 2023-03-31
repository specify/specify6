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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.DatamodelHelper;


/**
 * @author timbo
 *
 *this class creates Uploader definitions for a WorkbenchTemplate
 *
 * @code_status Alpha
 *
 */


public class WorkbenchUploadMapper
{
    protected static boolean           debugging = true;
    
    protected static final Logger      log         = Logger.getLogger(WorkbenchUploadMapper.class);

    protected final Collection<WorkbenchTemplateMappingItem> wbtItems;
    protected Vector<Short>            mappedItems = null;
    protected Vector<UploadMappingDef> maps        = null;
    
    protected Map<String, DefInfo>  defs;
    
    protected Map<String, TreeLevelInfo>   taxonLevels;
    protected Map<String, TreeLevelInfo>   geoLevels;
    protected Map<String, TreeLevelInfo>   lithoLevels;
    protected Map<String, TreeLevelInfo>   chronoLevels;
    protected Map<String, TreeLevelInfo>   storageLevels;
    
    
    
    private class DefInfo
    {
        public String table;
        public String name;
        public Integer oneToManySequence;
        public String sequenceField;
        public String actualTable;
        public String actualName;
        public String relationshipName;
        public String relatedFieldName;
        public String treeName;
        public String mapToField;
        public Integer rankId;
        
        /**
         * @param xmlNode
         */
        public DefInfo(final Element xmlNode) 
        {
            table = xmlNode.attributeValue("table");
            name = xmlNode.attributeValue("name");
            oneToManySequence = xmlNode.attributeValue("onetomanysequence") == null ? null : new Integer(xmlNode.attributeValue("onetomanysequence"));
            sequenceField = xmlNode.attributeValue("sequencefield");
            actualTable = xmlNode.attributeValue("actualtable");
            if (actualTable == null)
            {
                actualTable = table;
            }
            actualName = xmlNode.attributeValue("actualname");
            if (actualName == null)
            {
                actualName = name;
            }
            relationshipName = xmlNode.attributeValue("relationshipname");
            relatedFieldName = xmlNode.attributeValue("relatedfieldname");
            treeName = xmlNode.attributeValue("treename");
            mapToField = xmlNode.attributeValue("maptofield");
            rankId = xmlNode.attributeValue("rankid") == null ? null : new Integer(xmlNode.attributeValue("rankid"));            
        }
        
        @Override
        public String toString()
        {
            String result = new String((table == null ? "null" : table)
              + "." + (name == null ? "null" : name)  
              + ": 1-many: " + (oneToManySequence == null ? "null" : oneToManySequence.toString()) 
              + ", sequenceField: " + (sequenceField == null ? "null" : sequenceField)
              + ", actualTable: " + (actualTable == null ? "null" : actualTable)
              + ", actualName: " + (actualName == null ? "null" : actualName)
              + ", relName: " + (relationshipName == null ? "null" : relationshipName)
              + ", relFld: " + (relatedFieldName == null ? "null" : relatedFieldName)
              + ", treeName: "  + (treeName == null ? "null" : treeName)
              + ", mapTo: " + (mapToField == null ? "null" : mapToField)
              + ", rankID: " + (rankId == null ? "null" : rankId.toString()));
            return result;
        }
    }
    
    /**
     * @author timbo
     *
     * info about treeLevels for taxon, geography, ...
     *
     */
    private class TreeLevelInfo implements Comparable<TreeLevelInfo>
    {
        protected int     rank;
        protected boolean required;
        protected Integer sequence;
        protected int index;
        protected String wbFldName;
        protected String wbTblName;
        protected String fldName;
        
        public TreeLevelInfo(int rank, boolean required, Integer sequence, String fldName)
        {
            this.rank = rank;
            this.required = required;
            this.sequence = sequence;
            this.fldName = fldName;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(TreeLevelInfo info)
        {
            if (this.rank < info.rank)
            {
                return -1;
            }
            if (this.rank > info.rank)
            {
                return 1;
            }
            if (this.index < info.index)
            {
                return -1;
            }
            if (this.index > info.index)
            {
                return 1;
            }
            if (this.getSequence() != null && info.getSequence() != null)
            {
                return this.getSequence().compareTo(info.getSequence());
            }
            return 0;
        }

        /**
         * @return the index
         */
        public final int getIndex()
        {
            return index;
        }

        /**
         * @param index the index to set
         */
        public final void setIndex(int index)
        {
            this.index = index;
        }

        /**
         * @return the rank
         */
        public final int getRank()
        {
            return rank;
        }

        /**
         * @return the required
         */
        public final boolean isRequired()
        {
            return required;
        }

        /**
         * @return the sequence
         */
        public final Integer getSequence()
        {
            return sequence;
        }

        /**
         * @return the wbFldName
         */
        public final String getWbFldName()
        {
            return wbFldName;
        }

        /**
         * @param wbFldName the wbFldName to set
         */
        public final void setWbFldName(String wbFldName)
        {
            this.wbFldName = wbFldName;
        }

        
        /**
         * @return
         */
        public String getWbTblName() {
			return wbTblName;
		}

		/**
		 * @param wbTblName
		 */
		public void setWbTblName(String wbTblName) {
			this.wbTblName = wbTblName;
		}

		/**
         * @return the fldName
         */
        public String getFldName()
        {
            return fldName;
        }
    }

    /**
     * @return Map of defs for 'special' import fields.
     */
    protected Map<String, DefInfo> buildDefs()
    {
        Element xmlDef = getXMLDef();
        if (xmlDef != null)
        {
            Map<String, DefInfo> result = new HashMap<String, DefInfo>();
            for (Iterator<?> i = xmlDef.elementIterator("field"); i.hasNext();)
            {
                Element fld = (Element) i.next();
                try
                {
                    result.put(fld.attributeValue("table").toLowerCase() + "." + fld.attributeValue("name").toLowerCase(),
                            new DefInfo(fld));
                }
                catch (java.lang.NumberFormatException numEx)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchUploadMapper.class, numEx);
                    log.error("Bad number format in workbench upload def file: "
                            + DatamodelHelper.getDatamodelFilePath());
                    return null;
                }
            }
            return result;
        }
        log.error(getClass().getName() + ": unable to build defs");
        return null;
    }

    /**
     * @param wbt - the workbench template being uploaded.
     */
    public WorkbenchUploadMapper(final Collection<WorkbenchTemplateMappingItem> wbtItems)
    {
        this.wbtItems = wbtItems;
        mappedItems = new Vector<Short>();
        defs = buildDefs();
        taxonLevels = buildTreeLevels("taxon");
        geoLevels = buildTreeLevels("geography");
        lithoLevels = buildTreeLevels("lithostrat");
        chronoLevels = buildTreeLevels("geologictimeperiod");
        storageLevels = buildTreeLevels("storage");
        maps = new Vector<UploadMappingDef>();
    }

    /**
     * @param wbt - the workbench template being uploaded.
     */
    public WorkbenchUploadMapper(final WorkbenchTemplate wbt)
    {
        this(wbt.getWorkbenchTemplateMappingItems());
    }

    /**
     * @param treeName - the tree to build levels for.
     * @return TreeLevelInfos to use in a UploadMappingDefTree objects
     */
    protected Map<String, TreeLevelInfo> buildTreeLevels(String treeName)
    {
        if (defs != null)
        {
            Map<String, TreeLevelInfo> result = new HashMap<String, TreeLevelInfo>();
            for (DefInfo def : defs.values())
            {
                if (treeName.equals(def.treeName))
                {
                    //System.out.println(def.name);
                    result.put(def.name, new TreeLevelInfo(def.rankId, getTreeLevelFieldRequirement(def), def.oneToManySequence, def.mapToField));
                }
            }
            return result;
        }
        return null;
    }

    /**
     * @param fldName
     * @return the default isRequired status for the treelevel field defined by defInfo 
     */
    protected boolean getTreeLevelFieldRequirement(final DefInfo defInfo)
    {
        //this works because the 'main' field for all Specify treeables is named "name".
        return defInfo.mapToField.equalsIgnoreCase("name");
    }

    /**
     * @return Element for root object in workbenc upload def file.
     */
    protected final Element getXMLDef()
    {
        try
        {
            if (WorkbenchTask.isCustomizedSchema()) 
            {
            	return XMLHelper.readFileToDOM4J(new File(UIRegistry.getAppDataDir() + File.separator + "specify_workbench_upload_def.xml"));
            } else
            {
            	return XMLHelper.readDOMFromConfigDir("specify_workbench_upload_def.xml");
            }
            
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchUploadMapper.class, ex);
            log.error(ex);
            return null;
        }
    }

    /**
     * @return Vector of mappings for each column in this.wbt.
     */
    public Vector<UploadMappingDef> getImporterMapping() throws UploaderException
    {
        //mapAttachments();
        try
        {
            mapTrees();
            mapRelationships();
            mapLeftovers();
            return maps;
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchUploadMapper.class, ex);
            if (ex instanceof UploaderException)
            {
                throw (UploaderException )ex;
            }
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
    }
    
    /**
     * @throws UploaderException
     * 
     * maps Attached images, BGM data, etc...
     */
//    protected void mapAttachments() throws UploaderException
//    {
//        for (WorkbenchTemplateMappingItem wbi : wbt.getWorkbenchTemplateMappingItems())
//        {
//            if (!isMapped(wbi.getViewOrder()) && isAttachmentCol(wbi))
//            {
//                mapAttachmentCol(wbi);
//            }
//        }
//    }
//    
//    protected boolean isBiogeomancerCol(final WorkbenchTemplateMappingItem wbi)
//    {
//        return wbi.getrow.getBioGeomancerResults() != null && !row.getBioGeomancerResults().equals("")
//    }
//    protected boolean isAttachmentCol(final WorkbenchTemplateMappingItem wbi)
//    {
//        return false;
//    }
//    
//    protected void mapAttachmentCol(final WorkbenchTemplateMappingItem wbi) throws UploaderException
//    {
//        throw new UploaderException("bad attachment.", UploaderException.ABORT_IMPORT);
//    }
    
    /**
     * maps fields that don't involve tree relationships or one-to-many relationships
     */
    protected void mapLeftovers()
    {
        for (WorkbenchTemplateMappingItem wbi : wbtItems)
        {
            if (!isMapped(wbi.getViewOrder()))
            {
                DefInfo def = findDef(wbi);
                String fld;
                if (def == null)
                {
                    fld = wbi.getFieldName();
                }
                else
                {
                    fld = def.actualName;
                }
                maps.add(new UploadMappingDef(wbi.getTableName(), fld, wbi.getViewOrder(), wbi.getCaption()));
                mappedItems.add(wbi.getViewOrder());
            }
        }
    }
    /**
     * @throws UploaderException
     * 
     * creates mappings for one-to-many relationships
     */
    protected void mapRelationships() throws UploaderException
    {
        for (WorkbenchTemplateMappingItem wbi : wbtItems)
        {
            if (!isMapped(wbi.getViewOrder()))
            {
                DefInfo def = findDef(wbi);
                if (def != null && def.relationshipName != null)
                {
                    mapRelationship(wbi, def);
                    mappedItems.add(wbi.getViewOrder());
                }
            }
        }
    }
    
    /**
     * @param wbi
     * 
     * @return DefInfo corresponding to wbi.
     */
    protected final DefInfo findDef(final WorkbenchTemplateMappingItem wbi)
    {
        if (defs != null) { return defs.get(wbi.getTableName().toLowerCase() + "." + wbi.getFieldName().toLowerCase()); }
        return null;
    }
    
    /**
     * @param wbi
     * @param def
     * 
     * creates mapping for wbi.
     * 
     * @throws UploaderException
     */
    protected void mapRelationship(final WorkbenchTemplateMappingItem wbi, final DefInfo def) throws UploaderException
    {
        DBRelationshipInfo rel = DBTableIdMgr.getInstance().getByShortClassName(def.actualTable)
                .getRelationshipByName(def.relationshipName);
        if (rel == null) 
        { 
            log.debug("relationship '" + def.relationshipName + "' for table '" + def.actualTable + "' not found in database schema.");
            throw new UploaderException("relationship '" + def.relationshipName + "' for table '" + def.actualTable + "' not found in database schema.",
                UploaderException.ABORT_IMPORT); 
        }
        
        DBTableInfo relTbl = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
        UploadMappingDefRel map = findRelMap(def, rel.getColName(), relTbl.getShortClassName());
        if (map == null)
        {
            
            map = new UploadMappingDefRel(def.actualTable, rel.getColName(), relTbl.getShortClassName(), def.oneToManySequence, def.sequenceField, wbi.getCaption());
            maps.add(map);
        }
        if (def.relatedFieldName != null)
        {
            map.addRelatedField(def.relatedFieldName, wbi.getViewOrder(), wbi.getCaption());
        }
        else
        {
            map.addLocalField(def.actualName, wbi.getViewOrder(), wbi.getCaption());
       }
    }

    /**
     * @param def
     * @param foreignKey
     * @param relatedTable
     * @return an UploadMappingDefRel for the relationship that applies to def, if one has already been created.
     */
    protected final UploadMappingDefRel findRelMap(final DefInfo def, final String foreignKey, final String relatedTable)
    {
        for (UploadMappingDef map : maps)
        {
            if (map.getClass() == UploadMappingDefRel.class)
            {
                UploadMappingDefRel relMap = (UploadMappingDefRel)map;
                if (relMap.getTable().equals(def.actualTable) && relMap.getField().equals(foreignKey) && relMap.getRelatedTable().equals(relatedTable))
                {
                    Integer mapSeq = relMap.getSequence();
                    if (mapSeq != null && mapSeq.equals(def.oneToManySequence))
                    {
                        return relMap;
                    }
                    if (mapSeq == null && def.oneToManySequence == null)
                    {
                        return relMap;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * processes columns that are levels in 'trees', such as taxon and geography.
     */
    protected void mapTrees() throws Exception
    {
        Vector<WorkbenchTemplateMappingItem> taxTreeItems = new Vector<WorkbenchTemplateMappingItem>();
        Vector<WorkbenchTemplateMappingItem> geoTreeItems = new Vector<WorkbenchTemplateMappingItem>();
        Vector<WorkbenchTemplateMappingItem> lithoTreeItems = new Vector<WorkbenchTemplateMappingItem>();
        Vector<WorkbenchTemplateMappingItem> chronoTreeItems = new Vector<WorkbenchTemplateMappingItem>();
        Vector<WorkbenchTemplateMappingItem> storageTreeItems = new Vector<WorkbenchTemplateMappingItem>();

        for (WorkbenchTemplateMappingItem wbi : wbtItems)
        {
            if (!isMapped(wbi.getViewOrder()))
            {
                if (isTaxTreeItem(wbi))
                {
                    taxTreeItems.add(wbi);
                }
                else if (isGeoTreeItem(wbi))
                {
                    geoTreeItems.add(wbi);
                }
                else if (isLithoTreeItem(wbi))
                {
                    lithoTreeItems.add(wbi);
                }
                else if (isChronoTreeItem(wbi))
                {
                	chronoTreeItems.add(wbi);
                }
                else if (isStorageTreeItem(wbi))
                {
                	storageTreeItems.add(wbi);
                }
            }
        }
        mapTaxTreeItems(taxTreeItems);
        mapGeoTreeItems(geoTreeItems);
        mapLithoTreeItems(lithoTreeItems);
        mapChronoTreeItems(chronoTreeItems);
        mapStorageTreeItems(storageTreeItems);
    }

    /**
     * builds mapping for taxonomic levels
     * 
     * @param treeItems - the columns in this.wbt that are taxon levels
     */
    protected void mapTaxTreeItems(Vector<WorkbenchTemplateMappingItem> treeItems) throws Exception
    {
/*        log.debug("taxTreeItems:");
        for (WorkbenchTemplateMappingItem wbi : treeItems)
        {
            log.debug(wbi.getCaption() + ": " + wbi.getTableName() + "."
                    + wbi.getFieldName());
        }
        log.debug("");*/
        if (treeItems.size() > 0)
        {
            Vector<Vector<TreeMapElement>> levels = mapTreeItems(treeItems, taxonLevels);
            if (levels.size() > 0)
            {
                maps.add(new UploadMappingDefTree("taxon", "name", "parentId", null, levels, "Taxon"/*i18n*/));
            }
        }
    }

    /**
     * @param tmeVector
     * @param elements
     * @return element in elements that equal to tmeVector, or null.
     */
    protected Vector<TreeMapElement> findTreeMapElement(final Vector<TreeMapElement> tmeVector,
                                                        SortedSet<Vector<TreeMapElement>> elements)
    {
        SortedSet<Vector<TreeMapElement>> tail = elements.tailSet(tmeVector);
        if (tail != null && tail.size() > 0
                && elements.comparator().compare(tmeVector, tail.first()) == 0) { return tail
                .first(); }
        return null;
    }

    /**
     * builds mapping for geographic levels
     * 
     * @param treeItems - the columns in this.wbt that are geography levels
     */
    protected void mapGeoTreeItems(Vector<WorkbenchTemplateMappingItem> treeItems) throws Exception
    {
        log.debug("geoTreeItems:");
        for (WorkbenchTemplateMappingItem wbi : treeItems)
        {
            log.debug(wbi.getCaption() + ": " + wbi.getTableName() + "."
                    + wbi.getFieldName());
        }
        log.debug("");
        if (treeItems.size() > 0)
        {
            Vector<Vector<TreeMapElement>> levels = mapTreeItems(treeItems, geoLevels);
            if (levels.size() > 0)
            {
                maps.add(new UploadMappingDefTree("geography", "name", "parentId", null, levels, "geography"/*i18n*/));
            }
        }
    }

    /**
     * builds mapping for litho levels
     * 
     * @param treeItems - the columns in this.wbt that are litho levels
     */
    protected void mapLithoTreeItems(Vector<WorkbenchTemplateMappingItem> treeItems) throws Exception
    {
        log.debug("lithoTreeItems:");
        for (WorkbenchTemplateMappingItem wbi : treeItems)
        {
            log.debug(wbi.getCaption() + ": " + wbi.getTableName() + "."
                    + wbi.getFieldName());
        }
        log.debug("");
        if (treeItems.size() > 0)
        {
            Vector<Vector<TreeMapElement>> levels = mapTreeItems(treeItems, lithoLevels);
            if (levels.size() > 0)
            {
                maps.add(new UploadMappingDefTree("lithostrat", "name", "parentId", null, levels, "lithostrat"/*i18n*/));
            }
        }
    }
   
    /**
     * builds mapping for chrono levels
     * 
     * @param treeItems - the columns in this.wbt that are chrono levels
     */
    protected void mapChronoTreeItems(Vector<WorkbenchTemplateMappingItem> treeItems) throws Exception
    {
        log.debug("chronoTreeItems:");
        for (WorkbenchTemplateMappingItem wbi : treeItems)
        {
            log.debug(wbi.getCaption() + ": " + wbi.getTableName() + "."
                    + wbi.getFieldName());
        }
        log.debug("");
        if (treeItems.size() > 0)
        {
            Vector<Vector<TreeMapElement>> levels = mapTreeItems(treeItems,chronoLevels);
            if (levels.size() > 0)
            {
                maps.add(new UploadMappingDefTree("geologictimeperiod", "name", "parentId", null, levels, "geologictimeperiod"/*i18n*/));
            }
        }
    }

    
    /**
     * builds mapping for storage levels
     * 
     * @param treeItems - the columns in this.wbt that are storage levels
     */
    protected void mapStorageTreeItems(Vector<WorkbenchTemplateMappingItem> treeItems) throws Exception
    {
        log.debug("storageTreeItems:");
        for (WorkbenchTemplateMappingItem wbi : treeItems)
        {
            log.debug(wbi.getCaption() + ": " + wbi.getTableName() + "."
                    + wbi.getFieldName());
        }
        log.debug("");
        if (treeItems.size() > 0)
        {
            Vector<Vector<TreeMapElement>> levels = mapTreeItems(treeItems, storageLevels);
            if (levels.size() > 0)
            {
                maps.add(new UploadMappingDefTree("storage", "name", "parentId", null, levels, "storage"/*i18n*/));
            }
        }
    }

    protected void logDebug(Object toDebug)
    {
        if (debugging)
        {
            log.debug(toDebug);
        }
    }
    
    /**
     * @param treeItems WorkbenchTemplateMappingItems corresponding to levels in tree
     * @param levels defined for the tree
     * 
     * @return TreeMapElements for the levels in this.wbt.
     */
    protected Vector<Vector<TreeMapElement>> mapTreeItems(Vector<WorkbenchTemplateMappingItem> treeItems,
                                                          Map<String, TreeLevelInfo> ranks)
    {
        SortedSet<TreeLevelInfo> levels = new TreeSet<TreeLevelInfo>();
        for (WorkbenchTemplateMappingItem wbi : treeItems)
        {
            logDebug(wbi.getCaption() + ", " + wbi.getFieldName());
            TreeLevelInfo levelInfo = ranks.get(wbi.getFieldName());
            levelInfo.setIndex(wbi.getViewOrder());
            levelInfo.setWbFldName(wbi.getCaption());
            levelInfo.setWbTblName(wbi.getTableName());
            levels.add(levelInfo);
            mappedItems.add(wbi.getViewOrder());
        }
        Vector<Vector<TreeMapElement>> result = new Vector<Vector<TreeMapElement>>(levels.size());
        Iterator<TreeLevelInfo> levelsIter = levels.iterator();
        int currentRank = -1;
        Vector<TreeMapElement> currentElement = null;
        String prevWbTbl = null;
        boolean isSubTree = false;
        while (levelsIter.hasNext())
        {
            TreeLevelInfo level = levelsIter.next();
            logDebug(level.getWbFldName() + ", " + level.getIndex() + ", " + level.getRank());
            if (currentElement == null || level.getRank() != currentRank)
            {
                currentElement = new Vector<TreeMapElement>();
                result.add(currentElement);
                currentRank = level.getRank();
            }
            //isSubTree calc is pretty much hardwired for case where taxon tree levels are defined within the determination table...
            isSubTree |= prevWbTbl != null && level.getWbTblName() != null && !level.getWbTblName().equals(prevWbTbl);
            currentElement.add(new TreeMapElement(level.getIndex(), level.getFldName(), level.getWbFldName(), level.getRank(), level
                    .getSequence(), level.isRequired(), isSubTree));
            prevWbTbl = level.getWbTblName();
        }
        return result;
    }

    /**
     * @param itemIdx
     * @return
     */
    protected boolean isMapped(short itemIdx)
    {
        return mappedItems.contains(itemIdx);
    }

    /**
     * @param wbi
     * @return
     */
    protected boolean isTaxTreeItem(WorkbenchTemplateMappingItem wbi)
    {
//        return (wbi.getTableName().equals("taxon") || wbi.getTableName().equals("determination"))
//        && taxonLevels.containsKey(wbi.getFieldName()); /* plus other fields??? */
        return (wbi.getSrcTableId().equals(4) || wbi.getSrcTableId().equals(4000) || wbi.getTableName().equals("determination"))
                && taxonLevels.containsKey(wbi.getFieldName()); /* plus other fields??? */
    }

    /**
     * @param wbi
     * @return
     */
    protected boolean isGeoTreeItem(WorkbenchTemplateMappingItem wbi)
    {
        return wbi.getTableName().equals("geography") && geoLevels.containsKey(wbi.getFieldName());
    }
    

    /**
     * @param wbi
     * @return
     */
    protected boolean isLithoTreeItem(WorkbenchTemplateMappingItem wbi)
    {
        return wbi.getTableName().equals("lithostrat") && lithoLevels.containsKey(wbi.getFieldName());
    }

    
    /**
     * @param wbi
     * @return
     */
    protected boolean isStorageTreeItem(WorkbenchTemplateMappingItem wbi)
    {
        return (wbi.getTableName().equals("storage") || wbi.getTableName().equals("preparation")) && storageLevels.containsKey(wbi.getFieldName());
    }

    /**
     * @param wbi
     * @return
     */
    protected boolean isChronoTreeItem(WorkbenchTemplateMappingItem wbi)
    {
        return wbi.getTableName().equals("geologictimeperiod") && chronoLevels.containsKey(wbi.getFieldName());
    }

/*Saving this stuff in case it's needed again...
    for (UploadMappingDef map : maps)
    {
        System.out.println(map.getTable() + "." + map.getField() + " [" + map.getIndex() + "]");
        if (map.getClass() == UploadMappingDefTree.class)
        {
            UploadMappingDefTree treeMap = (UploadMappingDefTree) map;
            for (Vector<TreeMapElement> level : treeMap.getLevels())
            {
                for (TreeMapElement me : level)
                {
                    System.out.println("     " + me.getRank() + ", [" + me.getIndex() + "]"
                            + " (" + me.getSequence() + ")");
                }
            }
        }
        else if (map.getClass() == UploadMappingDefRel.class)
        {
            UploadMappingDefRel relMap = (UploadMappingDefRel)map;
            System.out.println("     " + relMap.getRelatedTable() + ", " + relMap.getSequence() + ", " + relMap.getSequenceFld());
            for (ImportMappingRelFld fld : relMap.getLocalFields())
            {
                System.out.println("   " + fld.getFieldName() + " (" + fld.getFldIndex() + ")");
            }
            for (ImportMappingRelFld fld : relMap.getRelatedFields())
            {
                System.out.println("       *" + fld.getFieldName() + " (" + fld.getFldIndex() + ")");
            }
        }
    }
*/

}


