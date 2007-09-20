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

package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.ui.UIHelper;

/**
 * Hold information about the subset of returns results. Each Express Search can return results from several different
 * DB tables of information. This information is constructed from an XML descrption
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ExpressResultsTableInfo
{
    protected String                    id;
    protected String                    tableId;
    protected String                    title;
    protected String                    name;
    protected boolean                   isExpressSearch       = true;
    protected boolean                   isIndexed             = true;
    protected boolean                   isFieldNameOnlyForSQL = false;

    // These are used for viewing the results
    protected String                    iconName          = null;
    protected String                    viewSql;
    protected boolean                   viewSQLOverridden = false;
    
    protected ERTIJoinColInfo[]         joinCols          = null;

    protected List<ERTICaptionInfo>     captionInfo;         
    protected List<ERTICaptionInfo>     visibleCaptionInfo;  
    
    protected int                       recordSetColumnInx;
    protected Color                     color;

    /**
     * Constructs a table info object
     * @param tableElement the DOM4J element representing the information
     * @param loadType what type of info to load from the DOM
     * @param isExpressSearch true/false
     */
    public ExpressResultsTableInfo(final Element   tableElement, 
                                   final boolean   isExpressSearch)
    {
        this.isExpressSearch = isExpressSearch;

        fill(tableElement);
    }

    /**
     * Fill the current object with the info from the DOM depending on the LOAD_TYPE
     * @param tableElement the DOM4J element used to fill the object
     */
    public void fill(final Element tableElement)
    {
        id              = tableElement.attributeValue("id");
        tableId         = tableElement.attributeValue("tableid");
        title           = tableElement.attributeValue("title");
        name            = tableElement.attributeValue("name");
        color           = UIHelper.parseRGB(tableElement.attributeValue("color"));

        isIndexed    = getAttr(tableElement, "indexed", true);
        
        Element viewElement   = (Element)tableElement.selectSingleNode("detailView");
        Element sqlElement    = (Element)viewElement.selectSingleNode("sql");
        
        isFieldNameOnlyForSQL = getAttr(sqlElement, "fieldnameonly", false);
        viewSql               = StringUtils.strip(sqlElement.getText());
        iconName              = viewElement.attributeValue("icon");

        List<?> captionItems = viewElement.selectNodes("captions/caption");
        if (captionItems.size() > 0)
        {
            int captionCount = captionItems.size();
            captionInfo  = new Vector<ERTICaptionInfo>(captionCount);
            int i        = 0;
            for (Iterator<?> capIter = captionItems.iterator(); capIter.hasNext(); )
            {
                Element captionElement = (Element)capIter.next();
                ERTICaptionInfo capInfo = new ERTICaptionInfo(captionElement);

                if (capInfo.isVisible())
                {
                    captionInfo.add(capInfo);
                    capInfo.setPosIndex(i);
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
            throw new RuntimeException("No Captions!");
        }
        
        Element rsElement  = (Element)viewElement.selectSingleNode("recordset");
        recordSetColumnInx = Integer.parseInt(rsElement.attributeValue("col"));

        List<?> joinColItems = tableElement.selectNodes("joins/join");
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
     * Returns whether this Search Definition should be indexed or whether it is only used or viewing results.
     * @return whether this Search Definition should be indexed or whether it is only used or viewing results.
     */
    public boolean isIndexed()
    {
        return isIndexed;
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
     * Returns an array of ERTIColInfo Objects that describe the indexed Columns.
     * @return an array of the columns that are to be indexes
     */
    public List<ERTICaptionInfo> getVisibleCaptionInfo()
    {
        return visibleCaptionInfo;
    }

    /**
     * Returns an array of ERTIColInfo Objects that describe the indexed Columns.
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

    public int getRecordSetColumnInx()
    {
        return recordSetColumnInx;
    }

    public Color getColor()
    {
        return color;
    }

    public boolean isFieldNameOnlyForSQL()
    {
        return isFieldNameOnlyForSQL;
    }

}

