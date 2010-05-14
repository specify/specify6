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
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace;
import edu.ku.brc.specify.tools.schemalocale.LocalizableStrIFace;

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
@Table(name = "splocalecontaineritem")
@org.hibernate.annotations.Table(appliesTo="localecontaineritem", indexes =
    {   @Index (name="SpLocaleContainerItemNameIDX", columnNames={"Name"})
    })
public class SpLocaleContainerItem extends SpLocaleBase implements LocalizableItemIFace, Comparable<SpLocaleContainerItem>
{
    private static final Logger log = Logger.getLogger(SpLocaleContainerItem.class);
    
    protected Integer                 spLocaleContainerItemId;
    protected SpLocaleContainer       container;
    
    protected Set<SpLocaleItemStr>    names;
    protected Set<SpLocaleItemStr>    descs;
    protected Set<SpExportSchemaItem> spExportSchemaItems;
    protected String                  webLinkName;
    protected Boolean                 isRequired;
    
    /**
     * 
     */
    public SpLocaleContainerItem()
    {
        // no op
    }

    /**
     * @return the localeContainerItemId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpLocaleContainerItemID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpLocaleContainerItemId()
    {
        return spLocaleContainerItemId;
    }

    /**
     * @param localeContainerItemId the localeContainerItemId to set
     */
    public void setSpLocaleContainerItemId(Integer localeContainerItemId)
    {
        this.spLocaleContainerItemId = localeContainerItemId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.LocaleBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.initialize();
        spLocaleContainerItemId = null;
        
        names               = new HashSet<SpLocaleItemStr>();
        descs               = new HashSet<SpLocaleItemStr>();
        spExportSchemaItems = new HashSet<SpExportSchemaItem>();
        isRequired          = false;
        container           = null;

    }
    
    /**
     * @return the descs
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "itemDesc")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpLocaleItemStr> getDescs()
    {
        return descs;
    }

    /**
     * @param descs the descs to set
     */
    public void setDescs(Set<SpLocaleItemStr> descs)
    {
        this.descs = descs;
    }

    /**
     * @return the names
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "itemName")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpLocaleItemStr> getNames()
    {
        return names;
    }

    /**
     * @param names the names to set
     */
    public void setNames(Set<SpLocaleItemStr> names)
    {
        this.names = names;
    }

    /**
     * @return the container
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpLocaleContainerID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpLocaleContainer getContainer()
    {
        return container;
    }

    /**
     * @param container the container to set
     */
    public void setContainer(SpLocaleContainer container)
    {
        this.container = container;
    }

    /**
     * @return the webLinkName
     */
    @Column(name = "WebLinkName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getWebLinkName()
    {
        return webLinkName;
    }

    /**
     * @param webLinkName the webLinkName to set
     */
    public void setWebLinkName(String webLinkName)
    {
        this.webLinkName = webLinkName;
    }

    /**
     * @return the isRequired
     */
    @Column(name = "IsRequired", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public Boolean getIsRequired()
    {
        return isRequired == null ? false : isRequired;
    }

    /**
     * @param isRequired the isRequired to set
     */
    public void setIsRequired(Boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /**
     * @return the spExportSchemaItems
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spLocaleContainerItem")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpExportSchemaItem> getSpExportSchemaItems()
    {
        return spExportSchemaItems;
    }

    /**
     * @param spExportSchemaItems the spExportSchemaItems to set
     */
    public void setSpExportSchemaItems(Set<SpExportSchemaItem> spExportSchemaItems)
    {
        this.spExportSchemaItems = spExportSchemaItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpLocaleContainerItem.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spLocaleContainerItemId;
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
        return 504;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace#fillDescs(java.util.List)
     */
    public void fillDescs(List<LocalizableStrIFace> descsArg)
    {
        descsArg.clear();
        descsArg.addAll(descs);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace#fillNames(java.util.List)
     */
    public void fillNames(List<LocalizableStrIFace> namesArg)
    {
        namesArg.clear();
        namesArg.addAll(names);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace#addDesc(edu.ku.brc.specify.tools.schemalocale.LocalizableStrIFace)
     */
    public void addDesc(LocalizableStrIFace str)
    {
        if (str != null && str instanceof SpLocaleItemStr)
        {
            SpLocaleItemStr strItem = (SpLocaleItemStr)str;
            strItem.setItemDesc(this);
            descs.add(strItem);
        } else
        {
            log.error("LocalizableStrIFace was null or not of Class SpLocaleItemStr");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace#addName(edu.ku.brc.specify.tools.schemalocale.LocalizableStrIFace)
     */
    public void addName(LocalizableStrIFace str)
    {
        if (str != null && str instanceof SpLocaleItemStr)
        {
            SpLocaleItemStr strItem = (SpLocaleItemStr)str;
            strItem.setItemName(this);
            names.add(strItem);
        } else
        {
            log.error("LocalizableStrIFace was null or not of Class SpLocaleItemStr");
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace#removeDesc(edu.ku.brc.specify.tools.schemalocale.LocalizableStrIFace)
     */
    public void removeDesc(LocalizableStrIFace str)
    {
        if (str != null && str instanceof SpLocaleItemStr)
        {
            descs.remove(str);
        } else
        {
            log.error("LocalizableStrIFace was null or not of Class SpLocaleItemStr");
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace#removeName(edu.ku.brc.specify.tools.schemalocale.LocalizableStrIFace)
     */
    public void removeName(LocalizableStrIFace str)
    {
        if (str != null && str instanceof SpLocaleItemStr)
        {
            names.remove((SpLocaleItemStr)str);
        } else
        {
            log.error("LocalizableStrIFace was null or not of Class SpLocaleItemStr");
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(SpLocaleContainerItem obj)
    {
        return name != null && obj != null && obj.getName() != null ? name.compareTo(obj.getName()) : 0;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.SpLocaleBase#getNamesSet()
     */
    @Transient
    public Set<SpLocaleItemStr> getNamesSet()
    {
        return names;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.SpLocaleBase#getDescsSet()
     */
    @Transient
    public Set<SpLocaleItemStr> getDescsSet()
    {
        return descs;
    }
}
