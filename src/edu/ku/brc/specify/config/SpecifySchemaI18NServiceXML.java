/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.config;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBInfoBase;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.specify.tools.schemalocale.DisciplineBasedContainer;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerXMLHelper;

/**
 * This class gets all the L10N string from the database for a locale and populates the DBTableInfo etc structures.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Oct 3, 2007
 *
 */
public class SpecifySchemaI18NServiceXML extends SchemaI18NService
{
    private static final Logger      log      = Logger.getLogger(SpecifySchemaI18NServiceXML.class);
    
    protected SchemaLocalizerXMLHelper schemaIO = null;
    
    
    /**
     * 
     */
    public SpecifySchemaI18NServiceXML()
    {
        
    }
    
    /**
     * @param names
     * @param descs
     * @param info
     */
    protected void setNameDesc(final Set<SpLocaleItemStr> names, 
                               final Set<SpLocaleItemStr> descs, 
                               DBInfoBase info)
    {
        for (Iterator<SpLocaleItemStr> iter=names.iterator();iter.hasNext();)
        {
            SpLocaleItemStr str = iter.next();
            if (str.getLanguage().equals(currentLocale.getLanguage()))
            {
                info.setTitle(str.getText());
                break;
            }
        }
        
        for (Iterator<SpLocaleItemStr> iter=descs.iterator();iter.hasNext();)
        {
            SpLocaleItemStr str = iter.next();
            if (str.getLanguage().equals(currentLocale.getLanguage()))
            {
                info.setDescription(str.getText());
                break;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SchemaI18NService#loadWithLocale(java.lang.Byte, int, edu.ku.brc.dbsupport.DBTableIdMgr, java.util.Locale)
     */
    @Override
    public void loadWithLocale(final Byte         schemaType, 
                               final int          collectionTypeId,
                               final DBTableIdMgr tableMgr, 
                               final Locale       locale)
    {
        schemaIO = new SchemaLocalizerXMLHelper(schemaType, tableMgr);
        schemaIO.load();
        
        Vector<DisciplineBasedContainer> containers = schemaIO.getSpLocaleContainers();
        
        for (DisciplineBasedContainer container : containers)
        {
            DBTableInfo ti = tableMgr.getInfoByTableName(container.getName());
            if (ti != null)
            {
               setNameDesc(container.getNames(), container.getDescs(), ti); 
               
               for (SpLocaleContainerItem item : container.getItems())
               {
                   DBFieldInfo fi = ti.getFieldByName(item.getName());
                   if (fi != null)
                   {
                      setNameDesc(item.getNames(), item.getDescs(), fi); 
                   } else
                   {
                       DBRelationshipInfo ri = ti.getRelationshipByName(item.getName());
                       if (ri != null)
                       {
                           setNameDesc(item.getNames(), item.getDescs(), ri); 
                       } else
                       {
                           log.error("Couldn't find field ["+item.getName()+"] in table ["+ti.getName()+"]");
                       }
                   }
               }
               
            } else
            {
                log.error("Couldn't find table ["+container.getName()+"]");
            }
        }
    }
}
