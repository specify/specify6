/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.UIHelper;

/**
 * Hold information about the subset of returns results. Each Express Search can return results from several different
 * DB tables of information. This information is constructed from an XML description
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ExpressResultsTableInfo
{
    private static final Logger log = Logger.getLogger(ExpressResultsTableInfo.class);
    
    protected String                    id;
    protected String                    tableId;
    protected String                    title;
    protected String                    name;
    protected boolean                   isExpressSearch       = true;
    protected boolean                   isFieldNameOnlyForSQL = false;
    protected String                    description;

    // These are used for viewing the results
    protected String                    iconName          = null;
    protected String                    viewSql;
    protected boolean                   viewSQLOverridden = false;
    
    protected ERTIJoinColInfo[]         joinCols          = null;

    protected List<ERTICaptionInfo>     captionInfo;         
    protected List<ERTICaptionInfo>     visibleCaptionInfo;  
    
    protected Color                     color;
    
    // Transient
    private DBTableInfo                 tableInfo = null;

    /**
     * Constructs a table info object
     * @param tableElement the DOM4J element representing the information
     * @param loadType what type of info to load from the DOM
     * @param isExpressSearch true/false
     */
    public ExpressResultsTableInfo(final Element        tableElement, 
                                   final boolean        isExpressSearch,
                                   final ResourceBundle resBundle)
    {
        this.isExpressSearch = isExpressSearch;

        fill(tableElement, resBundle);
    }

    /**
     * Fill the current object with the info from the DOM depending on the LOAD_TYPE
     * @param tableElement the DOM4J element used to fill the object
     */
    public void fill(final Element        tableElement,
                     final ResourceBundle resBundle)
    {
        id              = tableElement.attributeValue("id"); //$NON-NLS-1$
        tableId         = tableElement.attributeValue("tableid"); //$NON-NLS-1$
        name            = tableElement.attributeValue("name"); //$NON-NLS-1$
        color           = UIHelper.parseRGB(tableElement.attributeValue("color")); //$NON-NLS-1$

        if (isExpressSearch)
        {
            title = resBundle.getString(name);
            if (StringUtils.isEmpty(title))
            {
                log.error("Express Search with name["+name+"] is missing it's title in the expressearch properties file."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            description = resBundle.getString(name + "_desc"); //$NON-NLS-1$
            if (StringUtils.isEmpty(description))
            {
                log.error("Express Search with name["+name+"] is missing it's description in the expressearch properties file."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else
        {
            DBTableInfo tblInfo = getTableInfo();
            if (tblInfo != null)
            {
                title = tblInfo.getTitle();
            }
        }
        
        if (StringUtils.isEmpty(title))
        {
            title = getResourceString("ExpressResultsTableInfo.NOTITLE");  // XXX This should never happen! //$NON-NLS-1$
        }
        
        Element viewElement   = (Element)tableElement.selectSingleNode("detailView"); //$NON-NLS-1$
        Element sqlElement    = (Element)viewElement.selectSingleNode("sql"); //$NON-NLS-1$
        
        isFieldNameOnlyForSQL = getAttr(sqlElement, "fieldnameonly", false); //$NON-NLS-1$
        viewSql               = StringUtils.strip(sqlElement.getText());
        iconName              = viewElement.attributeValue("icon"); //$NON-NLS-1$

        List<?> captionItems = viewElement.selectNodes("captions/caption"); //$NON-NLS-1$
        if (captionItems.size() > 0)
        {
            int captionCount = captionItems.size();
            captionInfo  = new Vector<ERTICaptionInfo>(captionCount);
            int i        = 0;
            for (Iterator<?> capIter = captionItems.iterator(); capIter.hasNext(); )
            {
                Element captionElement = (Element)capIter.next();
                ERTICaptionInfo capInfo = new ERTICaptionInfo(captionElement, resBundle);

                if (capInfo.isVisible())
                {
                    captionInfo.add(capInfo);
                    capInfo.setPosIndex(i);
                    if (capInfo.getColName() == null && capInfo.getColInfoList().size() > 0)
                    {
                        i += capInfo.getColInfoList().size() - 1;
                    }
                } else
                {
                    capInfo.setPosIndex(-1);
                }
                i++;
            }
            
            if (captionInfo.size() != captionCount)
            {
                // Create mappings of visible items
                visibleCaptionInfo = new Vector<ERTICaptionInfo>(captionInfo.size());
                for (ERTICaptionInfo c : captionInfo)
                {
                    visibleCaptionInfo.add(c);
                }
            }
            
        } else
        {
            throw new RuntimeException("No Captions!"); //$NON-NLS-1$
        }
        
        List<?> joinColItems = tableElement.selectNodes("joins/join"); //$NON-NLS-1$
        if (joinColItems != null && joinColItems.size() > 0)
        {
            joinCols = new ERTIJoinColInfo[joinColItems.size()];
            for (int i=0;i<joinColItems.size();i++)
            {
                joinCols[i] = new ERTIJoinColInfo((Element)joinColItems.get(i));
            }
        }
    }

    /**
     * Cleanup any memory.
     */
    public void cleanUp()
    {
        captionInfo = null;
        joinCols    = null;
        viewSql     = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    public void finalize()
    {
        cleanUp();
    }

    /**
     * Returns an array of ERTICaptionInfo Objects that describe the indexed Columns.
     * @return an array of the columns that are to be indexes
     */
    public List<ERTICaptionInfo> getVisibleCaptionInfo()
    {
        return visibleCaptionInfo;
    }

    /**
     * Returns an array of ERTICaptionInfo Objects that describe the indexed Columns.
     * @return an array of the columns that are to be indexes
     */
    public List<ERTICaptionInfo> getCaptionInfo()
    {
        return captionInfo;
    }

    /**
     * Returns an array of ERTIJoinColInfo Objects that describe the indexed Join Columns.
     * @return an array of the columns that are to be indexes
     */
    public ERTIJoinColInfo[] getJoins()
    {
        return joinCols;
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public String getViewSql()
    {
        return viewSql;
    }

    public void setViewSql(String viewSql)
    {
        this.viewSql = viewSql;
        viewSQLOverridden = true;
    }

    public void setViewSQLOverridden(boolean viewSQLOverridden)
    {
        this.viewSQLOverridden = viewSQLOverridden;
    }

    public String getId()
    {
        return id;
    }

    public String getTableId()
    {
        return tableId;
    }

    public String getIconName()
    {
        return iconName;
    }
    public int getVisColCount()
    {
        return visibleCaptionInfo.size();
    }

    public Color getColor()
    {
        return color;
    }

    public boolean isFieldNameOnlyForSQL()
    {
        return isFieldNameOnlyForSQL;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        if (tableInfo == null)
        {
            tableInfo = DBTableIdMgr.getInstance().getInfoById(tableId);
        }
        return tableInfo;
    }

}

