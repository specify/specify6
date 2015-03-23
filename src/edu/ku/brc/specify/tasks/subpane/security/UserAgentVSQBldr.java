/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Dialog;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigCache;
import edu.ku.brc.af.ui.ESTermParser;
import edu.ku.brc.af.ui.SearchTermField;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.busrules.TableSearchResults;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Sep 22, 2009
 *
 */
public class UserAgentVSQBldr implements ViewBasedSearchQueryBuilderIFace
{
    private static final Logger log = Logger.getLogger(UserAgentVSQBldr.class);
    
    protected ExpressResultsTableInfo esTblInfo = null;
    protected ValComboBoxFromQuery    cbx;
    protected Integer                 disciplineID = null;
    
    /**
     * @param cbx
     */
    public UserAgentVSQBldr(final ValComboBoxFromQuery cbx)
    {
        this.cbx = cbx;
    }
    
    /**
     * @param dataMap
     * @param fieldNames
     * @return
     */
    @Override
    public String buildSQL(final Map<String, Object> dataMap, final List<String> fieldNames)
    {
        Vector<Object> disciplineIds   = BasicSQLUtils.querySingleCol("SELECT DisciplineID FROM discipline ORDER BY Name");
        if (disciplineIds.size() > 1)
        {
            Vector<Object> divisionNames = BasicSQLUtils.querySingleCol("SELECT Name FROM discipline ORDER BY Name");
            ToggleButtonChooserDlg<Object> divDlg = new ToggleButtonChooserDlg<Object>((Dialog)null, UIRegistry.getResourceString("SEC_PK_SRCH"), 
                                                                                       divisionNames, ToggleButtonChooserPanel.Type.RadioButton);
            divDlg.setUseScrollPane(true);
            divDlg.createUI();
            divDlg.getCancelBtn().setVisible(false);
            
            divDlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            UIHelper.centerAndShow(divDlg);
            int inx = divisionNames.indexOf(divDlg.getSelectedObject());
            disciplineID = (Integer)disciplineIds.get(inx);
            
        } else
        {
            disciplineID = (Integer)disciplineIds.get(0);
        }
        
        String searchName = cbx.getSearchName();
        if (searchName != null)
        {
            esTblInfo = ExpressSearchConfigCache.getTableInfoByName(searchName);
            if (esTblInfo != null)
            {
               String sqlStr = esTblInfo.getViewSql();
               return buildSearchString(dataMap, fieldNames, StringUtils.replace(sqlStr, "DSPLNID", disciplineID.toString()));
            }
        }
        return null;
    }
    
    /**
     * @param searchText
     * @param isForCount
     * @return
     */
    @Override
    public String buildSQL(String searchText, boolean isForCount)
    {
        /*String newEntryStr = searchText + '%';
        String sql = String.format("SELECT %s FROM Agent a LEFT JOIN a.specifyUser s INNER JOIN a.division d WHERE d.id = " + divId +
                                   " AND s = null AND LOWER(a.lastName) LIKE '%s2' ORDER BY a.lastName",
                                           isForCount ? "count(*)" : "a.lastName, a.firstName, a.agentId", newEntryStr);
        log.debug(sql);
        return sql;*/
        return null;
    }
    
    /**
     * @return
     */
    @Override
    public QueryForIdResultsIFace createQueryForIdResults()
    {
        return new TableSearchResults(DBTableIdMgr.getInstance().getInfoById(Agent.getClassTableId()), esTblInfo.getCaptionInfo()); //true => is HQL
    }
    
    /**
     * @param dataMap
     * @param fieldNames
     * @param sqlTemplate
     * @return
     */
    protected String buildSearchString(final Map<String, Object> dataMap, 
                                       final List<String>        fieldNames,
                                       final String              sqlTemplate)
    {
        StringBuilder orderBy  = new StringBuilder();
        StringBuilder criteria = new StringBuilder("agent.SpecifyUserID IS NULL AND (");
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
                    
                    if (criCnt > 0) criteria.append(" OR ");
                    
                    String clause = ESTermParser.getInstance().createWhereClause(firstTerm, null, colName);
                    criteria.append(clause);
                    
                    if (criCnt > 0) orderBy.append(',');
                    
                    orderBy.append(colName);
                    
                    criCnt++;
                }
            }
        }
        
        criteria.append(")");
        
        String sqlStr = null;
        
        StringBuffer sb = new StringBuffer();
        sb.append(criteria);

        
        int inxGrpBy = sqlTemplate.toLowerCase().indexOf( "group by");
        if (inxGrpBy == -1)
        {
            sb.append(" ORDER BY ");
            sb.append(orderBy);
            sqlStr = StringUtils.replace(sqlTemplate, "(%s)", sb.toString());
        } else
        {
            sqlStr = StringUtils.replace(sqlTemplate, "(%s)", sb.toString());
            sqlStr += " ORDER BY " + orderBy;
        }
        
        log.debug(sqlStr);
        
        return sqlStr;
    }
    
}