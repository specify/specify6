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
package edu.ku.brc.specify.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

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
public class SpecifySchemaI18NService extends SchemaI18NService
{
    private static final Logger      log      = Logger.getLogger(SpecifySchemaI18NService.class);
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SchemaI18NService#loadWithLocale(java.lang.Byte, int, edu.ku.brc.dbsupport.DBTableIdMgr, java.util.Locale)
     */
    @Override
    public void loadWithLocale(final Byte         schemaType, 
                               final int          disciplineId,
                               final DBTableIdMgr mgr, 
                               final Locale       locale)
    {
        // First do Just Hidden in case a table is missing a title or desc
        String sql = String.format("SELECT Name, IsHidden FROM  splocalecontainer WHERE SchemaType = %d AND DisciplineID = %d", schemaType, disciplineId);

        Vector<Object[]> rows = BasicSQLUtils.query(sql);

        for (Object[] row : rows)
        {
            DBTableInfo ti = mgr.getInfoByTableName(row[0].toString());
            if (ti != null)
            {
                Boolean isHidden = (Boolean)row[1];
                ti.setHidden(isHidden != null ? isHidden : false);

            } else
            {
                log.error("Couldn't find table [" + row[0] + "]");
            }
        }
        
        sql = "SELECT cn.Name, Text, cn.Aggregator, cn.IsUIFormatter, cn.Format FROM splocalecontainer cn INNER JOIN splocaleitemstr ON " +
              "cn.SpLocaleContainerID = splocaleitemstr.SpLocaleContainerNameID where Language = '"+locale.getLanguage()+"' AND " +
              "cn.SchemaType = " + schemaType +" AND cn.DisciplineID = " + disciplineId;

        rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            DBTableInfo ti = mgr.getInfoByTableName(row[0].toString());
            if (ti != null)
            {
                ti.setTitle(row[1].toString());
                ti.setAggregatorName(row[2] != null ? row[2].toString() : null);
                
                if (row[4] != null)
                {
                    if (row[3] != null && ((Boolean)row[3]))
                    {
                        ti.setUiFormatter(row[4].toString());
                    } else
                    {
                        ti.setDataObjFormatter(row[4].toString());
                    }
                }
            } else
            {
                log.error("Couldn't find table ["+row[0]+"]");
            }
        }
        
        sql = "SELECT cn.Name,Text FROM splocalecontainer cn INNER JOIN splocaleitemstr ON " +
              "cn.SpLocaleContainerID = splocaleitemstr.SpLocaleContainerDescID where Language = '"+locale.getLanguage()+"' AND " +
              "cn.SchemaType = " + schemaType +" AND cn.DisciplineID = " + disciplineId;
        
        rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            DBTableInfo ti = mgr.getInfoByTableName(row[0].toString());
            if (ti != null)
            {
                ti.setDescription(StringEscapeUtils.unescapeXml(row[1] != null ? row[1].toString() : null ));

            } else
            {
                log.error("Couldn't find table ["+row[0]+"]");
            }
        }
        
        sql = "SELECT cn.Name,splocalecontaineritem.Name,splocalecontaineritem.Format, " +
              "splocalecontaineritem.IsUIFormatter, splocalecontaineritem.PickListName, splocaleitemstr.Text, " +
              "splocalecontaineritem.IsHidden, splocalecontaineritem.WebLinkName , splocalecontaineritem.IsRequired  " +
              "FROM splocalecontainer cn INNER JOIN splocalecontaineritem ON cn.SpLocaleContainerID = splocalecontaineritem.SpLocaleContainerID "+
              "INNER JOIN splocaleitemstr ON splocalecontaineritem.SpLocaleContainerItemID = splocaleitemstr.SpLocaleContainerItemNameID "+
              " where splocaleitemstr.Language = '"+locale.getLanguage()+"' AND " +
              "cn.SchemaType = " + schemaType +" AND cn.DisciplineID = " + disciplineId + " order by cn.Name";
        log.debug(sql);
        
        String      name = "";
        DBTableInfo ti   = null;
        rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            String nm = row[0].toString();
            if (!name.equals(nm))
            {
                ti = mgr.getInfoByTableName(nm);
                name = nm;
            }
            
            if (ti != null)
            {
                DBTableChildIFace tblChild = ti.getItemByName(row[1].toString());
                if (tblChild != null)
                {
                    tblChild.setTitle(row[5] != null ? row[5].toString() : null);
                    Boolean isHidden = (Boolean)row[6];
                    tblChild.setHidden(isHidden != null ? isHidden : false);
                    
                } else
                {
                    log.error("Couldn't find field["+row[1]+"] for table ["+row[0]+"]");
                }
                
                if (tblChild instanceof DBFieldInfo)
                {
                    String  format       = row[2] == null ? null : row[2].toString();
                    boolean isUIFmt      = row[3] == null ? false : (Boolean)row[3];
                    String  pickListName = row[4] == null ? null : row[4].toString();
                    String  webLinkName  = row[7] == null ? null : row[7].toString();
                    boolean isRequired   = row[8] == null ? false : (Boolean)row[8];
                    
                    DBFieldInfo fieldInfo = (DBFieldInfo)tblChild;
                    fieldInfo.setPickListName(pickListName);
                    fieldInfo.setWebLinkName(webLinkName);
                    
                    if (!fieldInfo.isRequired() && isRequired)
                    {
                        fieldInfo.setRequired(isRequired);
                    }
                    
                    if (isUIFmt)
                    {
                        UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getInstance().getFormatter(format);
                        if (formatter != null)
                        {
                            fieldInfo.setFormatter(formatter);
                        } else
                        {
                            log.error("Couldn't find UIFieldFormatter with name ["+format+"]");
                        }
                        
                    } else if (StringUtils.isNotEmpty(format))
                    {
                        fieldInfo.setFormatStr(format);
                    }
                }
            } else
            {
                log.error("Couldn't find table ["+row[0]+"]");
            }
        }

        
        sql = "SELECT cn.Name, splocalecontaineritem.Name, splocaleitemstr.Text, splocalecontaineritem.IsHidden "+
              "FROM splocalecontainer cn INNER JOIN splocalecontaineritem ON cn.SpLocaleContainerID = splocalecontaineritem.SpLocaleContainerID "+
              "INNER JOIN splocaleitemstr ON splocalecontaineritem.SpLocaleContainerItemID = splocaleitemstr.SpLocaleContainerItemDescID "+
              " where splocaleitemstr.Language = '"+locale.getLanguage()+"' AND " +
              "cn.SchemaType = " + schemaType +" AND cn.DisciplineID = " + disciplineId + " order by cn.Name";
        
        
        name = "";
        ti   = null;
        rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            String nm = row[0].toString();
            if (!name.equals(nm))
            {
                ti   = mgr.getInfoByTableName(nm);
                name = nm;
            }
            
            if (ti != null)
            {
                DBTableChildIFace tblChild = ti.getItemByName(row[1].toString());
                if (tblChild != null)
                {
                    tblChild.setDescription(row[2] != null ? row[2].toString() : null);
                    Boolean isHidden = (Boolean)row[3];
                    tblChild.setHidden(isHidden != null ? isHidden : false);
                    
                } else
                {
                    log.error("Couldn't find field["+row[1]+"] for table ["+nm+"]");
                }
                
            } else
            {
                log.error("Couldn't find table ["+nm+"]");
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SchemaI18NService#getLocalesFromData(java.lang.Byte, int)
     */
    @Override
    public List<Locale> getLocalesFromData(final Byte schemaType, final int disciplineId)
    {
        List<Locale> localesList = new ArrayList<Locale>();
        
        String sql = String.format("SELECT i.Language, i.Country, i.Variant FROM splocalecontainer cn INNER JOIN splocaleitemstr i ON " +
                                   "cn.SpLocaleContainerID = i.SpLocaleContainerNameID WHERE cn.SchemaType = %d AND cn.DisciplineID = %d GROUP BY Language, Country, Variant",
                                   schemaType, disciplineId);
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            String language = (String)row[0];
            String country  = (String)row[1];
            String variant  = (String)row[2];
            
            Locale locale = null;
            
            if (StringUtils.isNotEmpty(language) && StringUtils.isNotEmpty(country) && StringUtils.isNotEmpty(variant))
            {
                locale = new Locale(language, country, variant);
                
            } else if (StringUtils.isNotEmpty(language) && StringUtils.isNotEmpty(country))
            {
                locale = new Locale(language, country);
                
            } else if (StringUtils.isNotEmpty(language))
            {
                locale = new Locale(language);
            }
            if (locale != null)
            {
                localesList.add(locale);
            }
        }
        return localesList;
    }
    
}
