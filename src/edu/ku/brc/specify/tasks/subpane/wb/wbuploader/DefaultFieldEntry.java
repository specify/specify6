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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.Determination;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Stores default values for (probably only required) fields not in uploading dataset.
 */
public class DefaultFieldEntry
{
    /**
     * The upload table that created this entry.
     */
    protected final UploadTable uploadTbl;
    
    /**
     * The uploadField for this entry. (may not exist)
     */
    protected final UploadField uploadFld;
    /**
     * The Java class of the field being uploaded to.
     */
    protected Class<?>          fldClass;
    /**
     * The method in tblClass that is used set values to the field being uploaded to.
     */
    protected Method            setter;
    /**
     * Default arg for setter member.
     */
    protected Vector<Object>          defaultValues = new Vector<Object>();
    /**
     * The name of the field being uploaded to.
     */
    protected String            fldName;

    /**
     * @param fldClass
     * @param setter
     * @param defaultValue
     * @param fldName
     */
    public DefaultFieldEntry(final UploadTable uploadTbl, Class<?> fldClass, Method setter,
            String fldName, final UploadField uploadFld)
    {
        super();
        this.uploadTbl = uploadTbl;
        this.fldClass = fldClass;
        this.setter = setter;
        this.fldName = fldName;
        this.uploadFld = uploadFld;
        this.defaultValues.add(null);
    }

    /**
     * @param params
     * @return the default value Object
     */
    protected Object getDefaultValue(Object... params)
    {
        //Cheapo fix for determination isCurrent
    	if (uploadTbl.getTblClass().equals(Determination.class) && fldName.equalsIgnoreCase("iscurrent"))
    	{
    		if (params != null && params.length == 1 && params[0] instanceof Integer)
    		{
    			Integer recNum = (Integer )params[0];
    			return recNum == 0;
    		}    			
    	}
    	
    	return defaultValues.get(0);
    }

    /**
     * @return
     */
    public boolean isMultiValued()
    {
    	return defaultValues.size() > 1;
    }
    
    /**
     * @param params
     * @return
     */
    protected List<Object> getDefaultValues(Object... params)
    {
    	return defaultValues;
    }
    
    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(Object defaultValue)
    {
        defaultValues.set(0, defaultValue);
    }

    /**
     * @param values a allowable values for matching.
     * the first value becomes the default value for new records.
     */
    public void setValues(Object[] values)
    {
    	this.defaultValues.clear();
    	if (values.length > 0)
    	{
    		for (int o = 0; o < values.length; o++)
    		{
    			this.defaultValues.add(values[o]);
    		}
    	}
    	else
    	{
    		this.defaultValues.add(null);
    	}
    }
    
    /**
     * @return the fldClass
     */
    public Class<?> getFldClass()
    {
        return fldClass;
    }

    /**
     * @return the fldName
     */
    public String getFldName()
    {
        return fldName;
    }

    /**
     * @return the setter
     */
    public Method getSetter()
    {
        return setter;
    }

    /**
     * @return true if a default value has been defined.
     */
    public boolean isDefined()
    {
        return defaultValues.get(0) != null;
    }

    /**
     * @return the uploadTbl
     */
    public UploadTable getUploadTbl()
    {
        return uploadTbl;
    }

    /**
     * @return the uploadFld
     */
    public UploadField getUploadFld()
    {
        return uploadFld;
    }
    
}
