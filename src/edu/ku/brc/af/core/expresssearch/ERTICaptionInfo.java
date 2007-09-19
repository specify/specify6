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
package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import org.dom4j.Element;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 10, 2007
 *
 */
public class ERTICaptionInfo
{
    protected String                  colName;
    protected String                  colLabel;
    protected boolean                 isVisible;
    protected String                  formatter;
    protected int                     posIndex;
    
    protected Class                   colClass = null;
    
    public ERTICaptionInfo(final Element element)
    {
        super();
        
        this.colName   = element.attributeValue("col");
        this.colLabel  = element.attributeValue("text");
        this.isVisible = getAttr(element, "visible", true);
        this.formatter = getAttr(element, "formatter", null);
    }

    public ERTICaptionInfo(String  colName, 
                           String  colLabel, 
                           boolean isVisible, 
                           String  formatter,
                           int     posIndex)
    {
        super();
        this.colName = colName;
        this.colLabel = colLabel;
        this.isVisible = isVisible;
        this.formatter = formatter;
        this.posIndex = posIndex;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SearchResultsCaptionIFace#getColName()
     */
    public String getColName()
    {
        return colName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SearchResultsCaptionIFace#getColLabel()
     */
    public String getColLabel()
    {
        return colLabel;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SearchResultsCaptionIFace#isVisible()
     */
    public boolean isVisible()
    {
        return isVisible;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SearchResultsCaptionIFace#getFormatter()
     */
    public String getFormatter()
    {
        return formatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SearchResultsCaptionIFace#getPosIndex()
     */
    public int getPosIndex()
    {
        return posIndex;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SearchResultsCaptionIFace#setPosIndex(int)
     */
    public void setPosIndex(int posIndex)
    {
        this.posIndex = posIndex;
    }

    /**
     * @return the colClass
     */
    public Class<?> getColClass()
    {
        return colClass;
    }

    /**
     * @param colClass the colClass to set
     */
    public void setColClass(Class<?> colClass)
    {
        this.colClass = colClass;
    }

    /**
     * @param colName the colName to set
     */
    public void setColName(String colName)
    {
        this.colName = colName;
    }

    /**
     * @param colLabel the colLabel to set
     */
    public void setColLabel(String colLabel)
    {
        this.colLabel = colLabel;
    }

    /**
     * @param isVisible the isVisible to set
     */
    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    /**
     * @param formatter the formatter to set
     */
    public void setFormatter(String formatter)
    {
        this.formatter = formatter;
    }
}
