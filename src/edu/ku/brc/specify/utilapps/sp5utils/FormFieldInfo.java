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
}