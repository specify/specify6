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
package edu.ku.brc.specify.utilapps.sp5utils;

import java.awt.Rectangle;

import com.thoughtworks.xstream.XStream;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 8, 2009
 *
 */
public class FormFieldInfo
{
    private static int segWidth = 10;
    
    private String sp5FieldName;
    private String sp6FieldName;
    private String caption;
    private String controlType;
    private String dataType;
    private String relatedTableName;
    private Integer top;
    private Integer left;
    private Integer width;
    private Integer height;
    
    private Integer controlTypeNum;
    private Integer dataTypeNum;
    
    private FormInfo parent;
    
    
    /**
     * @param sp5FieldName
     * @param sp6FieldName
     * @param caption
     * @param controlType
     * @param datatype
     * @param relatedTableName
     * @param top
     * @param left
     * @param width
     * @param height
     */
    public FormFieldInfo(String sp5FieldName, 
                           String sp6FieldName, 
                           String caption,
                           String controlType, 
                           String dataType, 
                           String relatedTableName, 
                           Integer top, 
                           Integer left,
                           Integer width, 
                           Integer height, 
                           Integer controlTypeNum, 
                           Integer dataTypeNum)
    {
        super();
        this.sp5FieldName = sp5FieldName;
        this.sp6FieldName = sp6FieldName;
        this.caption = caption;
        this.controlType = controlType;
        this.dataType = dataType;
        this.relatedTableName = relatedTableName;
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
        this.controlTypeNum = controlTypeNum;
        this.dataTypeNum = dataTypeNum;
    }


    /**
     * @param top the top to set
     */
    public void setTop(Integer top)
    {
        this.top = top;
    }


    /**
     * @param left the left to set
     */
    public void setLeft(Integer left)
    {
        this.left = left;
    }


    /**
     * @param width the width to set
     */
    public void setWidth(Integer width)
    {
        this.width = width;
    }


    /**
     * @param height the height to set
     */
    public void setHeight(Integer height)
    {
        this.height = height;
    }


    public int getCellX()
    {
        return left / segWidth;
    }
    
    public int getCellY()
    {
        return top / segWidth;
    }
    
    public int getCellWidth()
    {
        return width / segWidth;
    }
    
    public int getCellHeight()
    {
        return height / segWidth;
    }
    
    public Rectangle getBoundsFromCellDim()
    {
        Rectangle r = new Rectangle(getCellX()*segWidth, getCellY()*segWidth, getCellWidth()*segWidth, getCellHeight()*segWidth);
        return r;
    }
    
    /**
     * @param sp6FieldName the sp6FieldName to set
     */
    public void setSp6FieldName(String sp6FieldName)
    {
        this.sp6FieldName = sp6FieldName;
    }


    /**
     * @param parent the parent to set
     */
    public void setParent(FormInfo parent)
    {
        this.parent = parent;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return sp5FieldName;
    }


    /**
     * @return the sp5FieldName
     */
    public String getSp5FieldName()
    {
        return sp5FieldName;
    }


    /**
     * @return the sp6FieldName
     */
    public String getSp6FieldName()
    {
        return sp6FieldName;
    }


    /**
     * @return the caption
     */
    public String getCaption()
    {
        return caption;
    }


    /**
     * @return the controlType
     */
    public String getControlType()
    {
        return controlType;
    }


    /**
     * @return the dataType
     */
    public String getDataType()
    {
        return dataType;
    }


    /**
     * @return the relatedTableName
     */
    public String getRelatedTableName()
    {
        return relatedTableName;
    }


    /**
     * @return the top
     */
    public Integer getTop()
    {
        return top;
    }


    /**
     * @return the left
     */
    public Integer getLeft()
    {
        return left;
    }


    /**
     * @return the width
     */
    public Integer getWidth()
    {
        return width;
    }


    /**
     * @return the height
     */
    public Integer getHeight()
    {
        return height;
    }


    /**
     * @return the controlTypeNum
     */
    public Integer getControlTypeNum()
    {
        return controlTypeNum;
    }


    /**
     * @return the dataTypeNum
     */
    public Integer getDataTypeNum()
    {
        return dataTypeNum;
    }


    /**
     * @return the parent
     */
    public FormInfo getParent()
    {
        return parent;
    }
    

    /**
     * @return the segWidth
     */
    public static int getSegWidth()
    {
        return segWidth;
    }


    /**
     * @param segWidth the segWidth to set
     */
    public static void setSegWidth(int segWidth)
    {
        FormFieldInfo.segWidth = segWidth;
    }


    /**
     * Configures the XStream for I/O.
     * @param xstream the stream
     */
    public static void configXStream(final XStream xstream)
    {
        // Aliases
        xstream.alias("field",        FormFieldInfo.class); //$NON-NLS-1$

        xstream.aliasAttribute(FormFieldInfo.class, "sp5FieldName", "sp5FieldName"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "sp6FieldName", "sp6FieldName"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "caption", "caption"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "controlType", "controlType"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "dataType", "dataType"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "relatedTableName", "relatedTableName"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "top", "top"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "left", "left"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "width", "width"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "height", "height"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "controlTypeNum", "controlTypeNum"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(FormFieldInfo.class, "dataTypeNum", "dataTypeNum"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Things to omit
        xstream.omitField(FormFieldInfo.class,  "parent"); //$NON-NLS-1$
    }
}