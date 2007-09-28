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
package edu.ku.brc.specify.datamodel;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;

import edu.ku.brc.specify.tools.fielddesc.LocalizableStrIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 25, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "splocaleitemstr")
@org.hibernate.annotations.Table(appliesTo="splocaleitemstr", indexes =
    {   @Index (name="SpLocaleLanguageIDX", columnNames={"Language"}),
        @Index (name="SpLocaleCountyIDX", columnNames={"Country"})
    })
public class SpLocaleItemStr extends DataModelObjBase implements LocalizableStrIFace
{
    private static final Logger  log      = Logger.getLogger(SpLocaleItemStr.class);
            
    protected Integer spLocaleItemStrId;
    protected String  language;
    protected String  country;
    protected String  variant;
    protected String  text;
    
    protected SpLocaleContainer     containerDesc;
    protected SpLocaleContainer     containerName;
    protected SpLocaleContainerItem itemDesc;
    protected SpLocaleContainerItem itemName;
    
    /**
     * 
     */
    public SpLocaleItemStr()
    {
        // no op
    }

    public SpLocaleItemStr(final String text, final Locale locale)
    {
        initialize();
        this.text = text;
        language = locale.getLanguage();
        country  = locale.getCountry();
        variant  = locale.getVariant();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spLocaleItemStrId = null;
        language        = null;
        country         = null;
        variant         = null;
        text            = null;
    }

    /**
     * @return the localeItemStrId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpLocaleItemStrID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpLocaleItemStrId()
    {
        return spLocaleItemStrId;
    }

    /**
     * @param localeItemStrId the localeItemStrId to set
     */
    public void setSpLocaleItemStrId(Integer localeItemStrId)
    {
        this.spLocaleItemStrId = localeItemStrId;
    }
    
    /**
     * @param owner
     */
    /*public void setSpLocalizable(SpLocalizableIFace owner)
    {
        if (owner instanceof SpLocaleContainer)
        {
            setContainer((SpLocaleContainer)owner);
            
        } else if (owner instanceof SpLocaleContainerItem)
        {
            setItem((SpLocaleContainerItem)owner);
        } else
        {
            throw new RuntimeException("Can't set class of ["+owner+"]");
        }
    }*/

    /**
     * @return the text
     */
    @Column(name = "Text", unique = false, nullable = false, insertable = true, updatable = true, length = 255)
    public String getText()
    {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text)
    {
        if (text != null && text.length() > 255)
        {
            log.error("String len: "+text.length()+ " is > 255 ["+text+"]");
            this.text = text.substring(0, 255);
            
        } else
        {
            this.text = text;
        }
    }

    /**
     * @return the country
     */
    @Column(name = "Country", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
    public String getCountry()
    {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country)
    {
        this.country = country;
    }

    /**
     * @return the language
     */
    @Column(name = "Language", unique = false, nullable = false, insertable = true, updatable = true, length = 2)
    public String getLanguage()
    {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * @return the variant
     */
    @Column(name = "Variant", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
    public String getVariant()
    {
        return variant;
    }

    /**
     * @param variant the variant to set
     */
    public void setVariant(String variant)
    {
        this.variant = variant;
    }
    
    /**
     * @return the container
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpLocaleContainerDescID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpLocaleContainer getContainerDesc()
    {
        return containerDesc;
    }

    /**
     * @return the container
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpLocaleContainerNameID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpLocaleContainer getContainerName()
    {
        return containerName;
    }

    /**
     * @param container the container to set
     */
    public void setContainerDesc(SpLocaleContainer containerDesc)
    {
        this.containerDesc = containerDesc;
    }

    /**
     * @param container the container to set
     */
    public void setContainerName(SpLocaleContainer containerName)
    {
        this.containerName = containerName;
    }

    /**
     * @return the item
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpLocaleContainerItemDescID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpLocaleContainerItem getItemDesc()
    {
        return itemDesc;
    }

    /**
     * @param item the item to set
     */
    public void setItemDesc(SpLocaleContainerItem itemDesc)
    {
        this.itemDesc = itemDesc;
    }

    /**
     * @return the item
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpLocaleContainerItemNameID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpLocaleContainerItem getItemName()
    {
        return itemName;
    }

    /**
     * @param item the item to set
     */
    public void setItemName(SpLocaleContainerItem itemName)
    {
        this.itemName = itemName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpLocaleItemStr.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spLocaleItemStrId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 505;
    }
    
    @Transient
    public boolean isLocale(final Locale locale)
    {
        return language.equals(locale.getLanguage()) &&
               (country == null || country.equals(locale.getCountry())) && 
               (variant == null || variant.equals(locale.getVariant()));
    }
}
