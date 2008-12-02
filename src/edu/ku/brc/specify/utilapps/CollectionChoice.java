/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.utilapps;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.specify.config.DisciplineType;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 11, 2008
 *
 */
public class CollectionChoice
{
    protected DisciplineType.STD_DISCIPLINES  type;
    protected String  name;
    protected boolean isTissue;
    protected boolean isSelected;
    
    protected String  catalogNumberingFmtName;
    protected String  accessionNumberingFmtName;
    protected String  catNumScheme;
    protected String  accNumScheme;
    
    // Transient 
    protected UIFieldFormatterIFace catNumberFormatter = null;
    protected UIFieldFormatterIFace accNumberFormatter = null;
    
    /**
     * 
     */
    public CollectionChoice()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * @param type
     * @param isTissue
     * @param isSelected
     */
    public CollectionChoice(final DisciplineType.STD_DISCIPLINES type, 
                            final boolean isTissue, 
                            final boolean isSelected)
    {
        super();
        this.type       = type;
        this.isTissue   = isTissue;
        this.isSelected = isSelected;
        this.name       = type.toString();
        
    }
    
    /**
     * 
     */
    public void initialize()
    {
        type = DisciplineType.STD_DISCIPLINES.valueOf(name);
    }
    
    /**
     * @return the type
     */
    public DisciplineType.STD_DISCIPLINES getType()
    {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(DisciplineType.STD_DISCIPLINES type)
    {
        this.type = type;
        this.name = type.toString();
    }
    
    /**
     * @return the isTissue
     */
    public boolean isTissue()
    {
        return isTissue;
    }
    /**
     * @param isTissue the isTissue to set
     */
    public void setTissue(boolean isTissue)
    {
        this.isTissue = isTissue;
    }
    /**
     * @return the isSelected
     */
    public boolean isSelected()
    {
        return isSelected;
    }
    /**
     * @param isSelected the isSelected to set
     */
    public void setSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the catalogNumberingScheme
     */
    public String getCatalogNumberingFmtName()
    {
        return catalogNumberingFmtName;
    }

    /**
     * @param catalogNumberingScheme the catalogNumberingScheme to set
     */
    public void setCatalogNumberingFmtName(String catalogNumberingFmtName)
    {
        this.catalogNumberingFmtName = catalogNumberingFmtName;
        this.catNumberFormatter = null;
    }

    /**
     * @return the accessionNumberingScheme
     */
    public String getAccessionNumberingFmtName()
    {
        return accessionNumberingFmtName;
    }

    /**
     * @param accessionNumberingScheme the accessionNumberingScheme to set
     */
    public void setAccessionNumberingFmtName(String accessionNumberingFmtName)
    {
        this.accessionNumberingFmtName = accessionNumberingFmtName;
        this.accNumberFormatter = null;
    }
    
    /**
     * @return the catNumGroup
     */
    public String getCatNumGroup()
    {
        return catNumScheme;
    }

    /**
     * @param catNumGroup the catNumGroup to set
     */
    public void setCatNumGroup(String catNumGroup)
    {
        this.catNumScheme = catNumGroup;
    }

    /**
     * @return the accNumGroup
     */
    public String getAccNumGroup()
    {
        return accNumScheme;
    }

    /**
     * @param accNumGroup the accNumGroup to set
     */
    public void setAccNumGroup(String accNumGroup)
    {
        this.accNumScheme = accNumGroup;
    }

    /**
     * @return the catNumberFormatter
     */
    public UIFieldFormatterIFace getCatNumberFormatter()
    {
        if (catNumberFormatter == null)
        {
            catNumberFormatter = UIFieldFormatterMgr.getInstance().getFormatter(catalogNumberingFmtName);
        }
        return catNumberFormatter;
    }

    /**
     * @param catNumberFormatter the catNumberFormatter to set
     */
    public void setCatNumberFormatter(UIFieldFormatterIFace catNumberFormatter)
    {
        this.catNumberFormatter = catNumberFormatter;
    }

    /**
     * @return the accNumberFormatter
     */
    public UIFieldFormatterIFace getAccNumberFormatter()
    {
        if (accNumberFormatter == null)
        {
            accNumberFormatter = UIFieldFormatterMgr.getInstance().getFormatter(accessionNumberingFmtName);
        }
        return accNumberFormatter;
    }

    /**
     * @param accNumberFormatter the accNumberFormatter to set
     */
    public void setAccNumberFormatter(UIFieldFormatterIFace accNumberFormatter)
    {
        this.accNumberFormatter = accNumberFormatter;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        DisciplineType dType = DisciplineType.getDiscipline(type);
        return dType.getTitle() + (isTissue ? " Tissue" : "");
    }
    

    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("choice", CollectionChoice.class);
        xstream.useAttributeFor(CollectionChoice.class, "name");
        xstream.useAttributeFor(CollectionChoice.class, "isTissue");
        xstream.useAttributeFor(CollectionChoice.class, "isSelected");
        xstream.useAttributeFor(CollectionChoice.class, "accessionNumberingFmtName");
        xstream.useAttributeFor(CollectionChoice.class, "catalogNumberingFmtName");
        xstream.useAttributeFor(CollectionChoice.class, "catNumScheme");
        xstream.useAttributeFor(CollectionChoice.class, "accNumScheme");

        xstream.omitField(CollectionChoice.class, "type");
        xstream.omitField(CollectionChoice.class, "catNumberFormatter");
        xstream.omitField(CollectionChoice.class, "accNumberFormatter");
        
    }
    

}