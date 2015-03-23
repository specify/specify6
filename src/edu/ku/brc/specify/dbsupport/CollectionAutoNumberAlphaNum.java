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
package edu.ku.brc.specify.dbsupport;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.AutoNumberGeneric;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 8, 2007
 *
 */
public class CollectionAutoNumberAlphaNum extends AutoNumberGeneric
{
    /**
     * Default Constructor. 
     */
    public CollectionAutoNumberAlphaNum()
    {
        super();
        
        classObj  = CollectionObject.class;
        fieldName = "catalogNumber";
    }

    /**
     * Constructor with args.
     * @param properties the args
     */
    public CollectionAutoNumberAlphaNum(final Properties properties)
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
        Collection currCollection = AppContextMgr.getInstance().getClassObject(Collection.class);
        
        Integer yearVal = null;
        if (yearPos != null && StringUtils.isNotEmpty(value) && value.length() >= yearPos.second)
        {
            yearVal = extractIntegerValue(yearPos, value).intValue();
        }

        StringBuilder sb = new StringBuilder("SELECT c.catalogNumber From CollectionObject c Join c.collection col Join col.numberingSchemes cns WHERE cns.autoNumberingSchemeId = ");
        sb.append(currCollection.getNumberingSchemesByType(CollectionObject.getClassTableId()).getAutoNumberingSchemeId());
        
        if (yearVal != null)
        {
            sb.append(" AND ");
            sb.append(yearVal);
            sb.append(" = substring("+fieldName+","+(yearPos.first+1)+","+yearPos.second+")");
        }
        
        sb.append(" AND c.collectionMemberId = COLMEMID ORDER BY");
        
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
            
            String sql = QueryAdjusterForDomain.getInstance().adjustSQL(sb.toString());
            //System.out.println(sql);
            List<?> list = session.createQuery(sql).setMaxResults(1).list();
            if (list.size() == 1)
            {
                return list.get(0).toString();
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionAutoNumberAlphaNum.class, ex);
        }
        return null;
    }
}
