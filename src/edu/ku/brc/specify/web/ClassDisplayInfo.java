/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.web;

import java.util.Hashtable;
import java.util.Vector;

public class ClassDisplayInfo  implements Comparable<ClassDisplayInfo>
{
    protected static String packageName;
    
    protected String   className;
    protected Class<?> classObj;
    protected String   indexName;
    protected Class<?> indexClass;
    protected boolean  useIdentityTitle;
    protected String   linkField;
    
    protected TitleGetterIFace titleGetter = null;
    protected String           titleField  = null;
    
    // Fields are in Order
    protected Vector<FieldDisplayInfo>            fields    = new Vector<FieldDisplayInfo>();
    protected Vector<FieldDisplayInfo>            order     = new Vector<FieldDisplayInfo>();
    protected Hashtable<String, FieldDisplayInfo> fieldHash = new Hashtable<String, FieldDisplayInfo>();
    
    protected Vector<AdditionalDisplayField>            additional = new Vector<AdditionalDisplayField>();
    protected Hashtable<String, AdditionalDisplayField> additionalHash = new Hashtable<String, AdditionalDisplayField>();
    
    protected Vector<StatsDisplayInfo>                 stats = new Vector<StatsDisplayInfo>();
    

    public ClassDisplayInfo(final String   className, 
                            final Class<?> classObj, 
                            final String   indexName,
                            final Class<?> indexClass,
                            final String   linkField,
                            final boolean  useIdentityTitle)
    {
        super();
        this.className = className;
        this.classObj = classObj;
        this.indexName = indexName;
        this.indexClass = indexClass;
        this.linkField = linkField;
        this.useIdentityTitle = useIdentityTitle;
    }
    
    /**
     * @return the titleField
     */
    public String getTitleField()
    {
        return titleField;
    }

    /**
     * @param titleField the titleField to set
     */
    public void setTitleField(String titleField)
    {
        this.titleField = titleField;
    }

    /**
     * @return the titlGetter
     */
    public TitleGetterIFace getTitleGetter()
    {
        return titleGetter;
    }

    /**
     * @param titlGetter the titlGetter to set
     */
    public void setTitleGetter(TitleGetterIFace titleGetter)
    {
        this.titleGetter = titleGetter;
    }

    public void addOrdered(final FieldDisplayInfo field)
    {
        order.add(field);
        fields.add(field);
        fieldHash.put(field.getName(), field);
    }

    public void addSkipped(final FieldDisplayInfo field)
    {
        field.setSkipped(true);
        
        fields.add(field);
        fieldHash.put(field.getName(), field);
    }
    
    public void addAdditional(final AdditionalDisplayField field)
    {
        additional.add(field);
        additionalHash.put(field.getFieldName(), field);
    }
    
    public void addStat(final StatsDisplayInfo field)
    {
        stats.add(field);
    }
    
    public FieldDisplayInfo getField(final String fName)
    {
        return fieldHash.get(fName);
    }
    
    public boolean isFieldSkipped(final String name)
    {
        FieldDisplayInfo field = fieldHash.get(name);
        if (field != null)
        {
            return field.isSkipped();
        }
        return false;
    }
    
    /**
     * @return the fields
     */
    public Vector<FieldDisplayInfo> getFields()
    {
        return fields;
    }

    /**
     * @return the hash
     */
    public Hashtable<String, FieldDisplayInfo> getHash()
    {
        return fieldHash;
    }

    /**
     * @return the className
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * @return the fullName
     */
    public String getFullName()
    {
        return classObj.getName();
    }

    /**
     * @return the classObj
     */
    public Class<?> getClassObj()
    {
        return classObj;
    }

    /**
     * @return the indexName
     */
    public String getIndexName()
    {
        return indexName;
    }

    /**
     * @return the indexClass
     */
    public Class<?> getIndexClass()
    {
        return indexClass;
    }

    /**
     * @return the linkField
     */
    public String getLinkField()
    {
        return linkField;
    }

    /**
     * @return the useIdentityTitle
     */
    public boolean isUseIdentityTitle()
    {
        return useIdentityTitle;
    }

    /**
     * @return the order
     */
    public Vector<FieldDisplayInfo> getOrder()
    {
        return order;
    }

    /**
     * @return the stats
     */
    public Vector<StatsDisplayInfo> getStats()
    {
        return stats;
    }

    /**
     * @return the additional
     */
    public Vector<AdditionalDisplayField> getAdditional()
    {
        return additional;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ClassDisplayInfo o)
    {
        return className.compareTo(o.className);
    }

    /**
     * @return the packageName
     */
    public static String getPackageName()
    {
        return packageName;
    }

    /**
     * @param packageName the packageName to set
     */
    public static void setPackageName(String packageName)
    {
        ClassDisplayInfo.packageName = packageName;
    }
    
}
