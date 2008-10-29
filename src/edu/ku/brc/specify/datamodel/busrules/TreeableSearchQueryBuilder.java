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
import edu.ku.brc.af.core.expresssearch.ESTermParser;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.core.expresssearch.SearchTermField;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.TreeDefIface;
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
    protected Treeable<?, ?, ?>     nodeInForm;
    protected ValComboBox           rankCombo;
    protected boolean               accepted;
    protected List<ERTICaptionInfo> cols = new Vector<ERTICaptionInfo>();
    
    /**
     * @param nodeInForm
     */
    public TreeableSearchQueryBuilder(final Treeable<?,?,?> nodeInForm, final ValComboBox rankCombo, final boolean accepted)
    {
        this.nodeInForm = nodeInForm;
        this.rankCombo = rankCombo;
        this.accepted = accepted;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public String buildSQL(String searchText, boolean isForCount)
    {
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
            }
            else if (nodeNumber != null && highestChildNodeNumber != null)
            {
                //don't allow children to be used as (for example). hybrid parents
                queryStr += " and (n.nodeNumber not between " + nodeNumber + " and " + highestChildNodeNumber + ")";
            }
            
            if (accepted)
            {
                queryStr += " and n.accepted = true";
            }
            queryStr += " ORDER BY n.fullName asc";

            //log.debug(queryStr);
        }
        return queryStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.util.Map, java.util.List)
     */
    public String buildSQL(final Map<String, Object> dataMap, final List<String> fieldNames)
    {
        int disciplineId = AppContextMgr.getInstance().getClassObject(Discipline.class).getId();

        // get node table and primary key column names
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName());
        String tableName = tableInfo.getName();
        String idColName = tableInfo.getIdColumnName();
        
        // get definition table and primary key column names
        DBTableInfo defTableInfo = DBTableIdMgr.getInstance().getByClassName(tableInfo.getRelationshipByName("definition").getClassName());
        String defTableName = defTableInfo.getName();
        String defIdColName = defTableInfo.getIdColumnName();
        
        cols.clear();
        
        StringBuilder colNames = new StringBuilder();
        int dspCnt = 0;
        for (String colName : fieldNames)
        {
            if (dspCnt > 0) colNames.append(',');
            
            String columnName = colName;
            if (!colName.startsWith(tableName+"."))
            {
                columnName = tableName + "." + colName;
            }
            colNames.append(columnName);

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
            if (ESTermParser.parse(data.toLowerCase(), true))
            {
                if (StringUtils.isNotEmpty(data))
                {
                    List<SearchTermField> fields     = ESTermParser.getFields();
                    SearchTermField       firstTerm  = fields.get(0);
                    String                columnName = colName;
                    
                    if (!colName.startsWith(tableName+"."))
                    {
                        columnName = tableName + "." + colName;
                    }
    
                    if (criCnt > 0) criteria.append(" OR ");
                    
                    String clause = ESTermParser.createWhereClause(firstTerm, null, columnName);
                    criteria.append(clause);
                    
                    if (criCnt > 0) orderBy.append(',');
                    
                    orderBy.append(columnName);
                    
                    criCnt++;
                }
            }
        }
        
        String queryStr;
        if (tableInfo.getTableId() == Storage.getClassTableId())
        {
            String queryFormatStr = "SELECT %s.%s, %s from %s INNER JOIN %s d ON %s.%s = d.%s WHERE (%s) ORDER BY %s";
            queryStr = String.format(queryFormatStr, tableName, idColName, colNames.toString(), tableName, defTableName, tableName, defIdColName, defIdColName, criteria.toString(), orderBy.toString());
        } else
        {
            String queryFormatStr = "SELECT %s.%s, %s from %s INNER JOIN %s d ON %s.%s = d.%s INNER JOIN discipline dsp ON d.%s = dsp.%s WHERE (%s) AND dsp.DisciplineID = %d ORDER BY %s";
            queryStr = String.format(queryFormatStr, tableName, idColName, colNames.toString(), tableName, defTableName, tableName, defIdColName, defIdColName, 
                    defIdColName, defIdColName, criteria.toString(), disciplineId, orderBy.toString());
        }
        //System.out.println(queryStr);
        return queryStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace#createQueryForIdResults()
     */
    public QueryForIdResultsIFace createQueryForIdResults()
    {
        return new TableSearchResults(DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName()), cols);
    }
}