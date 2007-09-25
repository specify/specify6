/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 4, 2007
 *
 */
public class Table implements LocalizableNameDescIFace, Comparable<Table>, Cloneable
{
    protected String      name;
    protected List<Name>  names = new Vector<Name>();
    protected List<Desc>  descs = new Vector<Desc>();
    
    protected List<Field>        fields        = new Vector<Field>();
    protected List<Relationship> relationships = new Vector<Relationship>();

    public Table(final String name)
    {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the fields
     */
    public List<Field> getFields()
    {
        return fields;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(List<Field> fields)
    {
        this.fields = fields;
    }
    
    public String toString()
    {
        return name;
    }

    /**
     * @return the descs
     */
    public List<Desc> getDescs()
    {
        return descs;
    }

    /**
     * @param descs the descs to set
     */
    public void setDescs(List<Desc> descs)
    {
        this.descs = descs;
    }

    /**
     * @return the names
     */
    public List<Name> getNames()
    {
        return names;
    }

    /**
     * @param names the names to set
     */
    public void setNames(List<Name> names)
    {
        this.names = names;
    }

    /**
     * @return the relationships
     */
    public List<Relationship> getRelationships()
    {
        return relationships;
    }

    /**
     * @param relationships the relationships to set
     */
    public void setRelationships(List<Relationship> relationships)
    {
        this.relationships = relationships;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Table o)
    {
        return name.compareTo(o.name);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableNameDescIFace#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale)
    {
        for (Name nm : names)
        {
            nm.setLang(locale.getLanguage());
            nm.setCountry(locale.getCountry());
            nm.setVariant(locale.getVariant());
        }

        for (Desc d : descs)
        {
            d.setLang(locale.getLanguage());
            d.setCountry(locale.getCountry());
            d.setVariant(locale.getVariant());
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableNameDescIFace#setLocale(java.util.Locale)
     */
    public void copyLocale(final Locale srcLocale, final Locale dstLocale)
    {
        Name srcName = null;
        for (Name nm : names)
        {
            if (nm.isLocale(srcLocale))
            {
                srcName = nm;
                break;
            }
        }
        
        if (srcName != null)
        {
            Name name = new Name(srcName.getText(), dstLocale);
            names.add(name);
        }

        Desc srcDesc = null;
        for (Desc d : descs)
        {
            if (d.isLocale(srcLocale))
            {
                srcDesc = d;
                break;
            }
        }
        
        if (srcDesc != null)
        {
            Desc desc = new Desc(srcDesc.getText(), dstLocale);
            descs.add(desc);
        }       
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Table table = (Table)super.clone();
        table.name = name;
        
        table.fields = new Vector<Field>();
        table.names  = new Vector<Name>();
        table.descs  = new Vector<Desc>();
        
        for (Field f : fields)
        {
            table.fields.add((Field)f.clone());
        }

        for (Relationship r : relationships)
        {
            table.relationships.add((Relationship)r.clone());
        }

        for (Name nm : names)
        {
            table.names.add((Name)nm.clone());
        }

        for (Desc d : descs)
        {
            table.descs.add((Desc)d.clone());
        }
        return table;
    }
    
}
