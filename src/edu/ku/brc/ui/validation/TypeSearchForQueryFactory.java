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
package edu.ku.brc.ui.validation;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.db.TextFieldWithInfo;

/**
 * This factory knows how to create AutoComplete Comboboxes that get their data from a query.
 
 * @code_status Beta
 **
 * @author rods
 *
 */
public class TypeSearchForQueryFactory
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(TypeSearchForQueryFactory.class);
    
    protected static TypeSearchForQueryFactory instance = new TypeSearchForQueryFactory();
    
    // Data Members
    protected Hashtable<String, TypeSearchInfo> hash = new Hashtable<String, TypeSearchInfo>();
    
    /**
     * Protected Constructor.
     */
    protected TypeSearchForQueryFactory()
    {
        load();
    }
    
    /**
     * Loads the formats from the config file.
     *
     */
    public void load()
    {

        try
        {
            Element root  = AppContextMgr.getInstance().getResourceAsDOM("TypeSearches");
            if (root != null)
            {
                List<?> typeSearches = root.selectNodes("/typesearches/typesearch");
                for (Object fObj : typeSearches)
                {
                    Element tsElement = (Element)fObj;
                    String name = tsElement.attributeValue("name");
                    if (StringUtils.isNotBlank(name))
                    {
                        hash.put(name, new TypeSearchInfo(XMLHelper.getAttr(tsElement, "tableid", -1),
                                                          tsElement.attributeValue("searchcol"),
                                                          tsElement.attributeValue("searchfield"),
                                                          tsElement.attributeValue("displaycols"),
                                                          tsElement.attributeValue("format"),
                                                          tsElement.attributeValue("dataobjformatter")));
                    } else
                    {
                        log.error("TypeSearchInfo element is missing or has a blank name!");
                    }
                }
            } else
            {
                log.debug("Couldn't open typesearch_def.xml");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }


    /**
     * Creates a new ValComboBoxFromQuery by name.
     * @param name the name of the ValComboBoxFromQuery to return
     * @return a ValComboBoxFromQuery by name
     */
    public static TextFieldWithInfo getTextFieldWithInfo(final String name)
    {
        TypeSearchInfo typeSearchInfo = instance.hash.get(name);
        if (typeSearchInfo != null)
        {
            DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.lookupInfoById(typeSearchInfo.getTableId());
            if (tblInfo != null)
            {
    
                return new TextFieldWithInfo(tblInfo.getClassName(),
                                             tblInfo.getIdFieldName(),
                                             typeSearchInfo.getDisplayColumns(),
                                             typeSearchInfo.getFormat(),
                                             typeSearchInfo.getFormatName(),
                                             tblInfo.getNewObjDialog(),
                                             tblInfo.getObjTitle());
    
            } else
            {
                log.error("Table with ID["+typeSearchInfo.getTableId()+"] not found.");
            }
        } else
        {
            log.error("Object Type Search Name ["+name+"] not found.");
        }
        return null;
    }
    
    /**
     * For a given Formatter it returns the formatName.
     * @param name the name of the formatter to use
     * @return the name of the formatter
     */
    public static String getFormatName(final String name)
    {
        TypeSearchInfo typeSearchInfo = instance.hash.get(name);
        if (typeSearchInfo != null)
        {
            return typeSearchInfo.getFormatName();
            
        } else
        {
            log.error("Object Type Search Name ["+name+"] not found.");
        }
        return null;
    }

    /**
     * Creates a new ValComboBoxFromQuery by name.
     * @param name the name of the ValComboBoxFromQuery to return
     * @return a ValComboBoxFromQuery by name
     */
    public static ValComboBoxFromQuery createValComboBoxFromQuery(final String name)
    {
        TypeSearchInfo typeSearchInfo = instance.hash.get(name);
        if (typeSearchInfo != null)
        {
            DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.lookupInfoById(typeSearchInfo.getTableId());
            if (tblInfo != null)
            {
                return new ValComboBoxFromQuery(tblInfo.getTableName(),
                                                tblInfo.getIdColumnName(),
                                                typeSearchInfo.getSearchColumn(),
                                                typeSearchInfo.getDisplayColumns(),
                                                tblInfo.getClassName(),
                                                tblInfo.getIdFieldName(),
                                                typeSearchInfo.getSearchFieldName(),
                                                typeSearchInfo.getFormat(),
                                                typeSearchInfo.getFormatName(),
                                                tblInfo.getSearchDialog(),
                                                tblInfo.getNewObjDialog(),
                                                tblInfo.getObjTitle());

    
            } else
            {
                log.error("Table with ID["+typeSearchInfo.getTableId()+"] not found.");
            }
        } else
        {
            log.error("Object Type Search Name ["+name+"] not found.");
        }


        throw new RuntimeException("Couldn't create ValComboBoxFromQuery by name["+name+"]");
    }

    //-----------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------
    class TypeSearchInfo
    {
        protected int    tableId;
        protected String searchColumn;
        protected String displayColumns;
        protected String searchFieldName;
        protected String format;
        protected String formatName;

        public TypeSearchInfo(int    tableId,
                               String searchColumn,
                               String displayColumns,
                               String searchFieldName,
                               String format,
                               String formatName)
        {
            this.tableId         = tableId;
            this.searchColumn    = searchColumn;
            this.displayColumns  = displayColumns;

            this.searchFieldName = searchFieldName;
            this.format          = format;
            this.formatName      = formatName;
        }

        public int getTableId()
        {
            return tableId;
        }

        public String getDisplayColumns()
        {
            return displayColumns;
        }

        public String getFormat()
        {
            return format;
        }

        public String getFormatName()
        {
            return formatName;
        }

        public String getSearchColumn()
        {
            return searchColumn;
        }

        public String getSearchFieldName()
        {
            return searchFieldName;
        }
    }


}
