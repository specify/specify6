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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Calendar;
import java.util.Date;



/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ImportColumnInfo  implements Comparable<ImportColumnInfo>
{
    public enum ColumnType {Integer, Float, Double, Short, Boolean, Date, String}
    
    protected final Integer      colInx;
    protected final ColumnType disciplinee;
    protected final String     colName; // the name of the column (this may include the table name in parens)
    protected final String     colTitle; // name without parens
    protected String     data;
    protected String     mapToTbl = null; //mapping read from XLS document properties
    protected String     mapToFld = null; //mappint read from XLS document properties
    protected int		 formXCoord = -1;
    protected int 		 formYCoord = -1;
    protected int		 frmFieldType = 0;
    protected String     caption = null;
    protected String	 frmMetaData = null;
    
    protected final boolean    isSystemCol;
    
    public ImportColumnInfo(final int      colInx, 
                            final ColumnType disciplinee, 
                            final String     colName,
                            final String     colTitle,
                            final String     mapToTbl,
                            final String     mapToFld,
                            final String     data)
    {
        super();
        
        this.colInx  = colInx;
        this.disciplinee = disciplinee;
        this.colName = colName;
        this.isSystemCol = this.colName.equals(DataImport.GEO_DATA_HEADING) || this.colName.equals(DataImport.IMAGE_PATH_HEADING);
        this.colTitle = colTitle;
        this.caption = colTitle;
        this.mapToTbl = mapToTbl;
        this.mapToFld = mapToFld;
        this.data    = data;
    }

    public boolean getIsSystemCol()
    {
        return isSystemCol;
    }
    
    public String getColName()
    {
        return colName;
    }

    public ColumnType getColType()
    {
        return disciplinee;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public Integer getColInx()
    {
        return colInx;
    }

    /**
     * @return the colTitle
     */
    public String getColTitle()
    {
        return colTitle;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ImportColumnInfo obj)
    {
        return colInx.compareTo(obj.colInx);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return colName;
    }
    
    /**
     * Converts data type string into an enum.
     * @param type the string with the database type
     * @return the enum
     */
    public static ColumnType getType(final Class<?> type)
    {
        if (type != null)
        {
            if (type.equals(Integer.class))
            {
                return ImportColumnInfo.ColumnType.Integer;
                
            } else if (type.equals(Float.class))
            {
                return ImportColumnInfo.ColumnType.Float;
                
            } else if (type.equals(Double.class))
            {
                return ImportColumnInfo.ColumnType.Double;
                
            } else if (type.equals(Short.class))
            {
                return ImportColumnInfo.ColumnType.Short;
                
            } else if (type.equals(Calendar.class) || type.equals(Date.class))
            {
                return ImportColumnInfo.ColumnType.Date;
                
            } else if (type.equals(java.math.BigDecimal.class))
            {
                return ImportColumnInfo.ColumnType.Double;
            }
        }
        return ImportColumnInfo.ColumnType.String;
    }

    /**
     * @return the mapToTbl
     */
    public String getMapToTbl()
    {
        return mapToTbl;
    }

    /**
     * @return the mapToFld
     */
    public String getMapToFld()
    {
        return mapToFld;
    }

    /**
     * @param mapToTbl the mapToTbl to set
     */
    public void setMapToTbl(String mapToTbl)
    {
        this.mapToTbl = mapToTbl;
    }

    /**
     * @param mapToFld the mapToFld to set
     */
    public void setMapToFld(String mapToFld)
    {
        this.mapToFld = mapToFld;
    }

	/**
	 * @return the formXCoord
	 */
	public int getFormXCoord() 
	{
		return formXCoord;
	}

	/**
	 * @param formXCoord the formXCoord to set
	 */
	public void setFormXCoord(int formXCoord) 
	{
		this.formXCoord = formXCoord;
	}

	/**
	 * @return the formYCoord
	 */
	public int getFormYCoord() 
	{
		return formYCoord;
	}

	/**
	 * @param formYCoord the formYCoord to set
	 */
	public void setFormYCoord(int formYCoord) 
	{
		this.formYCoord = formYCoord;
	}

	/**
	 * @return the frmFieldType
	 */
	public int getFrmFieldType() 
	{
		return frmFieldType;
	}

	/**
	 * @param frmFieldType the frmFieldType to set
	 */
	public void setFrmFieldType(int frmFieldType) 
	{
		this.frmFieldType = frmFieldType;
	}

	/**
	 * @return the caption
	 */
	public String getCaption() 
	{
		return caption;
	}

	/**
	 * @param caption the caption to set
	 */
	public void setCaption(String caption) 
	{
		this.caption = caption;
	}

	/**
	 * @return the frmMetaData
	 */
	public String getFrmMetaData() 
	{
		return frmMetaData;
	}

	/**
	 * @param frmMetaData the frmMetaData to set
	 */
	public void setFrmMetaData(String frmMetaData) 
	{
		this.frmMetaData = frmMetaData;
	}
    
    
}
