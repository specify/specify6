/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
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