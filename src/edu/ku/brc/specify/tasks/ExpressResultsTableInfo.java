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

package edu.ku.brc.specify.tasks;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.ui.UIHelper.getBoolean;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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
    public enum LOAD_TYPE {Building, Viewing, Both}

    protected LOAD_TYPE                 loadType;

    protected String                    tableId;
    protected String                    title;
    protected String                    name;
    protected boolean                   isExpressSearch = true;

    // These are used for viewing the results
    protected String                    iconName      = null;
    protected String                    viewSql;
    protected boolean                   viewSQLOverridden = false;
    protected Vector<Integer>           recIds        = new Vector<Integer>();
    protected ColInfo[]                 cols          = null;

    // These data members are use for indexing
    protected boolean                   useHitsCache = false;
    protected String                    buildSql;
    protected String[]                  colNames     = null;
    protected String[]                  colLabels    = null;
    protected boolean[]                 visCols      = null;
    protected Hashtable<String, String> outOfDate    = new Hashtable<String, String>();
    protected Vector<Integer>           indexes      = new Vector<Integer>();

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
     */
    public ExpressResultsTableInfo(final Element tableElement, final LOAD_TYPE loadType)
    {
        this.loadType = loadType;

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
        tableId         = tableElement.attributeValue("id");
        title           = tableElement.attributeValue("title");
        name            = tableElement.attributeValue("name");
        isExpressSearch = getAttr(tableElement, "expresssearch", true);
        priority        = Integer.parseInt(tableElement.attributeValue("priority"));
        color           = parseRGB(tableElement.attributeValue("color"));

        String uhcStr = tableElement.attributeValue("usehitscache");
        useHitsCache  = uhcStr == null || uhcStr.length() == 0 ? false : getBoolean(uhcStr);

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

            buildSql  = indexElement.selectSingleNode("sql").getText();

            StringBuilder strBuf = new StringBuilder();
            List colItems = indexElement.selectNodes("cols/col");
            cols = new ColInfo[colItems.size()];
            for (int i=0;i<colItems.size();i++)
            {
                cols[i] = new ColInfo((Element)colItems.get(i));
                if (i > 0) strBuf.append(',');
                strBuf.append(cols[i].getColName());
            }
            
            buildSql = buildSql.replaceFirst("ColFieldsDef", strBuf.toString());
            System.err.println("["+buildSql+"]");
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
     * Returns the number of indexes.
     * @return the number of indexes
     */
    public int getNumIndexes()
    {
        return indexes.size();
    }

    /**
     * Adds an index.
     * @param index the index to add
     */
    public void addIndex(int index)
    {
        indexes.add(index);
    }

    /**
     * Returns the array of indexes.
     * @return the array of indexes
     */
    public int[] getIndexes()
    {
        int[] inxs = new int[indexes.size()];
        int inx = 0;
        for (Integer i : indexes)
        {
            inxs[inx++] = i;
        }
        indexes.clear();
        return inxs;
    }

    /**
     * Cleanup any memory.
     */
    public void cleanUp()
    {
        if (recIds != null) recIds.clear();
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
     * Returns an array with the column name mappined, return null if all columns are to be shown.
     * @return Returns an array with the column name mappined, return null if all columns are to be shown
     */
    public String[] getColNames()
    {
        String[] mappedColNames = null;
        if (getVisColCount() < colNames.length)
        {
            mappedColNames = new String[visColCount];
            int j = 0;
            for (int i=0;i<visCols.length;i++)
            {
                if (visCols[i])
                {
                    mappedColNames[j++] = colNames[i];
                }
            }
            return mappedColNames;
        }

        return colNames;
    }

    /**
     * Returns an array with the column name mappined, return null if all columns are to be shown.
     * @return Returns an array with the column name mappined, return null if all columns are to be shown
     */
    public String[] getColLabels()
    {
        String[] mappedColLabels = null;
        if (getVisColCount() < colLabels.length)
        {
            mappedColLabels = new String[visColCount];
            int j = 0;
            for (int i=0;i<visCols.length;i++)
            {
                if (visCols[i])
                {
                    mappedColLabels[j++] = colNames[i];
                }
            }
            return mappedColLabels;
        }

        return colLabels;
    }

    /**
     * Returns an array with the column mappings, return null if all columns are to be shown.
     * @return Returns an array with the column mappings, return null if all columns are to be shown
     */
    public int[] getDisplayColIndexes()
    {
        int[] cols = null;
        if (visCols != null && getVisColCount() < visCols.length)
        {
            cols = new int[visColCount];
            int j = 0;
            for (int i=0;i<visCols.length;i++)
            {
                if (visCols[i])
                {
                    cols[j++] = i;
                }
            }
        }
        return cols;
    }

    /**
     * Returns an array of ColINfo Objects that describe the indexed Columns.
     * @return an array of the columns that are to be indexes
     */
    public ColInfo[] getCols()
    {
        return cols;
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
    public Hashtable<String, String> getOutOfDate()
    {
        return outOfDate;
    }


    public Vector<Integer> getRecIds()
    {
        return recIds;
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
        return viewSQLOverridden ? viewSql : viewSql.replace("%s", getRecIdList());
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

    public void setRecIds(Vector<Integer> recIds)
    {
        this.recIds = recIds;
    }

    public String getTableId()
    {
        return tableId;
    }

    public String getRecIdList()
    {
        StringBuffer idsStr = new StringBuffer(recIds.size()*8);
        for (int i=0;i<recIds.size();i++)
        {
            if (i > 0) idsStr.append(',');
            idsStr.append(recIds.elementAt(i).toString());
        }
        return idsStr.toString();
    }

    public String getIconName()
    {
        return iconName;
    }

    public String getBuildSql()
    {
        return buildSql;
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
}

