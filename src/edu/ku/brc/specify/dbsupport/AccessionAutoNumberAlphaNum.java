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
package edu.ku.brc.specify.dbsupport;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.AutoNumberGeneric;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 8, 2007
 *
 */
public class AccessionAutoNumberAlphaNum extends AutoNumberGeneric
{
    
    private static final Logger  log = Logger.getLogger(AccessionAutoNumberAlphaNum.class);
    
    /**
     * Default Constructor. 
     */
    public AccessionAutoNumberAlphaNum()
    {
        super();
        
        classObj  = Accession.class;
        fieldName = "accessionNumber";
    }

    /**
     * Constructor with args.
     * @param properties the args
     */
    public AccessionAutoNumberAlphaNum(final Properties properties)
    {
        super(properties);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.AutoNumberGeneric#getHighestObject(edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace, org.hibernate.Session, java.lang.String, edu.ku.brc.util.Pair, edu.ku.brc.util.Pair)
     */
    @Override
    protected String getHighestObject(final UIFieldFormatterIFace formatter, 
                                      final Session session, 
                                      final String  value,
                                      final Pair<Integer, Integer> yearPos, 
                                      final Pair<Integer, Integer> pos) throws Exception
    {
        Division    currDivision = AppContextMgr.getInstance().getClassObject(Division.class);
        
        String ansSQL = "SELECT ans.AutonumberingSchemeID, ans.FormatName, ans.IsNumericOnly, ans.SchemeName, dv.Name, dv.DivisionID " +
        		        "FROM autonumberingscheme ans " +
        		        "Inner Join autonumsch_div ad ON ans.AutoNumberingSchemeID = ad.AutoNumberingSchemeID " +
        		        "Inner Join division dv ON ad.DivisionID = dv.UserGroupScopeId WHERE dv.UserGroupScopeId = %d AND FormatName = '%s'";
        String sql = String.format(ansSQL, currDivision.getId(), formatter.getName());
        log.debug(sql);
        
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        Integer ansID = null;
        if (rows.size() == 1)
        {
            ansID = (Integer)rows.get(0)[0];
            
        } else if (rows.size() == 0)
        {
            errorMsg = "There are NO formatters named ["+formatter.getName()+"]";
        } else
        {
            errorMsg = "Too many Formatters named ["+formatter.getName()+"]";
        }
        
        if (ansID == null)
        {
            log.debug(errorMsg);
            return null;
        }
        
        //String sql          = "SELECT autonumberingscheme.FormatName, autonumberingscheme.SchemeName, autonumberingscheme.IsNumericOnly, collection.CollectionName, discipline.Name, division.Name, accession.AccessionNumber FROM autonumberingscheme Inner Join autonumsch_coll ON autonumberingscheme.AutoNumberingSchemeID = autonumsch_coll.AutoNumberingSchemeID Inner Join collection ON autonumsch_coll.CollectionID = collection.UserGroupScopeId Inner Join discipline ON collection.DisciplineID = discipline.UserGroupScopeId Inner Join division ON discipline.DivisionID = division.UserGroupScopeId Inner Join accession ON division.UserGroupScopeId = accession.DivisionID ";
        //String ansToDivLong = "SELECT ans.FormatName, ans.SchemeName, ans.IsNumericOnly, c.CollectionName, ds.Name, dv.Name FROM autonumberingscheme ans Inner Join autonumsch_coll ac ON ans.AutoNumberingSchemeID = ac.AutoNumberingSchemeID Inner Join collection c ON ac.CollectionID = c.UserGroupScopeId Inner Join discipline ds ON c.DisciplineID = ds.UserGroupScopeId Inner Join division dv ON ds.DivisionID = dv.UserGroupScopeId ";
        String ansToDivSQL  = "SELECT dv.UserGroupScopeId DivID FROM autonumberingscheme ans " +
                        		"Inner Join autonumsch_div ad ON ans.AutoNumberingSchemeID = ad.AutoNumberingSchemeID " +
                        		"Inner Join division dv ON ad.DivisionID = dv.UserGroupScopeId " +
                        		"WHERE ans.AutoNumberingSchemeID = %d";
        
        ansToDivSQL = String.format(ansToDivSQL, ansID);
        log.debug(ansToDivSQL);
        
        Vector<Integer> divIds = new Vector<Integer>();
        for (Object[] row : BasicSQLUtils.query(ansToDivSQL))
        {
            divIds.add((Integer)row[0]);
        }
        
        Integer yearVal = null;
        if (yearPos != null && StringUtils.isNotEmpty(value) && value.length() >= yearPos.second)
        {
            yearVal = extractIntegerValue(yearPos, value).intValue();
        }

        StringBuilder sb = new StringBuilder("SELECT a.accessionNumber FROM Accession a Join a.division dv Join dv.numberingSchemes ans WHERE ans.id = ");
        AutoNumberingScheme accessionAutoNumScheme = currDivision.getNumberingSchemesByType(Accession.getClassTableId());
        if (accessionAutoNumScheme != null && accessionAutoNumScheme.getAutoNumberingSchemeId() != null)
        {
            sb.append(accessionAutoNumScheme.getAutoNumberingSchemeId());
            sb.append(" AND dv.id in (");
            for (Integer dvId : divIds)
            {
                sb.append(dvId);
                sb.append(',');
            }
            sb.setLength(sb.length()-1);
            sb.append(')');
            
        } else
        {
            UIRegistry.showError("There is no AutonumberingScheme for the Accession formatter!");
            return "";
        }
        
        if (yearVal != null)
        {
            sb.append(" AND ");
            sb.append(yearVal);
            sb.append(" = substring("+fieldName+","+(yearPos.first+1)+","+yearPos.second+")");
        }
        
        sb.append(" ORDER BY");
        
        try
        {
            if (yearPos != null)
            {
                sb.append(" substring("+fieldName+","+(yearPos.first+1)+","+yearPos.second+") desc");
            }
            
            if (pos != null)
            {
                if (yearPos != null)
                {
                    sb.append(", ");
                }
                sb.append(" substring("+fieldName+","+(pos.first+1)+","+pos.second+") desc");
            }
            
            System.out.println("AccessionAutoNumberAlphaNum - "+sb.toString());
            
            List<?> list = session.createQuery(sb.toString()).setMaxResults(1).list();
            if (list.size() == 1)
            {
                return list.get(0).toString();
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AccessionAutoNumberAlphaNum.class, ex);
            ex.printStackTrace();
        }
        return null;
    }
}
