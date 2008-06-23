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
import edu.ku.brc.af.core.expresssearch.ESTermParser;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.core.expresssearch.SearchTermField;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Feb 19, 2008
 *
 */
public class SearchQueryBuilder<T> implements ViewBasedSearchQueryBuilderIFace
{
    protected T                     nodeInForm;
    protected List<ERTICaptionInfo> cols        = new Vector<ERTICaptionInfo>();
    
    /**
     * @param nodeInForm
     */
    public SearchQueryBuilder(final T nodeInForm)
    {
        this.nodeInForm = nodeInForm;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.lang.String)
     */
    public String buildSQL(String searchText)
    {
        String queryStr = "";
        if (QueryAdjusterForDomain.getInstance().isUserInputNotInjectable(searchText))
        {
            int disciplineID = AppContextMgr.getInstance().getClassObject(Discipline.class).getId();

            // get node table and primary key column names
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName());
            String tableName = tableInfo.getName();
            String idColName = tableInfo.getIdColumnName();
            
            // get definition table and primary key column names
            DBTableInfo defTableInfo = DBTableIdMgr.getInstance().getByClassName(tableInfo.getRelationshipByName("definition").getClassName());
            String defTableName = defTableInfo.getName();
            String defIdColName = defTableInfo.getIdColumnName();
            
            String queryFormatStr = "SELECT n.FullName, n.%s from %s n INNER JOIN %s d ON n.%s = d.%s INNER JOIN discipline dsp ON d.%s = dsp.%s WHERE lower(n.FullName) LIKE \'%s\' AND dsp.DisciplineID = %d ORDER BY n.FullName asc";
            queryStr = String.format(queryFormatStr, idColName, tableName, defTableName, defIdColName, defIdColName, defIdColName, defIdColName, searchText.toLowerCase() + "%", disciplineID);
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
        
        String queryFormatStr = "SELECT %s.%s, %s from %s INNER JOIN %s d ON %s.%s = d.%s INNER JOIN discipline dsp ON d.%s = dsp.%s WHERE (%s) AND dsp.DisciplineID = %d ORDER BY %s";
        String queryStr = String.format(queryFormatStr, tableName, idColName, colNames.toString(), tableName, defTableName, tableName, defIdColName, defIdColName, 
                                        defIdColName, defIdColName, criteria.toString(), disciplineId, orderBy.toString());
        System.out.println(queryStr);
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