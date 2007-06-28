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
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 
 * @code_status Beta
 * @author jstewart
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "determinationstatus")
public class DeterminationStatus extends DataModelObjBase implements Serializable
{
    protected Long               determinationStatusId;
    protected Boolean            isCurrent;
    protected String             name;
    protected String             remarks;
    protected Set<Determination> determinations;

    public DeterminationStatus()
    {
        super();
    }

    public DeterminationStatus(Long determinationStatusId)
    {
        super();
        this.determinationStatusId = determinationStatusId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        determinationStatusId = null;
        name = null;
        remarks = null;
        determinations = new HashSet<Determination>();
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "status")
    public Set<Determination> getDeterminations()
    {
        return determinations;
    }

    public void setDeterminations(Set<Determination> determinations)
    {
        this.determinations = determinations;
    }

    @Id
    @GeneratedValue
    @Column(name = "DeterminationStatusID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getDeterminationStatusId()
    {
        return determinationStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Long getId()
    {
        return this.determinationStatusId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return DeterminationStatus.class;
    }

    public void setDeterminationStatusId(Long determinationStatusId)
    {
        this.determinationStatusId = determinationStatusId;
    }

    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the isCurrent
     */
    @Column(name = "IsCurrent", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsCurrent()
    {
        return isCurrent;
    }

    /**
     * @param isCurrent the isCurrent to set
     */
    public void setIsCurrent(Boolean isCurrent)
    {
        this.isCurrent = isCurrent;
    }

    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
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
        return 501;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
        // TODO Auto-generated method stub
        return "DeterminationStatus: " + this.name;
    }

}
