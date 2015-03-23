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
package edu.ku.brc.specify.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
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
    private static final Logger        log      = Logger.getLogger(SpecifySchemaI18NServiceXML.class);
    
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
                info.setDescription(StringEscapeUtils.unescapeXml(str.getText()));
                break;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SchemaI18NService#loadWithLocale(java.lang.Byte, int, edu.ku.brc.dbsupport.DBTableIdMgr, java.util.Locale)
     */
    @Override
    public void loadWithLocale(final Byte         schemaType, 
                               final int          disciplineId,
                               final DBTableIdMgr tableMgr, 
                               final Locale       locale)
    {
        schemaIO = new SchemaLocalizerXMLHelper(schemaType, tableMgr);
        schemaIO.load(true);
        
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SchemaI18NService#getLocalesFromData(java.lang.Byte, int)
     */
    @Override
    public List<Locale> getLocalesFromData(final Byte schemaType, final int disciplineId)
    {
        List<Locale> locales = new ArrayList<Locale>();
        
        HashSet<String> hash = new HashSet<String>();
        for (DisciplineBasedContainer container : schemaIO.getSpLocaleContainers())
        {
            for (SpLocaleItemStr str : container.getNames())
            {
                String language = str.getLanguage();
                String country  = str.getCountry();
                String variant  = str.getVariant();
                
                String key = String.format("%s_%s_%s", language, country != null ? country : "", variant != null ? variant : "");
                if (!hash.contains(key))
                {
                    hash.add(key);
                    Locale locale = null;
                    if (StringUtils.isNotBlank(language) && StringUtils.isNotBlank(country) && StringUtils.isNotBlank(variant))
                    {
                        locale = new Locale(language, country, variant);
                        
                    } else if (StringUtils.isNotBlank(language) && StringUtils.isNotBlank(country))
                    {
                        locale = new Locale(language, country);
                        
                    } else if (StringUtils.isNotBlank(language))
                    {
                        locale = new Locale(language);
                    }
                    if (locale != null)
                    {
                        locales.add(locale);
                    }
                }
            }
        }
        return locales;
    }

}
