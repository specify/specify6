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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 25, 2008
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spreport")
@org.hibernate.annotations.Table(appliesTo="spquery", indexes =
    {   @Index (name="SpReportNameIDX", columnNames={"Name"})
    })
public class SpReport extends DataModelObjBase
{
    protected Integer           spReportId;
    protected String            name;
    protected String            remarks;
    
    protected SpQuery           query;
    protected SpAppResource     appResource;
    protected SpecifyUser       specifyUser;

 
    /**
     * 
     */
    public SpReport()
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spReportId       = null;
        name             = null;
        remarks          = null;
        query            = null;
        appResource      = null;
        specifyUser      = null;
    }
    
    @Id
    @GeneratedValue
    @Column(name = "SpReportId", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpReportId()
    {
        return spReportId;
    }

    public void setSpReportId(Integer spReportId)
    {
        this.spReportId = spReportId;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public void setSpecifyUser(SpecifyUser owner)
    {
        this.specifyUser = owner;
    }

    public void setQuery(SpQuery query)
    {
        this.query = query;
    }

    public void setAppResource(SpAppResource appResource)
    {
        this.appResource = appResource;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }
    
    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    /**
     * @return the remarks
     */
    @Lob
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpQueryID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpQuery getQuery()
    {
        return query;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AppResourceID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpAppResource getAppResource()
    {
        return appResource;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser() 
    {
        return this.specifyUser;
    }
    
    //----------------------------------------------------------------------
    //-- DataModelObjBase
    //----------------------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpReport.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spReportId;
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
        return 519;
    }
}
