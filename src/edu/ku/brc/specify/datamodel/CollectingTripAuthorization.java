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
@Table(name = "collectingtripauthorization")
public class CollectingTripAuthorization extends DataModelObjBase implements java.io.Serializable,
        Comparable<CollectingTripAuthorization>
{

    // Fields

    protected Integer             collectingTripAuthorizationId;
    protected String              remarks;
    protected Permit              permit;
    protected CollectingTrip     collectingTrip;

    // Constructors

    /** default constructor */
    public CollectingTripAuthorization()
    {
        // do nothing
    }

    /** constructor with id */
    public CollectingTripAuthorization(Integer collectingTripAuthorizationId)
    {
        this.collectingTripAuthorizationId = collectingTripAuthorizationId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectingTripAuthorizationId = null;
        remarks = null;
        permit = null;
        collectingTrip = null;
    }

    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectingTripAuthorizationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectingTripAuthorizationId()
    {
        return this.collectingTripAuthorizationId;
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
        return this.collectingTripAuthorizationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectingTripAuthorization.class;
    }

    public void setCollectingTripAuthorizationId(Integer collectingTripAuthorizationId)
    {
        this.collectingTripAuthorizationId = collectingTripAuthorizationId;
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
     * * Permit authorizing CollectingTrip
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
     * * CollectingTrip authorized by permit
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectingTripID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectingTrip getCollectingTrip()
    {
        return this.collectingTrip;
    }

    public void setCollectingTrip(CollectingTrip collectingTrip)
    {
        this.collectingTrip = collectingTrip;
    }

    public int compareTo(CollectingTripAuthorization obj)
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
        return CollectingTrip.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return collectingTrip != null ? collectingTrip.getId() : null;
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
        return 158;
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
