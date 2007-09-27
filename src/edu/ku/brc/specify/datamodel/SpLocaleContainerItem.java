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

import java.util.HashSet;
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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

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
public class SpLocaleContainerItem extends SpLocaleBase implements SpLocalizableIFace
{
    protected Integer localeContainerItemId;
    protected SpLocaleContainer container;
    
    protected Set<SpLocaleItemStr> names;
    protected Set<SpLocaleItemStr> descs;

 
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
    @Column(name = "LocaleContainerItemID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLocaleContainerItemId()
    {
        return localeContainerItemId;
    }

    /**
     * @param localeContainerItemId the localeContainerItemId to set
     */
    public void setLocaleContainerItemId(Integer localeContainerItemId)
    {
        this.localeContainerItemId = localeContainerItemId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.LocaleBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.initialize();
        localeContainerItemId = null;
        
        names = new HashSet<SpLocaleItemStr>();
        descs = new HashSet<SpLocaleItemStr>();
        
        container = null;

    }

    /**
     * @return the descs
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "item")
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
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "item")
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
    @JoinColumn(name = "LocaleContainerID", unique = false, nullable = false, insertable = true, updatable = true)
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
        return localeContainerItemId;
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

}
