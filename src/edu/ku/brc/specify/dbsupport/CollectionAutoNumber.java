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

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.AutoNumberGeneric;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.util.Pair;

/**
 * Note: 'getHighestObject' from the base class never gets called. This class' getHighestObject gets called directly from
 * the owning object which is CatalogNumberUIFieldFormatter. This only 
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jun 20, 2007
 *
 */
public class CollectionAutoNumber extends AutoNumberGeneric
{
    protected static final Logger log = Logger.getLogger(CollectionAutoNumber.class);
    
    /**
     * Default Constructor. 
     */
    public CollectionAutoNumber()
    {
        super();
        
        classObj  = CollectionObject.class;
        fieldName = "catalogNumber";
    }

    /**
     * Constructor with args.
     * @param properties the args
     */
    public CollectionAutoNumber(final Properties properties)
    {
        super(properties);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.AutoNumberGeneric#getHighestObject(edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace, org.hibernate.Session, java.lang.String, edu.ku.brc.util.Pair, edu.ku.brc.util.Pair)
     */
    @Override
    protected Object getHighestObject(final UIFieldFormatterIFace formatter, 
                                      final Session session,
                                      final String value,
                                      final Pair<Integer, Integer> yearPos,
                                      final Pair<Integer, Integer> pos) throws Exception
    {
        boolean doDebug = false;
        
        Collection          currCollection = AppContextMgr.getInstance().getClassObject(Collection.class);
        AutoNumberingScheme catNumScheme   = currCollection.getNumberingSchemesByType(CollectionObject.getClassTableId());
        if (catNumScheme == null)
        {
            throw new RuntimeException("The Catalog Numbering Scheme cannot be null! Collection Table ID: "+CollectionObject.getClassTableId());
        }
        catNumScheme = (AutoNumberingScheme)session.merge(catNumScheme);
        
        if (doDebug) System.out.println("CatNumScheme: "+catNumScheme.getSchemeName());
        
        StringBuilder sb = new StringBuilder(" From CollectionObject c Join c.collection col Join col.numberingSchemes cns WHERE cns.autoNumberingSchemeId = ");
        sb.append(catNumScheme.getAutoNumberingSchemeId());
        
        sb.append(" AND c.collectionMemberId = COLMEMID ORDER BY CatalogNumber DESC");
        try
        {
            String sql = QueryAdjusterForDomain.getInstance().adjustSQL(sb.toString());
            //System.out.println(sql);
            List<?> list = session.createQuery(sql).setMaxResults(1).list();
            if (list.size() == 1)
            {
                Object[] objArray = (Object[]) list.get(0);
                return objArray[0];
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
