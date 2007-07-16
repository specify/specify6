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

package edu.ku.brc.af.core;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.exceptions.ConfigurationException;

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
    protected boolean                   isExpressSearch = true;
    protected boolean                   isIndexed       = true;

    // These are used for viewing the results
    protected String                    iconName          = null;
    protected String                    viewSql;
    protected boolean                   viewSQLOverridden = false;
    
    protected ERTIColInfo[]             colInfo           = null;
    protected ERTIJoinColInfo[]         joinCols          = null;

    // These data members are use for indexing
    protected boolean                   useHitsCache  = false;
    protected String                    patternSql;
    protected String                    buildSql;
    protected String                    updateSql;
    
    protected ERTICaptionInfo[]         captionInfo;         
    protected ERTICaptionInfo[]         visibleCaptionInfo;  
    
    protected Hashtable<String, String> outOfDate     = new Hashtable<String, String>();

    //protected int                       tableType;
    protected int                       recordSetColumnInx;
    protected int                       priority;
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
     * Parse comma separated r,g,b string
     * @param rgb the string with comma separated color values
     * @return the Color object
     */
    protected Color parseRGB(final String rgb)
    {
        StringTokenizer st = new StringTokenizer(rgb, ",");
        if (st.countTokens() == 3)
        {
            String r = st.nextToken().trim();
            String g = st.nextToken().trim();
            String b = st.nextToken().trim();
            return new Color(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
        }
        throw new ConfigurationException("R,G,B value is bad ["+rgb+"]");
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
        priority        = getAttr(tableElement, "priority", 1);
        color           = parseRGB(tableElement.attributeValue("color"));

        isIndexed    = getAttr(tableElement, "indexed", true);
        useHitsCache = getAttr(tableElement, "usehitscache", false);
        
        Element viewElement  = (Element)tableElement.selectSingleNode("detailView");

        viewSql  = StringUtils.strip(viewElement.selectSingleNode("sql").getText());
        //System.out.println("["+viewSql+"]");
        iconName = viewElement.attributeValue("icon");

        List<?> captionItems = viewElement.selectNodes("captions/caption");
        if (captionItems.size() > 0)
        {
            int captionCount = captionItems.size();
            
            Vector<ERTICaptionInfo> list = new Vector<ERTICaptionInfo>();
            captionInfo    = new ERTICaptionInfo[captionCount];
            int i          = 0;
            for (Iterator capIter = captionItems.iterator(); capIter.hasNext(); )
            {
                Element captionElement = (Element)capIter.next();
                ERTICaptionInfo capInfo = new ERTICaptionInfo(captionElement);

                if (capInfo.isVisible())
                {
                    list.add(capInfo);
                    capInfo.setPosIndex(i);
                } else
                {
                    capInfo.setPosIndex(-1);
                }
                captionInfo[i] = capInfo;
                i++;
            }
            
            if (list.size() != captionCount)
            {
                // Create mappings of visible items
                visibleCaptionInfo = new ERTICaptionInfo[list.size()];
                i = 0;
                for (ERTICaptionInfo c : list)
                {
                    visibleCaptionInfo[i++] = c;
                }
            }
            
        } else
        {
            throw new RuntimeException("No Captions!");
        }
        Element rsElement  = (Element)viewElement.selectSingleNode("recordset");
        recordSetColumnInx = Integer.parseInt(rsElement.attributeValue("col"));

        
        List tables = tableElement.selectNodes("outofdate/table");
        for ( Iterator iter = tables.iterator(); iter.hasNext(); )
        {
            Element tblElement = (Element)iter.next();
            outOfDate.put(tblElement.attributeValue("name"), tblElement.attributeValue("title"));
        }

        Element indexElement = (Element)tableElement.selectSingleNode("index");

        Element sqlElement = (Element)indexElement.selectSingleNode("sql");
        patternSql = sqlElement != null ? sqlElement.getText() : "";
 
        StringBuilder strBuf    = new StringBuilder();
        List          colItems  = indexElement.selectNodes("cols/col");
    
        colInfo = new ERTIColInfo[colItems.size()];
        for (int i=0;i<colItems.size();i++)
        {
            ERTIColInfo columnInfo = new ERTIColInfo((Element)colItems.get(i));
            if (i > 0) strBuf.append(',');
            strBuf.append(columnInfo.getColName());
            colInfo[i] = columnInfo;
        }
        
        List joinColItems = indexElement.selectNodes("cols/join");
        joinCols = new ERTIJoinColInfo[joinColItems.size()];
        for (int i=0;i<joinColItems.size();i++)
        {
            ERTIJoinColInfo joinInfo = new ERTIJoinColInfo((Element)joinColItems.get(i));
            if (colItems.size() > 0) strBuf.append(',');
            strBuf.append(joinInfo.getColName());
            joinCols[i] = joinInfo;
        }
        
        buildSql = patternSql.replaceFirst("ColFieldsDef", strBuf.toString());
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
        if (outOfDate != null)
        {
            outOfDate.clear();
        }
        captionInfo = null;
        colInfo     = null;
        joinCols    = null;
        viewSql     = null;
        buildSql    = null;
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
    public ERTICaptionInfo[] getVisibleCaptionInfo()
    {
        return visibleCaptionInfo;
    }

    /**
     * Returns an array of ERTIColInfo Objects that describe the indexed Columns.
     * @return an array of the columns that are to be indexes
     */
    public ERTICaptionInfo[] getCaptionInfo()
    {
        return captionInfo;
    }

    /**
     * Returns an array of ERTIColInfo Objects that describe the indexed Columns.
     * @return an array of the columns that are to be indexes
     */
    public ERTIColInfo[] getColInfo()
    {
        return colInfo;
    }

    /**
     * Returns an array of ERTIJoinColInfo Objects that describe the indexed Join Columns.
     * @return an array of the columns that are to be indexes
     */
    public ERTIJoinColInfo[] getJoins()
    {
        return joinCols;
    }
    
    /**
     * Returns out of date hash.
     * @return out of date hash.
     */
    public Map<String, String> getOutOfDate()
    {
        return outOfDate;
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public boolean isExpressSearch()
    {
        return isExpressSearch;
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

    public String getBuildSql()
    {
        return buildSql;
    }

    public String getUpdateSql(final int tableIdArg)
    {
        StringBuilder strBuf    = new StringBuilder();
        
        boolean doingMainRecord = Integer.parseInt(tableId) == tableIdArg;
        int     i               = 0;
        String  idColName       = null;
        
        for (ERTIColInfo cInfo : colInfo)
        {
            if (i > 0) strBuf.append(',');
            if (doingMainRecord && cInfo.isIdColumn)
            {
                idColName = cInfo.getColName();
            }
            strBuf.append(cInfo.getColName());
            i++;
        }
        
        for (ERTIJoinColInfo joinInfo : joinCols)
        {
            if (colInfo.length > 0) strBuf.append(',');
            strBuf.append(joinInfo.getColName());
            if (joinInfo.getJoinTableIdAsInt() == tableIdArg)
            {
                idColName = joinInfo.getColName();
            }
        }
        
        String sql = patternSql.replaceFirst("ColFieldsDef", strBuf.toString());
        if (sql.toLowerCase().indexOf("where") > -1)
        {
            return  idColName != null ? sql + " AND " + idColName + " in (%s)" : null;
        }
        
        return  idColName != null ? sql + " where " + idColName + " in (%s)" : null;

    }

    public boolean isUseHitsCache()
    {
        return useHitsCache;
    }

    public int getVisColCount()
    {
        return visibleCaptionInfo.length;
    }

    public int getRecordSetColumnInx()
    {
        return recordSetColumnInx;
    }

    public Color getColor()
    {
        return color;
    }

    public int getPriority()
    {
        return priority;
    }
    
    public int getIdColIndex()
    {
        // this will usually return the position from the first array element
        for (ERTIColInfo ci : colInfo)
        {
            if (ci.isIdColumn())
            {
                return ci.getPosition();
            }
        }
        return -1;
    }
}

