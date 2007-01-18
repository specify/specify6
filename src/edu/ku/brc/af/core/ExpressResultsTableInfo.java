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
    public enum LOAD_TYPE {Building, Viewing, Both}

    protected LOAD_TYPE                 loadType;

    protected String                    id;
    protected String                    tableId;
    protected String                    title;
    protected String                    name;
    protected boolean                   isExpressSearch = true;
    protected boolean                   isIndexed       = true;

    // These are used for viewing the results
    protected String                    iconName      = null;
    protected String                    viewSql;
    protected boolean                   viewSQLOverridden = false;
    protected ColInfo[]                 cols          = null;
    protected JoinColInfo[]             joinCols      = null;

    // These data members are use for indexing
    protected boolean                   useHitsCache  = false;
    protected String                    patternSql;
    protected String                    buildSql;
    protected String                    updateSql;
    protected String[]                  colNames      = null;
    protected String[]                  colLabels     = null;
    protected boolean[]                 visCols       = null;
    protected Hashtable<String, String> outOfDate     = new Hashtable<String, String>();

    //protected int                       tableType;
    protected int                       recordSetColumnInx;
    protected int                       priority;
    protected Color                     color;

    // Derived Data member
    protected int                       visColCount = 0;

    /**
     * Constructs a table info object
     * @param tableElement the DOM4J element representing the information
     * @param loadType what type of info to load from the DOM
     * @param isExpressSearch true/false
     */
    public ExpressResultsTableInfo(final Element   tableElement, 
                                   final LOAD_TYPE loadType,
                                   final boolean   isExpressSearch)
    {
        this.loadType        = loadType;
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
        
       // This info is used for indexing
        if (loadType == LOAD_TYPE.Building || loadType == LOAD_TYPE.Both)
        {
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
            
            cols = new ColInfo[colItems.size()];
            for (int i=0;i<colItems.size();i++)
            {
                ColInfo colInfo = new ColInfo((Element)colItems.get(i));
                if (i > 0) strBuf.append(',');
                strBuf.append(colInfo.getColName());
                cols[i] = colInfo;
            }
            
            List joinColItems = indexElement.selectNodes("cols/join");
            joinCols = new JoinColInfo[joinColItems.size()];
            for (int i=0;i<joinColItems.size();i++)
            {
                JoinColInfo joinInfo = new JoinColInfo((Element)joinColItems.get(i));
                if (colItems.size() > 0) strBuf.append(',');
                strBuf.append(joinInfo.getColName());
                joinCols[i] = joinInfo;
            }
            
            buildSql  = patternSql.replaceFirst("ColFieldsDef", strBuf.toString());
           
            //System.err.println("["+buildSql+"]");
        }

        if (loadType == LOAD_TYPE.Viewing || loadType == LOAD_TYPE.Both)
        {
            Element viewElement  = (Element)tableElement.selectSingleNode("detailView");

            viewSql  = StringUtils.strip(viewElement.selectSingleNode("sql").getText());
            //System.out.println("["+viewSql+"]");
            iconName = viewElement.attributeValue("icon");

            List captionItems = viewElement.selectNodes("captions/caption");
            if (captionItems.size() > 0)
            {
                colNames  = new String[captionItems.size()];
                colLabels = new String[captionItems.size()];
                visCols   = new boolean[captionItems.size()];
                int i = 0;
                for ( Iterator capIter = captionItems.iterator(); capIter.hasNext(); )
                {
                    Element captionElement = (Element)capIter.next();
                    colLabels[i] = captionElement.attributeValue("text");
                    colNames[i]  = captionElement.attributeValue("col");
                    String vc = captionElement.attributeValue("visible");
                    visCols[i] = vc == null || vc.length() == 0 || !vc.toLowerCase().equals("false");
                    visColCount += visCols[i] ? 1 : 0;
                    i++;
                }
            } else
            {
                //log.info("No Captions!");
            }
            Element rsElement  = (Element)viewElement.selectSingleNode("recordset");
            //tableType = Integer.parseInt(rsElement.attributeValue("tabletype"));
            recordSetColumnInx = Integer.parseInt(rsElement.attributeValue("col"));

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
         if (outOfDate != null) outOfDate.clear();
        colNames = null;
        cols     = null;
        viewSql  = null;
        buildSql = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    public void finalize()
    {
        cleanUp();
    }
    
    /**
     * Helper to map an array to the visiable columns of the array.
     * @param array the array to be mapped
     * @return a new array that is mapped or the same array
     */
    protected String[] getMappedArray(final String[] array)
    {
        String[] mappedColNames = null;
        if (visColCount < array.length)
        {
            mappedColNames = new String[visColCount];
            int j = 0;
            for (int i=0;i<visCols.length;i++)
            {
                if (visCols[i])
                {
                    mappedColNames[j++] = array[i];
                }
            }
            return mappedColNames;
        }

        return array;  
    }

    /**
     * Returns an array with the column name mappined, return null if all columns are to be shown.
     * @return Returns an array with the column name mappined, return null if all columns are to be shown
     */
    public String[] getColNames()
    {
        return getMappedArray(colNames);
    }

    /**
     * Returns an array with the column name mappined, return null if all columns are to be shown.
     * @return Returns an array with the column name mappined, return null if all columns are to be shown
     */
    public String[] getColLabels()
    {
        return getMappedArray(colLabels);
    }

    /**
     * Returns an array with the column mappings, return null if all columns are to be shown.
     * @return Returns an array with the column mappings, return null if all columns are to be shown
     */
    public int[] getDisplayColIndexes()
    {
        int[] colsArray = null;
        if (visCols != null && visColCount < visCols.length)
        {
            colsArray = new int[visColCount];
            int j = 0;
            for (int i=0;i<visCols.length;i++)
            {
                if (visCols[i])
                {
                    colsArray[j++] = i;
                }
            }
        }
        return colsArray;
    }

    /**
     * Returns an array of ColInfo Objects that describe the indexed Columns.
     * @return an array of the columns that are to be indexes
     */
    public ColInfo[] getCols()
    {
        return cols;
    }

    /**
     * Returns an array of JoinColInfo Objects that describe the indexed Join Columns.
     * @return an array of the columns that are to be indexes
     */
    public JoinColInfo[] getJoins()
    {
        return joinCols;
    }

    /**
     * Returns the current LoadType: this object's internal contents were parsed for indexing or search processing.
     * @return the current LoadType
     */
    public LOAD_TYPE getLoadType()
    {
        return loadType;
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
        
        for (ColInfo colInfo : cols)
        {
            if (i > 0) strBuf.append(',');
            if (doingMainRecord && colInfo.isIdColumn)
            {
                idColName = colInfo.getColName();
            }
            strBuf.append(colInfo.getColName());
            i++;
        }
        
        for (JoinColInfo joinInfo : joinCols)
        {
            if (cols.length > 0) strBuf.append(',');
            strBuf.append(joinInfo.getColName());
            if (joinInfo.getJoinTableIdAsInt() == tableIdArg)
            {
                idColName = joinInfo.getColName();
            }
        }
        
        String sql = patternSql.replaceFirst("ColFieldsDef", strBuf.toString());
        if (sql.toLowerCase().indexOf("where") > -1)
        {
            return  idColName != null ? sql + " AND " + idColName + " = %d" : null;
        }
        
        return  idColName != null ? sql + " where " + idColName + " = %d" : null;

    }

    public boolean isUseHitsCache()
    {
        return useHitsCache;
    }

    public int getVisColCount()
    {
        return visColCount;
    }

    public boolean[] getVisCols()
    {
        return visCols;
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
        for (ColInfo ci : cols)
        {
            if (ci.isIdColumn())
            {
                return ci.getPosition();
            }
        }
        return -1;
    }
    
    //-------------------------------------------------------------
    //-- Inner Classes
    //-------------------------------------------------------------

    public class ColInfo 
    {
        protected int     position;
        protected boolean isIdColumn;
        protected String  colName;
        protected String  secondaryKey;
        protected int     idIndex = -1;
        
        public ColInfo(final Element element)
        {
            position     = getAttr(element, "pos", -1);
            isIdColumn   = getAttr(element, "id", false);
            colName      = element.getTextTrim();
            secondaryKey = getAttr(element, "key", null);
            if (isIdColumn)
            {
                idIndex = position;
            }
        }

        public String getColName()
        {
            return colName;
        }

        public boolean isIdColumn()
        {
            return isIdColumn;
        }

        public int getPosition()
        {
            return position;
        }

        public String getSecondaryKey()
        {
            return secondaryKey;
        }
        
    }
    
    public class JoinColInfo 
    {
        protected int    position;
        protected String joinTableId;
        protected int    joinTableIdAsInt;
        protected String colName;
        
        public JoinColInfo(final Element element)
        {
            position         = getAttr(element, "pos", -1);
            joinTableId      =  getAttr(element, "tableid", null);
            joinTableIdAsInt =  getAttr(element, "tableid", -1);
            colName         = element.getTextTrim();
        }

        public int getPosition()
        {
            return position;
        }
        
        public String getColName()
        {
            return colName;
        }

        public String getJoinTableId()
        {
            return joinTableId;
        }

        public int getJoinTableIdAsInt()
        {
            return joinTableIdAsInt;
        }
   
    }
}

