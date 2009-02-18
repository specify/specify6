/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.ESTermParser;
import edu.ku.brc.af.ui.SearchTermField;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * This class adds extra lookup conditions required for Combo Query Boxes that access Treeable objects.
 *  
 * Feb 19, 2008
 *
 */
public class TreeableSearchQueryBuilder implements ViewBasedSearchQueryBuilderIFace
{
    public static final int         PARENT = 0;
    public static final int         ACCEPTED_PARENT = 1;
    public static final int         HYBRID_PARENT = 2;
    
	protected Treeable<?, ?, ?>     nodeInForm;
    protected ValComboBox           rankCombo;
    protected boolean               accepted;
    protected int                   lookupType;
    protected List<ERTICaptionInfo> cols = new Vector<ERTICaptionInfo>();
    
    /**
     * @param nodeInForm
     */
    public TreeableSearchQueryBuilder(final Treeable<?,?,?> nodeInForm, final ValComboBox rankCombo, final int lookupType)
    {
        this.nodeInForm = nodeInForm;
        this.rankCombo = rankCombo;
        this.lookupType = lookupType;
        this.accepted = lookupType != HYBRID_PARENT;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public String buildSQL(String searchText, boolean isForCount)
    {
        //XXX use SQL instead of HQL, for portability.
        
        String queryStr = "";
        if (QueryAdjusterForDomain.getInstance().isUserInputNotInjectable(searchText))
        {
            TreeDefIface<?, ?, ?> treeDef = ((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass((Class<? extends Treeable<?,?,?>> )nodeInForm.getClass());
            Integer treeDefId = treeDef.getTreeDefId();
            
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName());
            
            // get definition table and primary key column names
            DBTableInfo defTableInfo = DBTableIdMgr.getInstance().getByClassName(tableInfo.getRelationshipByName("definition").getClassName());
            String defMemberName = defTableInfo.getShortClassName();
            defMemberName = defMemberName.substring(0, 1).toLowerCase().concat(defMemberName.substring(1));
            
            String queryFormatStr;
            if (isForCount)
            {
                queryFormatStr = "Select count(n.id) "; 
            }
            else
            {
                queryFormatStr = "SELECT n.fullName, n.id ";
            }
            queryFormatStr += "from %s n INNER JOIN n.definition d WHERE lower(n.fullName) LIKE \'%s\' AND d.id = %d";
            queryStr = String.format(queryFormatStr, tableInfo.getShortClassName(), searchText.toLowerCase() + "%", treeDefId);
            
            Integer nodeId = nodeInForm == null ? null : nodeInForm.getTreeId();
            Integer nodeNumber = null;
            Integer highestChildNodeNumber = null;
            if (nodeInForm != null)
            {
            	if (lookupType == ACCEPTED_PARENT && nodeId == null)
            	{
            	    //if doing acceptedParent lookup for new record, since no children can
            		//be present for new records, can use parent's node numbers to
            		// prevent ancestors being available for accepted parent 
            		//(this restriction is enforced by TreeTableViewer)
            		Treeable<?, ?, ?> parentNode = nodeInForm.getParent();
            		nodeNumber = parentNode == null ? null : parentNode.getNodeNumber();
            		highestChildNodeNumber = parentNode == null ? null : parentNode.getHighestChildNodeNumber();
            	}
            	else
            	{
            		nodeNumber = nodeInForm.getNodeNumber();
            		highestChildNodeNumber = nodeInForm.getHighestChildNodeNumber();
            	}
            }
            if (nodeId != null)
            {
                queryStr += " and n.id != " + nodeId;
            }
            
            if (rankCombo != null && rankCombo.getComboBox() != null)
            {
                TreeDefItemIface<?,?,?> rank = (TreeDefItemIface<?,?,?> )rankCombo.getValue();
                if (rank != null)
                {
                    if (lookupType == ACCEPTED_PARENT)
                    {
                    	queryStr += " and (n.rankId = " + rank.getRankId() + " or n.rankId >= " + treeDef.getSynonymizedLevel() + ") ";
                    }
                    else
                    {
                    	//Actually, it seems that this case is impossible, because for new nodes RankCombo is cleared when
                    	//parent control is modified. And when editing existing nodes, the parent control is not editable.
                	
                    	queryStr += " and n.rankId < " + rank.getRankId();
                    	//Now force rank to be greater than or equal the nearest required rank in the tree                    
                    	int minRank = 0;
                    	for (TreeDefItemIface defItem : treeDef.getTreeDefItems())
                    	{
                    		if (defItem.getRankId() == ((Number )rank).intValue())
                    		{
                    			break;
                    		}
                    		if (defItem.getIsEnforced())
                    		{
                    			minRank = defItem.getRankId();
                    		}
                    	}
                    	if (minRank > 0)
                    	{
                    		queryStr += " and n.rankId >= " + minRank;
                    	}
                    }
                }
                else
                {
                    if (lookupType == ACCEPTED_PARENT)
                    {
                    	queryStr += " and n.rankId >= " + treeDef.getSynonymizedLevel();
                    }
                    else
                    {
                    	int maxRank = 0;
                    	for (TreeDefItemIface defItem : treeDef.getTreeDefItems())
                    	{
                    		if (defItem.getRankId() > maxRank)
                    		{
                    			maxRank = defItem.getRankId();
                    		}
                    	}
                    	queryStr += " and n.rankId < " + maxRank;
                    }
                }
            }
            if (nodeNumber != null && highestChildNodeNumber != null)
            {
                //don't allow children to be used as (for example). hybrid parents
                queryStr += " and (n.nodeNumber not between " + nodeNumber + " and " + highestChildNodeNumber + ")";
                if (lookupType == ACCEPTED_PARENT)
                {
                	//don't allow ancestors to be accpeted parents. 
                	//The tree viewer enforces this -- although we are not sure why.
                	queryStr += " and (" + nodeNumber + " not between n.nodeNumber and n.highestChildNodeNumber)";
                }
            }
            
            if (accepted)
            {
                queryStr += " and n." + getAcceptedBooleanFldName() + " = true";
            }
            if (!isForCount)
            {
                queryStr += " ORDER BY n.fullName asc";
            }
            //log.debug(queryStr);
        }
        return queryStr;
    }

    protected String getAcceptedBooleanFldName()
    {
        return "isAccepted";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.util.Map, java.util.List)
     */
    @SuppressWarnings("unchecked")
    public String buildSQL(final Map<String, Object> dataMap, final List<String> fieldNames)
    {
     //XXX use SQL instead of HQL, for portability.
        String queryStr = "";
        TreeDefIface<?, ?, ?> treeDef = ((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass((Class<? extends Treeable<?,?,?>> )nodeInForm.getClass());
        Integer treeDefId = treeDef.getTreeDefId();
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName());
        String tableName = tableInfo.getName();
        
        // get definition table and primary key column names
        DBTableInfo defTableInfo = DBTableIdMgr.getInstance().getByClassName(tableInfo.getRelationshipByName("definition").getClassName());
        String defMemberName = defTableInfo.getShortClassName();
        defMemberName = defMemberName.substring(0, 1).toLowerCase().concat(defMemberName.substring(1));
        cols.clear();
        
        StringBuilder colNames = new StringBuilder();
        int dspCnt = 0;
        for (String colName : fieldNames)
        {
            if (dspCnt > 0) colNames.append(',');
            
            String columnName = colName;
            String fieldName;
            
            if (!colName.startsWith(tableName+"."))
            {
                columnName = tableName + "." + colName;
                fieldName = "n." + columnName.substring(0, 1).toLowerCase().concat(columnName.substring(1));
            }
            else
            {
                String fld = StringUtils.substringAfter(colName, ".");
                fieldName = "n." + fld.substring(0, 1).toLowerCase().concat(fld.substring(1));
            }
            
            colNames.append(fieldName);

            String baseName = StringUtils.substringAfter(colName, ".");
            if (StringUtils.isEmpty(baseName))
            {
                baseName = colName;
            }
            
            String colTitle;
            DBFieldInfo fi = tableInfo.getFieldByColumnName(baseName);
            if (fi != null)
            {
                colTitle = fi.getTitle();
            } else
            {
                colTitle = baseName;
            }
            
            ERTICaptionInfo col = new ERTICaptionInfo(columnName, colTitle, true, null, dspCnt+1);
            cols.add(col);
            dspCnt++;
        }
 
        StringBuilder orderBy  = new StringBuilder();
        StringBuilder criteria = new StringBuilder();
        int criCnt = 0;
        for (String colName : dataMap.keySet())
        {
            String data = (String)dataMap.get(colName);
            if (ESTermParser.getInstance().parse(data.toLowerCase(), true))
            {
                if (StringUtils.isNotEmpty(data))
                {
                    List<SearchTermField> fields     = ESTermParser.getInstance().getFields();
                    SearchTermField       firstTerm  = fields.get(0);
                    String                fieldName = colName;
                    
                    if (!colName.startsWith(tableName+"."))
                    {
                         fieldName = "n." + colName.substring(0, 1).toLowerCase().concat(defMemberName.substring(1));
                    }
                    else
                    {
                        String fld = StringUtils.substringAfter(colName, ".");
                        fieldName = "n." + fld.substring(0, 1).toLowerCase().concat(fld.substring(1));
                    }
    
                    if (criCnt > 0) criteria.append(" OR ");
                    
                    String clause = ESTermParser.getInstance().createWhereClause(firstTerm, null, fieldName);
                    criteria.append(clause);
                    
                    if (criCnt > 0) orderBy.append(',');
                    
                    orderBy.append(fieldName);
                    
                    criCnt++;
                }
            }
        }

        
        queryStr = "SELECT n.id, " + colNames;
        queryStr += " from " + tableInfo.getShortClassName()+ " n INNER JOIN n.definition d WHERE " + criteria.toString() + " AND d.id = " + treeDefId;
        
        Integer nodeId = nodeInForm == null ? null : nodeInForm.getTreeId();
        Integer nodeNumber = nodeInForm == null ? null : nodeInForm.getNodeNumber();
        Integer highestChildNodeNumber = nodeInForm == null ? null : nodeInForm.getHighestChildNodeNumber();
        
        if (nodeId != null)
        {
            queryStr += " and n.id != " + nodeId;
        }
        
        if (rankCombo != null)
        {
            Object rank = rankCombo.getValue();
            if (rank != null)
            {
                queryStr += " and n.rankId < " + rank;
            }
            else
            {
                int maxRank = 0;
                for (TreeDefItemIface defItem : treeDef.getTreeDefItems())
                {
                    if (defItem.getRankId() > maxRank)
                    {
                        maxRank = defItem.getRankId();
                    }
                }
                queryStr += " and n.rankId < " + maxRank;
            }
        }
        else if (nodeNumber != null && highestChildNodeNumber != null)
        {
            //don't allow children to be used as (for example). hybrid parents
            queryStr += " and (n.nodeNumber not between " + nodeNumber + " and " + highestChildNodeNumber + ")";
        }
        
        if (accepted)
        {
            queryStr += " and n." + getAcceptedBooleanFldName() + " = true";
        }
        if (!StringUtils.isBlank(orderBy.toString()))
        {
            queryStr += " ORDER BY " + orderBy.toString();
        }
        //log.debug(queryStr);
        return queryStr;
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.util.Map, java.util.List)
     */
//    public String buildSQL(final Map<String, Object> dataMap, final List<String> fieldNames)
//    {
//        int disciplineId = AppContextMgr.getInstance().getClassObject(Discipline.class).getId();
//
//        // get node table and primary key column names
//        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName());
//        String tableName = tableInfo.getName();
//        String idColName = tableInfo.getIdColumnName();
//        
//        // get definition table and primary key column names
//        DBTableInfo defTableInfo = DBTableIdMgr.getInstance().getByClassName(tableInfo.getRelationshipByName("definition").getClassName());
//        String defTableName = defTableInfo.getName();
//        String defIdColName = defTableInfo.getIdColumnName();
//        
//        cols.clear();
//        
//        StringBuilder colNames = new StringBuilder();
//        int dspCnt = 0;
//        for (String colName : fieldNames)
//        {
//            if (dspCnt > 0) colNames.append(',');
//            
//            String columnName = colName;
//            if (!colName.startsWith(tableName+"."))
//            {
//                columnName = tableName + "." + colName;
//            }
//            colNames.append(columnName);
//
//            String baseName = StringUtils.substringAfter(colName, ".");
//            if (StringUtils.isEmpty(baseName))
//            {
//                baseName = colName;
//            }
//            
//            String colTitle;
//            DBFieldInfo fi = tableInfo.getFieldByColumnName(baseName);
//            if (fi != null)
//            {
//                colTitle = fi.getTitle();
//            } else
//            {
//                colTitle = baseName;
//            }
//            
//            ERTICaptionInfo col = new ERTICaptionInfo(columnName, colTitle, true, null, dspCnt+1);
//            cols.add(col);
//            dspCnt++;
//        }
//        
//        
//        StringBuilder orderBy  = new StringBuilder();
//        StringBuilder criteria = new StringBuilder();
//        int criCnt = 0;
//        for (String colName : dataMap.keySet())
//        {
//            String data = (String)dataMap.get(colName);
//            if (ESTermParser.getInstance().parse(data.toLowerCase(), true))
//            {
//                if (StringUtils.isNotEmpty(data))
//                {
//                    List<SearchTermField> fields     = ESTermParser.getInstance().getFields();
//                    SearchTermField       firstTerm  = fields.get(0);
//                    String                columnName = colName;
//                    
//                    if (!colName.startsWith(tableName+"."))
//                    {
//                        columnName = tableName + "." + colName;
//                    }
//    
//                    if (criCnt > 0) criteria.append(" OR ");
//                    
//                    String clause = ESTermParser.getInstance().createWhereClause(firstTerm, null, columnName);
//                    criteria.append(clause);
//                    
//                    if (criCnt > 0) orderBy.append(',');
//                    
//                    orderBy.append(columnName);
//                    
//                    criCnt++;s
//                }
//            }
//        }
//        
//        String queryStr;
//        if (tableInfo.getTableId() == Storage.getClassTableId())
//        {
//            String queryFormatStr = "SELECT %s.%s, %s from %s INNER JOIN %s d ON %s.%s = d.%s WHERE (%s) ORDER BY %s";
//            queryStr = String.format(queryFormatStr, tableName, idColName, colNames.toString(), tableName, defTableName, tableName, defIdColName, defIdColName, criteria.toString(), orderBy.toString());
//        } else
//        {
//            String queryFormatStr = "SELECT %s.%s, %s from %s INNER JOIN %s d ON %s.%s = d.%s INNER JOIN discipline dsp ON d.%s = dsp.%s WHERE (%s) AND dsp.DisciplineID = %d ORDER BY %s";
//            queryStr = String.format(queryFormatStr, tableName, idColName, colNames.toString(), tableName, defTableName, tableName, defIdColName, defIdColName, 
//                    defIdColName, defIdColName, criteria.toString(), disciplineId, orderBy.toString());
//        }
//        //System.out.println(queryStr);
//        return queryStr;
//    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace#createQueryForIdResults()
     */
    public QueryForIdResultsIFace createQueryForIdResults()
    {
      //XXX use SQL instead of HQL, for portability.
      return new TableSearchResults(DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName()), cols, true); //true => is HQL
    }
}