/* Copyright (C) 2021, Specify Collections Consortium
 *
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;

import javax.persistence.*;

/**
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collectingeventauthorization")
public class CollectingEventAuthorization extends DataModelObjBase implements java.io.Serializable,
        Comparable<CollectingEventAuthorization>
{

    // Fields

    protected Integer             collectingEventAuthorizationId;
    protected String              remarks;
    protected Permit              permit;
    protected CollectingEvent     collectingEvent;

    // Constructors

    /** default constructor */
    public CollectingEventAuthorization()
    {
        // do nothing
    }

    /** constructor with id */
    public CollectingEventAuthorization(Integer collectingEventAuthorizationId)
    {
        this.collectingEventAuthorizationId = collectingEventAuthorizationId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectingEventAuthorizationId = null;
        remarks = null;
        permit = null;
        collectingEvent = null;
    }

    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectingEventAuthorizationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectingEventAuthorizationId()
    {
        return this.collectingEventAuthorizationId;
    }

    /**
     * Generic Getter for the ID Property.
     *
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.collectingEventAuthorizationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectingEventAuthorization.class;
    }

    public void setCollectingEventAuthorizationId(Integer collectingEventAuthorizationId)
    {
        this.collectingEventAuthorizationId = collectingEventAuthorizationId;
    }

    /**
     *
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     * * Permit authorizing CollectingEvent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PermitID", unique = false, nullable = false, insertable = true, updatable = true)
    public Permit getPermit()
    {
        return this.permit;
    }

    public void setPermit(Permit permit)
    {
        this.permit = permit;
    }

    /**
     * * CollectingEvent authorized by permit
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectingEventID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectingEvent getCollectingEvent()
    {
        return this.collectingEvent;
    }

    public void setCollectingEvent(CollectingEvent collectingEvent)
    {
        this.collectingEvent = collectingEvent;
    }

    public int compareTo(CollectingEventAuthorization obj)
    {
        if (permit != null && permit.permitNumber != null &&
                obj.permit.permitNumber != null &&
                obj.permit.permitNumber != null)
        {
            return permit.permitNumber.compareTo(obj.permit.permitNumber);
        }
        // else
        return timestampCreated != null && obj != null && obj.timestampCreated != null ? timestampCreated.compareTo(obj.timestampCreated) : 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return CollectingEvent.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return collectingEvent != null ? collectingEvent.getId() : null;
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
        return 13;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        if (permit != null)
        {
            return permit.getIdentityTitle();
        }

        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(Permit.getClassTableId());
        return ti.getTitle();
    }
}
